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
public class DeleteWorkerTest {

	@Autowired private DeleteWorker worker;
	
	@Test
	public void parsing() throws IOException {

		File file = new File("d:/test.delete");
		List<String> lines = new ArrayList<>();
		lines.add("2016-03-01T00:00 2016-03-01T23:59 AK ANM * LHZ");

		FileUtils.writeLines(file, lines);
		worker.service(file);

		while(true) {}
	}
}

