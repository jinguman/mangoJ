package com.kit.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kit.Util.Helpers;
import com.kit.Util.PropertyManager;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkPacket;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import lombok.Getter;
import lombok.Setter;

public class SeedlinkClientService {

	final Logger logger = LoggerFactory.getLogger(SeedlinkClientService.class);
	final String EMPTY = SeedlinkReader.EMPTY;

	private BlockingQueue<Document> queue;
	private PropertyManager pm;

	@Setter @Getter private String network;
	//@Setter @Getter private String station;
	@Setter @Getter private String location;
	@Setter @Getter private String channel;
	@Setter @Getter private String host;
	@Setter @Getter private String start;
	@Setter @Getter private String end;
	@Setter @Getter private int port;
	@Setter @Getter private int timeoutSeconds;
	@Setter @Getter private boolean verbose = false;
	private boolean isSharpMinute = false;
	@Setter @Getter private List<String> stations;

	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy,DDD,HH:mm:ss");	//2015,306,00:49:01.7750
	private GenerateMiniSeed gm;

	public SeedlinkClientService(BlockingQueue<Document> queue, PropertyManager pm) {
		this.queue = queue;
		this.pm = pm;
		isSharpMinute = pm.getBooleanProperty("mc.sharpMinute");
		gm = new GenerateMiniSeed();
	}

	public Document getStreamsInfo() throws UnknownHostException, IOException, SeedlinkException, SeedFormatException {
		// Get seedlink
        SeedlinkReader reader = null;
		Document doc = new Document();
		String streams;

		try {
	        reader = new SeedlinkReader(host, port, timeoutSeconds, verbose);
			streams = reader.getInfoString("STREAMS");
		} finally {
			if ( reader != null ) reader.close();
		}
		if ( streams == null ) {
			logger.warn("No info streams.");
			return null;
		}

		InputSource is = new InputSource(new StringReader(streams)); 
		try {
			org.w3c.dom.Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			Element root = xml.getDocumentElement();
			
			NodeList stationList = root.getElementsByTagName("station"); 
			Map<String, List<Document>> networkMap = new HashMap<>();
			for(int i=0; i< stationList.getLength(); i++) {
				Node stationNode = stationList.item(i);
				
				if ( stationNode.getNodeType() == Node.ELEMENT_NODE ) {
					Element stationElemnt = (Element) stationNode;
					
					String network = stationElemnt.getAttribute("network");
					String station = stationElemnt.getAttribute("name");
					
					// stream еб╠в
					NodeList streamList = stationElemnt.getElementsByTagName("stream");
					List<Document> channelDocList = new ArrayList<Document>();
					for(int j=0; j<streamList.getLength(); j++) {
						Node streamNode = streamList.item(j);
						if ( streamNode.getNodeType() == Node.ELEMENT_NODE ) {
							Element streamElemnt = (Element) streamNode;
							String location = streamElemnt.getAttribute("location");
							String channel = streamElemnt.getAttribute("seedname");
							
							Document channelDoc = new Document();
							channelDoc.append(location, channel);
							channelDocList.add(channelDoc);
						}
					}
					Document stationDoc = new Document();
					stationDoc.append(station, channelDocList);
					
					List<Document> stationDocList = null;
					if ( networkMap.get(network) == null ) {
						stationDocList = new ArrayList<>();
					} else {
						stationDocList = networkMap.get(network);
					}

					stationDocList.add(stationDoc);
					networkMap.put(network, stationDocList);
				}
			}
			
			for( String key : networkMap.keySet() ){
	            List<Document> stationDocList = networkMap.get(key);
	            doc.append(key, stationDocList);
	        }
			
		} catch (SAXException | ParserConfigurationException e) {
			logger.error("{}", e);
		}
		
		return doc;
	}
	
	public void getTraceRaw() throws UnknownHostException, IOException, SeedlinkException, SeedFormatException, ParseException, InterruptedException {
		
        SeedlinkReader reader = null;
		
        try {
        	reader = new SeedlinkReader(host, port, timeoutSeconds, verbose);

    		// queue limit
    		int queueLimit = pm.getIntegerProperty("sc.queuelimit");
    		
    		// Seedlink verbose setting
            //LoggingOutputStream los = new LoggingOutputStream(logger, Level.DEBUG);
            //PrintWriter out = new PrintWriter(los, true);
            //PrintWriter out = new PrintWriter(System.out, true);
            
            //if (verbose) {
            //	reader.setVerbose(true);
            //	reader.setVerboseWriter(out);
            //}

            try {

    			//reader.select(network, station, location, channel);
    			//reader.sendCmd("DATA 00000000");

            	for(String sta : stations) {
            		reader.select(network, sta, location, channel);
        			reader.sendCmd("DATA 00000000");
            	}

            	reader.endHandshake();
    		} catch (SeedlinkException | IOException e) {
    			logger.error("{} {}","Failure during reading seedlink.",e);
    		}

            // Seedlink stream reading
            while (reader.hasNext()) {
                SeedlinkPacket slp = reader.readPacket();
                DataRecord dr = slp.getMiniSeed();

                if (isSharpMinute) {

                	List<DataRecord> records = gm.splitPacketPerMinute(dr);
                	for(DataRecord record : records) {
                		
                		String startTime = record.getHeader().getStartTime();
                        String endTime = record.getHeader().getEndTime();
                        Document d = Helpers.dRecordToDoc(record, Helpers.convertDatePerfectly(startTime, sdf, sdfToSecond), Helpers.convertDatePerfectly(endTime, sdf, sdfToSecond));

                        queue.put(d);
                	}
                	
                } else {
                	String startTime = dr.getHeader().getStartTime();
                    String endTime = dr.getHeader().getEndTime();
                    Document d = Helpers.dRecordToDoc(dr, Helpers.convertDatePerfectly(startTime, sdf, sdfToSecond), Helpers.convertDatePerfectly(endTime, sdf, sdfToSecond));

                    queue.put(d);
                }
                
                if ( queueLimit > 0 ) {
                	if ( queue.size() > queueLimit ) {
                    	logger.debug("Queue is full. size: {}, init..", queue.size());
                    	
                    	while(true) {
                    		try {
                    			queue.remove();
                    			
                    		} catch (NoSuchElementException e) {
                    			break;
                    		}
                    	}
                    }
                }
                
            }
             
        } finally {
        	if ( reader != null ) reader.close();
        }
           
	}
	
	public List<String> getStationListFromStreamsInfo(String network) throws UnknownHostException, SeedlinkException, SeedFormatException, IOException {
		
		List<String> stations = new ArrayList<>();
		
		Document doc = getStreamsInfo();
		List<Document> stationListDoc = (List<Document>) doc.get(network);
		if ( stationListDoc == null ) return stations;
		
		for(Document stationDoc : stationListDoc) {
			for( String station : stationDoc.keySet() ){
				stations.add(station);
	        }
		}
		
		return stations;
	}

}
