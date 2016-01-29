package app.kit.com.daemon;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import app.kit.com.conf.MangoConf;
import app.kit.controller.mongo.MongoSimpleClient;
import app.kit.controller.seedlink.SeedlinkClient;
import app.kit.service.mongo.MongoInitialClientService;
import app.kit.vo.SLState;
import lombok.extern.slf4j.Slf4j;

/**
 * Mango collector starter Loader Class
 * @author jman
 *
 */
@Slf4j
public class MangoCollectorLoader implements IMangoLoader{

	private AbstractApplicationContext ctx;
	private MangoConf conf;
	private SLState slState;
	private MongoInitialClientService initialService;
	ExecutorService exec = Executors.newCachedThreadPool();
	int cnt = 0;

	@Override
	public void startEngine() throws Exception {

		log.info("Start Mango collector server.");

		ctx = new AnnotationConfigApplicationContext(MangoConf.class);
		ctx.registerShutdownHook();
		conf = ctx.getBean(MangoConf.class);
		slState = ctx.getBean(SLState.class);
		initialService = ctx.getBean(MongoInitialClientService.class);
				
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					stopEngine();
				} catch (Exception e) {
					log.error("There is error in Mango collector server.",e);
				}
			}
		});

		// stream에 기록하고 이어받기 하는 것으로 대체함.
		//if ( conf.isMfResumeQueue() ) {
		//	log.info("Loading queue...");
		//	File file = new File(conf.getMfQueue());
		//	queue.load(file);
		//	log.info("Read from file. size: {}, file: {}", queue.size(), file.getAbsolutePath());
		//}
		
		// loading stream
		try {
			slState.restoreStreams(new File(conf.getScState()));
		} catch(IOException e) {
			log.info("Nothing to load from file. {}", conf.getScState());
		}
		
		log.info("MongoDB initiate ---------------------------------------");
		initialService.getIndexes();
		log.info("Get index info. {}", slState.getIndexMap().toString());
		initialService.getShardCollections();
		log.info("Get shard info. {}", slState.getShardMap().toString());
		initialService.getShardRange();
		log.info("Get shardRange info. {}", slState.getShardRangeMap().toString());

    	// MongoSimpleClient Thread
		log.info("MongoDB thread execute --------------------------------");
    	int threadCnt = conf.getMcThread();
    	for (int i = 0; i < threadCnt; i++) {
    		MongoSimpleClient client = ctx.getBean(MongoSimpleClient.class);
    		exec.execute(client);
    	}

    	// SeedlinkClient Thread
		log.info("Seedlink thread execute --------------------------------");

    	cnt = conf.getScThread();
    	for(int i = 1; i < cnt+1; i++) {
    		SeedlinkClient client = ctx.getBean(SeedlinkClient.class, 
    				conf.getScNetwork(i), conf.getScStation(i), conf.getScChannel(i), conf.getScHost(i), conf.getScPort(i));
    		exec.execute(client);
    	}
	}

	@Override
	public void stopEngine() throws Exception {
		log.info("Stop Mango collector server.");
		
        // Mongo shutdown
		exec.shutdown();
        if (!exec.awaitTermination(5, TimeUnit.SECONDS)) { 
        	log.info("Executor did not terminate in the specified time."); 
            
            List<Runnable> droppedTasks = exec.shutdownNow();
            log.info("Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
        }
		
        // write state
        slState.saveStreams(new File(conf.getScState()));
        log.info("Write stream state.");
        
        // stream에 기록하고 이어받기 하는 것으로 대체함.
		// Write contents of queue to file
		//if ( queue.size() > 0 ) {
		//	File file = new File(conf.getMfQueue());
		//	queue.save(file);
		//	log.info("Write contents of queue to file. size: {}, file: {}", queue.size(), file.getAbsolutePath());
		//}
		
        if ( ctx != null ) ctx.close();
	}

}
