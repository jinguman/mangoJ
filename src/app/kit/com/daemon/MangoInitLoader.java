package app.kit.com.daemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import app.kit.com.conf.MangoConf;
import app.kit.controller.mongo.MongoInitialClient;
import app.kit.controller.seedlink.SeedlinkStreamClient;
import app.kit.service.mongo.MongoInitialClientService;
import app.kit.vo.SLState;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Mango initial starter Loader Class
 * @author jman
 *
 */
@Slf4j
public class MangoInitLoader implements IMangoLoader{

	private AbstractApplicationContext ctx;
	private MangoConf conf;
	private SLState slState;
	private MongoInitialClientService initialService;
	ExecutorService exec = Executors.newCachedThreadPool();

	@Setter private String year; 
	
	int cnt = 0;
	
	@Override
	public void startEngine() throws Exception {

		log.info("Start Mango collector server.");

		ctx = new AnnotationConfigApplicationContext(MangoConf.class);
		ctx.registerShutdownHook();
		slState = ctx.getBean(SLState.class);
		conf = ctx.getBean(MangoConf.class);
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

		log.info("MongoDB initiate ---------------------------------------");
		initialService.getIndexes();
		log.info("Get index info. {}", slState.getIndexMap().toString());
		initialService.getShardCollections();
		log.info("Get shard info. {}", slState.getShardMap().toString());
		initialService.getShardRange();
		log.info("Get shardRange info. {}", slState.getShardRangeMap().toString());

		// MongoInitialClient Thread
		log.info("MongoDB thread execute --------------------------------");
    	cnt = conf.getMiThread();
    	for (int i = 0; i < cnt; i++) {
    		MongoInitialClient client = ctx.getBean(MongoInitialClient.class);
    		exec.execute(client);
    	}
 
    	// SeedlinkStreamClinet Thread
		log.info("Seedlink thread execute --------------------------------");
    	cnt = conf.getScThread();
    	if ( conf.isMiBuildEntireList() ) cnt = 1;
    	for(int i = 1; i < cnt+1; i++) {
    		SeedlinkStreamClient client = ctx.getBean(SeedlinkStreamClient.class,
    				conf.getScNetwork(i), conf.getScStation(i), conf.getScChannel(i), conf.getScHost(i), conf.getScPort(i), year);
    		
    		exec.execute(client);
    	}
	}

	@Override
	public void stopEngine() throws Exception {
		log.info("Stop Mango initiate server.");
        if ( ctx != null ) ctx.close();
	}
}
