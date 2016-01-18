package com.kit.Monitor;

public class RestoreWorker {
/*
	private List<FileContentVo> contents;
	private FileParser parser;
	private ReadMiniSeed readMiniSeed;
	private MongoSimpleClientService mscs;
	
	final Logger logger = LoggerFactory.getLogger(RestoreWorker.class);
	
	public RestoreWorker(MongoClient client, MongoDatabase database, PropertyManager pm, SLState state ) {
		parser = new FileParser();
		readMiniSeed = new ReadMiniSeed(client, database, pm, state);
	}
	
	public void service(File file) {
		
		contents = parser.parse2(file);

		for(FileContentVo content: contents) {

			String dir = content.getDir();

			// Get all files in directory
			List<File> files = (List<File>) FileUtils.listFiles(new File(dir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

			for(File f : files) {
				readMiniSeed.read(f);
			}
		}
	}
*/
}
