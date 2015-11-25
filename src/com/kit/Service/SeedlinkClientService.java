package com.kit.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kit.Util.Helpers;
import com.kit.Util.LoggingOutputStream;
import com.kit.Util.PropertyManager;

import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
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
	@Setter @Getter private String station;
	@Setter @Getter private String location;
	@Setter @Getter private String channel;
	@Setter @Getter private String host;
	@Setter @Getter private String start;
	@Setter @Getter private String end;
	@Setter @Getter private int port;
	@Setter @Getter private int timeoutSeconds;
	@Setter @Getter private boolean verbose;

	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy,DDD,HH:mm:ss");	//2015,306,00:49:01.7750

	public SeedlinkClientService(BlockingQueue<Document> queue, PropertyManager pm) {
		this.queue = queue;
		this.pm = pm;
	}

	public void getTrace() throws SeedlinkException, SeedFormatException, UnsupportedCompressionType, IOException, CodecException, ParseException, InterruptedException, SocketTimeoutException {
		// Get seedlink
        SeedlinkReader reader = null;
		reader = new SeedlinkReader(host, port, timeoutSeconds, verbose);

		// Seedlink verbose setting
        LoggingOutputStream los = new LoggingOutputStream(logger, Level.DEBUG);
        PrintWriter out = new PrintWriter(los, true);
        if (verbose) reader.setVerboseWriter(out);
        
        try {
			reader.select(network, station, location, channel);
			reader.startData(start, end);
		} catch (SeedlinkException | IOException e) {
			logger.error("{} {}","Failure during reading seedlink.",e);
		}
        
        // Seedlink stream reading
        while (reader.hasNext()) {
            SeedlinkPacket slp = reader.readPacket();
            DataRecord dr = slp.getMiniSeed();

            String networkCode = dr.getHeader().getNetworkCode().trim();
            String stationIdentifier = dr.getHeader().getStationIdentifier().trim();
            String channelIdentifier = dr.getHeader().getChannelIdentifier().trim();
            String locationIdentifier = dr.getHeader().getLocationIdentifier().trim();

            String startTime = dr.getHeader().getStartTime();
            String endTime = dr.getHeader().getEndTime();

            float sampleRate = dr.getHeader().getSampleRate();
            int numSamples = dr.getHeader().getNumSamples();
            int sampleRateFactor = dr.getHeader().getSampleRateFactor();
            int sampleRateMultiplier = dr.getHeader().getSampleRateMultiplier();
            byte activityFlags = dr.getHeader().getActivityFlags();
            byte ioClockFlags = dr.getHeader().getActivityFlags();
            byte dataQualityFlags = dr.getHeader().getDataQualityFlags();
            int timeCorrection = dr.getHeader().getTimeCorrection();

            DecompressedData decomData = dr.decompress();
            int[] temp = decomData.getAsInt();

            List<Integer> ints = new ArrayList<Integer>();
            for(int i=0; i<temp.length; i++) 
            	ints.add(temp[i]);

            Document d = new Document()
					.append("st", Helpers.convertDatePerfectly(startTime, sdf, sdfToSecond))
					.append("n", numSamples)
					.append("c", sampleRateFactor)
					.append("m", sampleRateMultiplier)
					.append("s", sampleRate)
					.append("et", Helpers.convertDatePerfectly(endTime, sdf, sdfToSecond))
					.append("f", activityFlags + "," + ioClockFlags + "," + dataQualityFlags + "," + timeCorrection)
					.append("d", ints)
					.append("network", networkCode)
					.append("station", stationIdentifier)
					.append("location", locationIdentifier)
					.append("channel", channelIdentifier);

            queue.put(d);

        }
        reader.close();    
	}

	public Document getStreamsInfo() throws UnknownHostException, IOException, SeedlinkException, SeedFormatException {
		// Get seedlink
        SeedlinkReader reader = null;
		reader = new SeedlinkReader(host, port, timeoutSeconds, verbose);
		
		Document doc = new Document();
		
		// get station information
		//System.out.println(reader.getInfoString("STATIONS"));
		//System.out.println(reader.getInfoString("STREAMS"));
		String streams = reader.getInfoString("STREAMS");
		
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
	
	public void getTraceRaw() throws SeedlinkException, SeedFormatException, UnsupportedCompressionType, IOException, CodecException, ParseException, InterruptedException, SocketTimeoutException {
		// Get seedlink
        SeedlinkReader reader = null;
		reader = new SeedlinkReader(host, port, timeoutSeconds, verbose);

		// Seedlink verbose setting

        //LoggingOutputStream los = new LoggingOutputStream(logger, Level.DEBUG);
        //PrintWriter out = new PrintWriter(los, true);
        PrintWriter out = new PrintWriter(System.out, true);
        
        if (verbose) {
        	reader.setVerbose(true);
        	reader.setVerboseWriter(out);
        }

        try {

			reader.select(network, station, location, channel);
			reader.startData(start, end);
		} catch (SeedlinkException | IOException e) {
			logger.error("{} {}","Failure during reading seedlink.",e);
		}
        
        // Seedlink stream reading
        while (reader.hasNext()) {
            SeedlinkPacket slp = reader.readPacket();
            DataRecord dr = slp.getMiniSeed();

            String networkCode = dr.getHeader().getNetworkCode().trim();
            String stationIdentifier = dr.getHeader().getStationIdentifier().trim();
            String channelIdentifier = dr.getHeader().getChannelIdentifier().trim();
            String locationIdentifier = dr.getHeader().getLocationIdentifier().trim();

            String startTime = dr.getHeader().getStartTime();
            String endTime = dr.getHeader().getEndTime();

            float sampleRate = dr.getHeader().getSampleRate();
            int numSamples = dr.getHeader().getNumSamples();


            byte[] bytes = slp.getMseedBytes();
            Binary data = new Binary(bytes);

            Document d = new Document()
            		.append("st", Helpers.convertDatePerfectly(startTime, sdf, sdfToSecond))
					.append("n", numSamples)
					.append("s", sampleRate)
					.append("et", Helpers.convertDatePerfectly(endTime, sdf, sdfToSecond))
					.append("d", data)
					.append("network", networkCode)
					.append("station", stationIdentifier)
					.append("location", locationIdentifier)
					.append("channel", channelIdentifier);
            
            //System.out.println("Get packet. st:" + startTime + ", et: " + endTime );
            
            queue.put(d);

        }
        reader.close();    
	}
	

}
