package com.kit.SeedlinkClient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Service.SeedlinkClientService;
import com.kit.Util.Helpers;
import com.kit.Util.PropertyManager;
import com.kit.Vo.SLState;

import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import lombok.Getter;
import lombok.Setter;

public class SeedlinkStreamClient implements Runnable {

	final Logger logger = LoggerFactory.getLogger(SeedlinkStreamClient.class);
	final String EMPTY = SeedlinkReader.EMPTY;
	
	private BlockingQueue<Document> queue;
	private PropertyManager pm;
	
	@Setter private String[] networks;
	@Setter @Getter private String host = SeedlinkReader.DEFAULT_HOST;
	@Setter @Getter private String start = EMPTY;
	@Setter @Getter private String end = EMPTY;
	@Setter @Getter private int port = SeedlinkReader.DEFAULT_PORT;
	@Setter @Getter private int timeoutSeconds = SeedlinkReader.DEFAULT_TIMEOUT_SECOND;
	@Setter @Getter private boolean verbose = false;
	private Document streamsInfoDoc = null;
	private boolean isBuildEntireList = false;

	public SeedlinkStreamClient(BlockingQueue<Document> queue, PropertyManager pm) {
		this.queue = queue;
		this.pm = pm;
		
		isBuildEntireList = pm.getBooleanProperty("mi.buildentirelist");
	}

	public void run() {

		logger.info("SeedlinkStreamClient start.. ");

		SeedlinkClientService scs = new SeedlinkClientService(queue, pm);
		scs.setHost(host);
		scs.setPort(port);
		scs.setTimeoutSeconds(timeoutSeconds);
		scs.setVerbose(verbose);

		while(true) {
			try {
				streamsInfoDoc = scs.getStreamsInfo();
				logger.debug("Get streamsInfoDoc.");
				Helpers.printJson(streamsInfoDoc);
				break;
			} catch (SeedlinkException | SeedFormatException | IOException e) {
				logger.warn("{} {}", "Failure in Seedlink.", e);
				
				int restartSec = pm.getIntegerProperty("sc.restartsec");
				logger.info("SeedlinkStreamClient restart after {} seconds. {}", restartSec);
				try {
					Thread.sleep(restartSec*1000);
				} catch (InterruptedException e1) {
					logger.error("{} {}","Error during sleep.",e1);
					return;
				}
			}
		}
		
		addStreams();
	}
	
	private void addStreams() {
		
		// { NETWORK : [{
		//			STATION: [{
		//					LOCATION:CHANNEL
		//					},{
		//					LOCATION:CHANNEL
		//			}]
		//			
		//Helpers.printJson(streamsInfoDoc);
		if ( isBuildEntireList ) {
			
			for(String network : streamsInfoDoc.keySet()) {
				List<Document> stationListDoc = (List<Document>) streamsInfoDoc.get(network);
				
				for(Document stationDoc : stationListDoc) {
					for( String station : stationDoc.keySet() ){
						List<Document> channelListDoc = (List<Document>) stationDoc.get(station);
						for(Document channelDoc : channelListDoc) {
							for( String location : channelDoc.keySet() ){
								String channel = channelDoc.getString(location);

								Document doc = new Document().append("network", network)
											.append("station", station)
											.append("location", location)
											.append("channel", channel);

								try {
									queue.put(doc);
								} catch (InterruptedException e) {
									logger.error("{}", e);
								}
							}
						}
			        }
				}
			}
			
		} else {
			
			for(String network : networks) {
				List<Document> stationListDoc = (List<Document>) streamsInfoDoc.get(network);
				if ( stationListDoc == null ) continue;
				
				for(Document stationDoc : stationListDoc) {
					for( String station : stationDoc.keySet() ){
						List<Document> channelListDoc = (List<Document>) stationDoc.get(station);
						for(Document channelDoc : channelListDoc) {
							for( String location : channelDoc.keySet() ){
								String channel = channelDoc.getString(location);

								Document doc = new Document().append("network", network)
											.append("station", station)
											.append("location", location)
											.append("channel", channel);

								try {
									queue.put(doc);
								} catch (InterruptedException e) {
									logger.error("{}", e);
								}
							}
						}
			        }
				}
			}
			
		}

	}
}
