package com.kit.SeedlinkClient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Service.SeedlinkClientService;
import com.kit.Util.PropertyManager;

import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import lombok.Getter;
import lombok.Setter;

public class SeedlinkClient implements Runnable {

	final Logger logger = LoggerFactory.getLogger(SeedlinkClient.class);
	final String EMPTY = SeedlinkReader.EMPTY;
	
	private BlockingQueue<Document> queue;
	private PropertyManager pm;
	
	@Setter @Getter private String network;
	@Setter @Getter private String station = "*";
	@Setter @Getter private String location = EMPTY;
	@Setter @Getter private String channel = "???";
	@Setter @Getter private String host = SeedlinkReader.DEFAULT_HOST;
	@Setter @Getter private String start = EMPTY;
	@Setter @Getter private String end = EMPTY;
	@Setter @Getter private int port = SeedlinkReader.DEFAULT_PORT;
	@Setter @Getter private int timeoutSeconds = SeedlinkReader.DEFAULT_TIMEOUT_SECOND;
	@Setter @Getter private boolean verbose = false;

	public SeedlinkClient(BlockingQueue<Document> queue, PropertyManager pm) {
		this.queue = queue;
		this.pm = pm;
	}

	public void run() {

		logger.info("SeedlinkClient start.. network: {}", network);

		SeedlinkClientService scs = new SeedlinkClientService(queue, pm);
		scs.setNetwork(network);
		scs.setStation(station);
		scs.setLocation(location);
		scs.setChannel(channel);
		scs.setHost(host);
		scs.setStart(start);
		scs.setEnd(end);
		scs.setPort(port);
		scs.setTimeoutSeconds(timeoutSeconds);
		scs.setVerbose(verbose);
		
		while(true) {
			try {
				scs.getTraceRaw();
			} catch (SeedlinkException | SeedFormatException | IOException | CodecException | ParseException
					| InterruptedException e) {
				logger.warn("{} {}", "Failure in Seedlink.", e);
			}

			int restartSec = pm.getIntegerProperty("sc.restartsec");
			logger.info("SeedlinkClient restart after {} seconds.. network: {}", restartSec, network);
			try {
				Thread.sleep(restartSec*1000);
			} catch (InterruptedException e) {
				logger.warn("{} {}","Error during sleep.",e);
			}
		} 
	}


}
