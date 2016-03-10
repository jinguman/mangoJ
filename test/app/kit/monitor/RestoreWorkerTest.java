package app.kit.monitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.PathFileComparator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class RestoreWorkerTest {

	@Autowired private RestoreWorker worker;
	
	@Test
	public void parsing() throws IOException {

		File file = new File("d:/test.restore");
		List<String> lines = new ArrayList<>();
		//lines.add("d:/temp/ false");
		lines.add("D:/26931_USR0400 true");

		FileUtils.writeLines(file, lines);
		worker.service(file);

		while(true) {}
	}
	
	//@Test
	public void sort() {
		
		String dir = "d:/ttt";
		List<File> files = (List<File>) FileUtils.listFiles(new File(dir), null, true);
		
		// sorting
		Collections.sort(files, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return PathFileComparator.PATH_REVERSE.compare(o1, o2);
			}
		});
		
		for(File f : files) {
			System.out.println(f.getAbsolutePath());
		}
	}
}

