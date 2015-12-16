package com.kit.Monitor;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class FileParserTest {

	@Test
	public void test() {
		
		File file = new File("d:/test.bckup");
		
		FileParser parser = new FileParser();
		List<FileContentVo> contents = parser.parse(file);
		
		for(FileContentVo content : contents) {
			System.out.println(content.toString());
		}
	}

}
