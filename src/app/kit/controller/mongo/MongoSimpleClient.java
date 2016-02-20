package app.kit.controller.mongo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.result.UpdateResult;

import app.kit.com.conf.MangoConf;
import app.kit.com.queue.BlockingBigQueue;
import app.kit.service.mongo.MongoSimpleClientService;
import app.kit.vo.Gaps;
import app.kit.vo.SLState;
import app.kit.vo.Stats;
import app.kit.vo.Trace;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class MongoSimpleClient implements Runnable {

	@Autowired private BlockingBigQueue queue;
	@Autowired private MongoSimpleClientService service;
	@Autowired private MangoConf conf;
	@Autowired private SLState slState;
	private Stats stats = new Stats();
	private Gaps gaps = new Gaps();
	private File slStateFile;
	
	int logCnt = 0; // log print count
	int streamCnt = 0; // stream write count

	public void run() {
		
		slStateFile = new File(conf.getScState());
		
		Random random = new Random();
		if ( conf.getMcLogThreshold() > 0 ) logCnt = random.nextInt(conf.getMcLogThreshold());
		
		while(true) {

			List<Trace> traces = queue.take();
			if ( traces == null ) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					log.error("{}", e);
				}
				continue;
			}

			putTraceSafety(traces);
		}	
	}

	private void putTraceSafety(List<Trace> traces) {

		while(true) {
	 		try {
				int traceCnt = 1;
				int traceSize = traces.size();
				for(Trace trace : traces) {

					UpdateResult result = service.insertTrace(trace);
		
					// print log
					String logStr = "[" + (traceCnt++) + "/" + traceSize + "] " + trace.getNetwork() + "." + trace.getStation() + "." + trace.getLocation() + "." + trace.getChannel() + " " + trace.getStStr();
					if ( logCnt > conf.getMcLogThreshold() ) {
						if ( result.getModifiedCount() > 0 ) log.debug("Update trace({}). {}", queue.size(), logStr); 
						else if ( result.getUpsertedId() != null ) log.debug("Insert trace({}). {}", queue.size(), logStr);
						logCnt = 1;
					}
					logCnt++;
					
					// Update or Insert condition
					if ( result.getModifiedCount() > 0 || result.getUpsertedId() != null ) {
						slState.addStream(trace.getNetwork(), trace.getStation(), trace.getSeqnum(), trace.getStBtime());
						
						stats.put(trace);
						gaps.put(trace);
					}
					trace.clear();
				}
				service.insertStats(stats);
				service.insertGaps(gaps);
				
				if ( streamCnt > conf.getScStateThreshold()) {
					try {
						slState.saveStreams(slStateFile);
					} catch (IOException e) { log.warn("Failed to save slState. file: {}, {}", slStateFile.getAbsolutePath(), e.getMessage());
					}
					streamCnt = 0;
				} else {
					streamCnt++;
				}
				
				stats.clear();
				gaps.clear();
				traces.clear();
				return;
				
			} catch (MongoSocketReadException | MongoTimeoutException e) {
				log.error("{}", e);
				log.info("MongoClient restart after {} seconds.", conf.getMcRestartSec());
				try {
					Thread.sleep(conf.getMcRestartSec()*1000);
				} catch (InterruptedException e1) {
					log.error("{}",e1);
				}
			}
		}
	}
}
