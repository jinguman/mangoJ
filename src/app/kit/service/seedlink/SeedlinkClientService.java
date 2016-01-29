package app.kit.service.seedlink;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import app.kit.com.conf.MangoConf;
import app.kit.com.queue.BlockingMessageQueue;
import app.kit.vo.InfoSeedlink;
import app.kit.vo.SLState;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkPacket;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("seedlinkClientService")
@Scope("prototype")
@Slf4j
public class SeedlinkClientService {

	@Setter private String networks;
	@Setter private String stations;
	@Setter private String channel;
	@Setter private String host;
	@Setter private int port;
	@Autowired private SLState slState;
	private String location = SeedlinkReader.EMPTY;
	
	@Autowired private GenerateMiniSeed gm;
	@Autowired BlockingMessageQueue queue;
	@Autowired MangoConf conf;

	private SeedlinkReader reader;
	private String[] networkList;
	private String[] stationList;

	public InfoSeedlink getInfoSeedlink() throws UnknownHostException, IOException, SeedlinkException, SeedFormatException {
		// Get seedlink
        SeedlinkReader reader = null;
		String streamsStr;

		try {
	        reader = new SeedlinkReader(host, port, SeedlinkReader.DEFAULT_TIMEOUT_SECOND, false);
	        streamsStr = reader.getInfoString("STREAMS");
		} finally {
			if ( reader != null ) reader.close();
		}

		if ( streamsStr == null ) {
			log.warn("No info streams.");
			return null;
		}

		XmlMapper mapper = new XmlMapper();
		InfoSeedlink info = mapper.readValue(streamsStr, InfoSeedlink.class);
		
		return info;
	}
	
	public void getTraceRaw() throws UnknownHostException, IOException, SeedlinkException, SeedFormatException, InterruptedException {

        try {
        	reader = new SeedlinkReader(host, port, SeedlinkReader.DEFAULT_TIMEOUT_SECOND, false);
            //PrintWriter out = new PrintWriter(System.out, true);
            //reader.setVerbose(true);
            //reader.setVerboseWriter(out);
        	
        	networkList = networks.split(",");
        	for(String net : networkList) {
        		
        		if ( stations.equals("*") && !host.equals(SeedlinkReader.DEFAULT_HOST)) {
        			InfoSeedlink info = getInfoSeedlink();
        			stationList = info.getStations(net);
        		} else {
        			stationList = stations.split(",");
        		}
        		
        		for(String sta : stationList) {
            		reader.select(net, sta, location, channel);
        			
            		int seqnum = slState.findStreamSeqnum(net, sta); 
            		String seqnumStr = Integer.toHexString(seqnum + 1);
            		
            		if ( seqnum > 0 && conf.isScResume() ) {
            			reader.sendCmd("DATA " + seqnumStr);
            			log.info("{}.{} requesting resume data from 0x {}(decimal: {})",net, sta, seqnumStr, seqnum);
            		} else {
            			reader.sendCmd("DATA");
            			log.info("{}.{} requesting data from current time",net, sta);
            		}
            	}
        		reader.endHandshake();
        	}

            // Seedlink stream reading
            List<Trace> traces = new ArrayList<Trace>();
            while (reader.hasNext()) {
            	
                SeedlinkPacket slp = reader.readPacket();
                DataRecord dr = slp.getMiniSeed();

                if (conf.isMcSharpMinute()) {

                	List<DataRecord> records = gm.splitPacketPerMinute(dr);
                	for(DataRecord record : records) {
                		traces.add(new Trace(record));
                	}
                } else {
                	traces.add(new Trace(dr));
                }
                
            	if ( reader.availableBytes() == 0 ) {
            		queue.put(traces);
            		traces = new ArrayList<>();
            	}
            	
            	// forced
            	if ( traces.size() > 50000 ) {
            		queue.put(traces);
            		traces = new ArrayList<>();
            		log.warn("Traces size > 50000. put the queue by force.");
            	}
                
                if ( conf.getScQueueLimit() > 0 ) {
                	if ( queue.size() > conf.getScQueueLimit() ) {
                    	log.info("Queue is full. size: {}, init..", queue.size());
                    	
                    	while(true) {
                    		try { queue.remove();
                    		} catch (NoSuchElementException e) { break;}
                    	}
                    }
                }
            } // end of while
             
        } finally {
        	if ( reader != null ) reader.close();
        }
	}

	public void close() {
		if ( reader != null ) reader.close();
	}
	
}
