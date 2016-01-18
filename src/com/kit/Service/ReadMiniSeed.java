package com.kit.Service;

public class ReadMiniSeed {
/*
	private TraceDao traceDao;
	private GenerateMiniSeed gm;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy,DDD,HH:mm:ss");	//2015,306,00:49:01.7750
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private MongoSimpleClientService mscs;
	
	final Logger logger = LoggerFactory.getLogger(ReadMiniSeed.class);

	public ReadMiniSeed(MongoClient client, MongoDatabase database, PropertyManager pm, SLState state ) {
		traceDao = new TraceDao(database);
		gm = new GenerateMiniSeed();
		mscs = new MongoSimpleClientService(pm, state);
	}
	
	public void read(File file) {
		
		DataInput input;
		try {
			input = new DataInputStream(new FileInputStream(file.toString()));
			
			while(true) {
				DataRecord dr = (DataRecord) SeedRecord.read(input);
				
				List<DataRecord> records = gm.splitPacketPerMinute(dr);
            	for(DataRecord record : records) {
            		writeDataRecord(record);
            	}
			}
		} catch (EOFException e) {
			
		} catch (FileNotFoundException e) {
			logger.warn("{}", e);
			e.printStackTrace();
		} catch (SeedFormatException e) {
			logger.warn("{}", e);
		} catch (IOException e) {
			logger.warn("{}", e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void writeDataRecord(DataRecord dr) throws ParseException {
		
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
			UpdateResult result = mscs.insertTraceRaw(Helpers.dRecordToDoc(dr, st, et));
			logger.debug("Case 1 : {}", result);
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
				
				System.out.println(Helpers.getBtimeBeforeOneSample(stReadDRBtime, sampleRate).toString() + ", " + stReadDRBtime.toString());
				
				String drStStr = Helpers.convertDatePerfectly(drNew.getHeader().getStartTime(), sdf, sdfToSecond);
				String drEtStr = Helpers.convertDatePerfectly(drNew.getHeader().getEndTime(), sdf, sdfToSecond);
				
				UpdateResult result = mscs.insertTraceRaw(Helpers.dRecordToDoc(drNew, drStStr, drEtStr));
				logger.debug("Case 2. ori: {}, mod: {}", dr.toString(), drNew.toString());
				logger.debug("Case 2. {}", result);
				
			}
			
			stReadDRBtime = etMongoBtime;
			
		}
		
		// DR패킷시작시간이 DB패킷종료시간보다 앞에 있고..
		if ( stReadDRBtime.before(etReadDRBtime) ) {
			
			DataRecord drNew = gm.trimPacket(Helpers.getBtimeBeforeOneSample(stReadDRBtime, sampleRate), etReadDRBtime, dr, true);
			
			String drStStr = Helpers.convertDatePerfectly(drNew.getHeader().getStartTime(), sdf, sdfToSecond);
			String drEtStr = Helpers.convertDatePerfectly(drNew.getHeader().getEndTime(), sdf, sdfToSecond);
			
			UpdateResult result = mscs.insertTraceRaw(Helpers.dRecordToDoc(drNew, drStStr, drEtStr));
			logger.debug("Case 4. ori: {}, mod: {}", dr.toString(), drNew.toString());
			logger.debug("Case 4. {}", result);
		}
	}
*/	
}

