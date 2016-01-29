package app.kit.monitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import app.kit.com.conf.MangoConf;
import app.kit.service.miniseed.WriteMiniSeed;
import app.kit.service.mongo.TraceStatsDao;
import app.kit.vo.FileContentVo;

/**
 * Backup worker
 * filename: xxxx.bckup
 * contents: START_TIME END_IME NETWORK STATION LOCATION CHANNEL DIR
 *           yyyy-MM-ddThh:mm yyyy-MM-ddThh:mm AK * * * d:/
 * @author jman
 *
 */
@Component
public class BckupWorker {

	@Autowired private FileParser parser;
	@Autowired private TraceStatsDao traceStatsDao;
	@Autowired private MangoConf conf;
	private static ApplicationContext context;
	private Executor exec;
	
    @Autowired
    public void init(ApplicationContext context) {
        BckupWorker.context = context;
    }
	
	public void service(File file) {
		exec = Executors.newFixedThreadPool(conf.getMcThread());
		List<FileContentVo> contents = parser.parse(file);
		List<FileContentVo> newContents = new ArrayList<>();
		
		for(FileContentVo content: contents) {
		
			List<Document> docs = traceStatsDao.findTraceStats(content.getNetwork(), content.getStation(), content.getLocation(), content.getChannel(), null, null);
			int totSize = docs.size();
			
			for(int i=0; i<totSize; i++) {
				Document doc = docs.get(i);
				FileContentVo newContent = new FileContentVo(doc.getString("net"), doc.getString("sta"), doc.getString("loc"), doc.getString("cha"), 
						content.getRootDir(), content.getStBtime(), content.getEtBtime());
				newContents.add(newContent);
			}
		}
		
		int i=0, totSize=newContents.size();
		for(FileContentVo c : newContents) {
			exec.execute(context.getBean(WriteMiniSeed.class, c, ++i, totSize));
		}
	}
}
