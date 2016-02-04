package app.kit.controller.mongo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.MongoSocketReadException;

import app.kit.com.conf.MangoConf;
import app.kit.com.queue.BlockingMessageQueue;
import app.kit.service.mongo.MongoInitialClientService;
import app.kit.vo.Trace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class MongoInitialClient implements Runnable {

	@Autowired private BlockingMessageQueue queue; 
	@Autowired private MongoInitialClientService initialService;
	@Autowired private MangoConf conf;

	public void run() {

		try {
			take();
		} catch (MongoSocketReadException e) {
			log.error("{}", e);
			log.info("MongoClient restart after {} seconds.", conf.getMiRestartSec());
			try {
				Thread.sleep(conf.getMiRestartSec()*1000);
			} catch (InterruptedException e1) {
				log.error("{}", e1);
			}
		} catch(InterruptedException e ) {
			log.error("{}", e);
		}
	}

	private void take() throws InterruptedException {
		while(true) {

			List<Trace> traces = queue.take();
			
			for(Trace trace : traces) {
				String network = trace.getNetwork();
				String station = trace.getStation();
				String location = trace.getLocation();
				String channel = trace.getChannel();
				String year = trace.getStartYear();
				String month = trace.getStartMonth();
				
				if ( conf.isMiIndex() ) initialService.doEtIndex(network, station, location, channel, year, month, false);
				if ( conf.isMiShard() ) initialService.doShard(network, station, location, channel, year, month);
			}
			log.info("Execute initiate.({}). [{}/{}]", queue.size(), traces.size(), traces.size());
		}
	}

}
