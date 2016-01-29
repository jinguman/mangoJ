package app.kit.monitor;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import app.kit.com.conf.MangoConf;
import app.kit.service.miniseed.ReadMiniSeed;
import app.kit.vo.FileContentVo;
import lombok.extern.slf4j.Slf4j;

/**
 * filename: xxxx.restore
 * contents: DIR BOOLEAN(true if need check db, before data insert.)
 *           d:/temp/ true
 * @author jman
 *
 */
@Component
public class RestoreWorker {

	@Autowired private FileParser parser;
	@Autowired private MangoConf conf;
	private static ApplicationContext context;
	private Executor exec;
	
    @Autowired
    public void init(ApplicationContext context) {
        RestoreWorker.context = context;
    }
	
	public void service(File file) {
		exec = Executors.newFixedThreadPool(conf.getMcThread());
		List<FileContentVo> contents = parser.parse2(file);

		for(FileContentVo content: contents) {
			
			Collection<File> files = FileUtils.listFiles(new File(content.getRootDir()), null, true);
			int i=0, totSize=files.size();
			for(File f : files) {
				exec.execute(context.getBean(ReadMiniSeed.class, f, content.isDbCheck(), ++i, totSize));
			}
		}
	}
}
