package app.kit.monitor;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileListener implements FileAlterationListener {

	@Autowired private BckupWorker bckupWorker;
	
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
		
		log.debug("New file created. " + file.getAbsolutePath());

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
