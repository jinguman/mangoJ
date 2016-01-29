package app.kit.monitor;

import java.io.File;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class FileMonitor {

	/** TestDir Directory Listener */
	@Autowired private FileAlterationListener listener;

	private String suffix = ".bckup";
	private String suffix2 = ".restore";
	private String suffix3 = ".delete";
	
	/** watchdir Directory Monitor */
	private FileAlterationMonitor monitor;

	/**
	 * Monitor Start
	 */
	public void start(){

		if (ClassLoader.getSystemResource("watchdir") == null) {
			log.warn("There is no directory './watchdir'. FileMonitor module not loaded.");
			return;
		}

		File monitorDir = new File(ClassLoader.getSystemResource("watchdir").getFile());

		log.debug("FileMonitor start. " + monitorDir.getAbsolutePath());
		IOFileFilter files       = FileFilterUtils.or(
                FileFilterUtils.suffixFileFilter(suffix), FileFilterUtils.suffixFileFilter(suffix2), FileFilterUtils.suffixFileFilter(suffix3));
		FileAlterationObserver observer = new FileAlterationObserver(monitorDir,files);
		observer.addListener(listener);

		monitor = new FileAlterationMonitor(10000);
		monitor.addObserver(observer);
		try {
			monitor.start();
		} catch (Exception e) {
			log.error("FileMonitor could not start.");
		}
	}

	/**
	 * Monitor Stop
	 */
	public void stop(){
		log.debug("FileMonitor stop");
		try {
			monitor.stop();
		} catch (Exception e) {
			log.error("FileMonitor could not stop.");
		}
	}
}
