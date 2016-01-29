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
import app.kit.vo.FileContentVo;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class FileParserTest {

	@Autowired private FileParser parser;
	
	@Test
	public void parsing() throws IOException {
	
		File file = new File("d:/test.bckup");
		
		// save
		List<String> lines = new ArrayList<>();
		lines.add("2016-01-18T16:00:00.0000 2016-01-18T16:03:00.0000 AK * * * d:/temp/");
		//lines.add("2016-01-18T16:00:00.0000 2016-12-15T01:00:00.0000 PB B013 * * d:/temp/");

		FileUtils.writeLines(file, lines);
		
		List<FileContentVo> contents = parser.parse(file);
		System.out.println(contents.size());
		for(FileContentVo content : contents) {
			System.out.println(content);
		}
	}
}

