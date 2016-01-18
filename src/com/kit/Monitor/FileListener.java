package com.kit.Monitor;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

public class FileListener implements FileAlterationListener {

	final Logger logger = LoggerFactory.getLogger(FileListener.class);
	@Setter private BckupWorker bckupWorker;
	@Setter private RestoreWorker restoreWorker;

	
	@Override
	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDirectoryCreate(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDirectoryChange(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDirectoryDelete(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileCreate(File file) {
		
		logger.debug("New File Created : " + file.getAbsolutePath());

		String jdate = file.getName();
		
		// backup
		int idx = jdate.indexOf(".bckup");
		if ( idx > 0 ) {
			bckupWorker.service(file);
			FileUtils.deleteQuietly(file);
		}
		
		// restore
		idx = jdate.indexOf(".restore");
		if ( idx > 0 ) {
			//restoreWorker.service(file);
			FileUtils.deleteQuietly(file);
		}
	}

	@Override
	public void onFileChange(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileDelete(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub

	}

}
