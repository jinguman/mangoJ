package app.kit.service.miniseed;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.UpdateResult;

import app.kit.com.util.Helpers;
import app.kit.service.mongo.MongoSimpleClientService;
import app.kit.service.mongo.TraceDao;
import app.kit.service.seedlink.GenerateMiniSeed;
import app.kit.vo.FileContentVo;
import app.kit.vo.Gaps;
import app.kit.vo.Stats;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class ReadMiniSeed implements Runnable {

	@Autowired private TraceDao traceDao;
	@Autowired private GenerateMiniSeed gm;
	@Autowired private MongoSimpleClientService service;
	private Stats stats = new Stats();
	private Gaps gaps = new Gaps();
	private File file;
	private boolean isDbCheck;
	private int cnt;
	private int totSize;
	private static ApplicationContext context;
	
	public ReadMiniSeed(File file, boolean isDbCheck, int cnt, int totSize) {
		this.file = file;
		this.isDbCheck = isDbCheck;
		this.cnt = cnt;
		this.totSize = totSize;
	}
	
    @Autowired
    public void init(ApplicationContext context) {
    	ReadMiniSeed.context = context;
    }
	
	@Override
	public void run() {
		log.info("Reading file. {}", file.getAbsoluteFile());
		read();
	}
	
	public boolean read() {
		
		DataInput input;
		List<Trace> traces = new ArrayList<Trace>();
		try {
			
			input = new DataInputStream(new FileInputStream(file));	
			while(true) {
				DataRecord dr = (DataRecord) SeedRecord.read(input);
				List<DataRecord> records = gm.splitPacketPerMinute(dr);
				for(DataRecord record : records) {
            		traces.add(new Trace(record));
            	}
			}
		} catch (EOFException e) {
		} catch (SeedFormatException | IOException e) {
			log.warn("{}, {}", file.getAbsoluteFile(), e.toString());
		} catch (Exception e) {
			log.warn("{}, {}", file.getAbsoluteFile(), e.toString());
		}

		// write
		if ( traces.size() > 0 ) {
			if (isDbCheck) deleteDataRecord(traces);
			directWriteDataRecord(traces);
		}
		gaps.clear();
		
		return true;
	}
	
	public void deleteDataRecord(List<Trace> traces) {
		// assume trace in sort
		String network = traces.get(0).getNetwork();
		String station = traces.get(0).getStation();
		String location = traces.get(0).getLocation();
		String channel = traces.get(0).getChannel();
		
		// min stBtime & max etBtime
		Btime stBtime = traces.get(0).getStBtime();
		Btime etBtime = traces.get(0).getEtBtime();
		for(Trace trace : traces) {
			if ( stBtime.afterOrEquals(trace.getStBtime())) stBtime = trace.getStBtime();
			if ( trace.getStBtime().afterOrEquals(etBtime)) etBtime = trace.getStBtime();
		}
		stBtime = new Btime(stBtime.getAsBytes());stBtime.sec = 0;stBtime.tenthMilli = 0;
		etBtime = new Btime(etBtime.getAsBytes());etBtime.sec = 0;etBtime.tenthMilli = 0;
		
		FileContentVo content = new FileContentVo(network, station, location, channel, "/temp", stBtime, etBtime);
		DeleteMiniSeed deleteMiniSeed = context.getBean(DeleteMiniSeed.class, content, 1, 1);
		deleteMiniSeed.run();
	}
	
	/**
	 * Write to database
	 * @param dr
	 */
	public void directWriteDataRecord(List<Trace> traces) {
		
		/*
		for(Trace trace : traces) {
			System.out.println(Trace.getBtimeToStringYMDHMS(trace.getStBtime()) + " " +  Trace.getBtimeToStringYMDHMS(trace.getEtBtime()) + " " + trace.getNumSamples() );
		}
		*/
		
		
		for(Trace trace : traces) {

			UpdateResult result = service.insertTrace(trace);

			// Update or Insert condition
			if ( result.getModifiedCount() > 0 || result.getUpsertedId() != null ) {
				
				stats.put(trace);
				gaps.put(trace);
			}
			trace.clear();
		}
		service.insertStats(stats);
		service.insertGaps(gaps);

		log.info("Write complete({}/{}). file: {}, {}", cnt, totSize, file.getAbsolutePath(), stats.toString());
		
		stats.clear();
		gaps.clear();
		traces.clear();
		
	}
	
	public void writeDataRecord(DataRecord dr) throws ParseException {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy,DDD,HH:mm:ss");	//2015,306,00:49:01.7750
		SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		String network = dr.getHeader().getNetworkCode().trim();
		String station = dr.getHeader().getStationIdentifier().trim();
		String channel = dr.getHeader().getChannelIdentifier().trim();
		String location = dr.getHeader().getLocationIdentifier().trim();
		
		String st = Helpers.convertDatePerfectly(dr.getHeader().getStartTime(), sdf, sdfToSecond);
		String et = Helpers.convertDatePerfectly(dr.getHeader().getEndTime(), sdf, sdfToSecond);
		Btime stReadDRBtime = dr.getHeader().getStartBtime();
		Btime etReadDRBtime = dr.getHeader().getPredictedNextStartBtime();
		float sampleRate = dr.getHeader().getSampleRate();
		
		List<Document> documents = traceDao.getTraceTime(network, station, location, channel, st, et);
		
		// not exist
		if ( documents == null || documents.size() == 0 ) {
			Trace trace = new Trace(dr);
			UpdateResult result = service.insertTrace(trace);
			log.debug("Case 1 : {}", result);
			return;
		}
		
		// if exist
		for(Document doc: documents) {

			Btime stMongoBtime = Helpers.getBtime(doc.getString("st"), sdfToSecond);
			Btime etMongoBtime = Helpers.getBtime(doc.getString("et"), sdfToSecond);

			// 몽고DB시작시간이 DR패킷시작시간보다 뒤에 있고..
			// DR패킷시작시간~몽고DB시작시간-1/sampling을 취함
			if ( stMongoBtime.after(stReadDRBtime) ) {
				DataRecord drNew = gm.trimPacket(Helpers.getBtimeBeforeOneSample(stReadDRBtime, sampleRate), Helpers.getBtimeBeforeOneSample(stMongoBtime, sampleRate), dr, true);
				
				UpdateResult result = service.insertTrace(new Trace(drNew));
				log.debug("Case 2. ori: {}, mod: {}", dr.toString(), drNew.toString());
				log.debug("Case 2. {}", result);
				
			}
			
			stReadDRBtime = etMongoBtime;
			
		}
		
		// DR패킷시작시간이 DB패킷종료시간보다 앞에 있고..
		if ( stReadDRBtime.before(etReadDRBtime) ) {
			
			DataRecord drNew = gm.trimPacket(Helpers.getBtimeBeforeOneSample(stReadDRBtime, sampleRate), etReadDRBtime, dr, true);

			UpdateResult result = service.insertTrace(new Trace(drNew));
			log.debug("Case 4. ori: {}, mod: {}", dr.toString(), drNew.toString());
			log.debug("Case 4. {}", result);
		}
	}

}

