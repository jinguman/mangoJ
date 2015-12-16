package com.kit.Monitor;

import java.io.File;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;



public class FileMonitor {

	final Logger logger = LoggerFactory.getLogger(FileMonitor.class);
	/** TestDir Directory Listener */
	@Setter private FileAlterationListener listener;

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
			logger.warn("There is no directory './watchdir'. FileMonitor module not loaded.");
			return;
		}

		File monitorDir = new File(ClassLoader.getSystemResource("watchdir").getFile());

		logger.debug("FileMonitor start. " + monitorDir.getAbsolutePath());
		IOFileFilter files       = FileFilterUtils.or(
                FileFilterUtils.suffixFileFilter(suffix), FileFilterUtils.suffixFileFilter(suffix2), FileFilterUtils.suffixFileFilter(suffix3));
		FileAlterationObserver observer = new FileAlterationObserver(monitorDir,files);
		observer.addListener(listener);

		monitor = new FileAlterationMonitor(10000);
		monitor.addObserver(observer);
		try {
			monitor.start();
		} catch (Exception e) {
			logger.error("FileMonitor could not start.");
		}
	}

	/**
	 * Monitor Stop
	 */
	public void stop(){
		logger.debug("FileMonitor stop");
		try {
			monitor.stop();
		} catch (Exception e) {
			logger.error("FileMonitor could not stop.");
		}
	}
}
