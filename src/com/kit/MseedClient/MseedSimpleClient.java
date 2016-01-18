package com.kit.MseedClient;

public class MseedSimpleClient {
/*
	// ���� �����Ͱ� ����� 
	// 1. full-overwrite: ������ ������.
	// 2. gap-overwrite: ����� ä���.
	final Logger logger = LoggerFactory.getLogger(MseedSimpleClient.class);
	
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy,DDD,HH:mm:ss");	//2015,306,00:49:01.7750
	private MongoSimpleClientService mscs;
	private GenerateMiniSeed gm;
	
	public MseedSimpleClient(PropertyManager pm, SLState state) {
		this.mscs = new MongoSimpleClientService(pm, state);
		this.gm = new GenerateMiniSeed();
	}
	
	public void read(String filename) {
	
		try {
			DataInput di = new DataInputStream(new FileInputStream(filename));

			while(true) {
				DataRecord record = (DataRecord) SeedRecord.read(di);
				
				List<DataRecord> records = gm.splitPacketPerMinute(record);
				
				for(DataRecord dr : records) {
					String startTime = dr.getHeader().getStartTime();
		            String endTime = dr.getHeader().getEndTime();
					Document d = Helpers.dRecordToDoc(dr, Helpers.convertDatePerfectly(startTime, sdf, sdfToSecond), Helpers.convertDatePerfectly(endTime, sdf, sdfToSecond));

					String network = d.getString("network");
					String station = d.getString("station");
					String channel = d.getString("channel");
					String location = d.getString("location");
					String st = d.getString("st");
					
					UpdateResult result = mscs.insertTraceRaw(d);
					
					String logStr = network + "." + station + "." + location + "." + channel + " " + st;
					if ( result.getModifiedCount() > 0 ) {
						logger.debug("Update trace. file: {}, {}", filename, logStr); 
					} else if ( result.getUpsertedId() != null ) {
						logger.debug("Insert trace. file: {}, {}", filename, logStr);
					}
				}
				
			}
			
		} catch (EOFException e) {
			
			System.out.println("EOF, so done.");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
		}

	}
	
*/		
	
}
