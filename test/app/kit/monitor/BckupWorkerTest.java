package app.kit.monitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class BckupWorkerTest {

	@Autowired private BckupWorker worker;
	
	@Test
	public void parsing() throws IOException {

		File file = new File("d:/test.bckup");
		List<String> lines = new ArrayList<>();
		lines.add("2016-02-10T20:00 2016-02-10T21:59 KS * * * d:/26931_USR0400_BACKUP/");

		FileUtils.writeLines(file, lines);
		worker.service(file);

		while(true) {}
	}
}

