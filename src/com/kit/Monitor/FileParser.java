package com.kit.Monitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileParser {

	final Logger logger = LoggerFactory.getLogger(FileParser.class);
	
	/**
	 * # Comment line
	 * ST ET NET STA LOC CHA DIR
	 * @param file
	 * @return
	 */
	public List<FileContentVo> parse(File file) {
		
		List<FileContentVo> contents = new ArrayList<>();
		try {
			List<String> lines = FileUtils.readLines(file);
			
			for(String line : lines) {
				
				if ( line.startsWith("#") ) continue;
				
				String[] words = line.trim().split(" ");
				
				if ( words.length >= 6 ) {

					FileContentVo content = new FileContentVo();

					content.setSt(words[0]);
					content.setEt(words[1]);
					content.setNetwork(words[2]);
					content.setStation(words[3]);
					content.setLocation(words[4]);
					content.setChannel(words[5]);
					
					if ( words.length >= 7 ) content.setDir(words[6]);
					
					contents.add(content);
				}
			}
			
		} catch (IOException e) {
			logger.warn("{}", e);
		}
		
		return contents;
	}
	
	/**
	 * # Comment line
	 * DIR
	 * @param file
	 * @return
	 */
	public List<FileContentVo> parse2(File file) {
		
		List<FileContentVo> contents = new ArrayList<>();
		try {
			List<String> lines = FileUtils.readLines(file);
			
			for(String line : lines) {
				
				if ( line.startsWith("#") ) continue;
				
				FileContentVo content = new FileContentVo();
				content.setDir(line.trim());
				contents.add(content);
			}
			
		} catch (IOException e) {
			logger.warn("{}", e);
		}
		
		return contents;
	}
}
