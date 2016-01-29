package app.kit.monitor;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import app.kit.com.util.Helpers;
import app.kit.vo.FileContentVo;
import edu.sc.seis.seisFile.mseed.Btime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileParser {
	
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
					
					String st = words[0];
					Btime stBtime = Helpers.getBtime(st, new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"));
					String et = words[1];
					Btime etBtime = Helpers.getBtime(et, new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"));
					etBtime.sec = 59;
					etBtime.tenthMilli = 9999;
					String network = words[2];
					String station = words[3];
					String location = words[4];
					String channel = words[5];
					String rootDir = "";
					if ( words.length >= 7 ) rootDir = words[6];

					FileContentVo vo = new FileContentVo(network, station, location, channel, rootDir, stBtime, etBtime); 
					contents.add(vo);
				}
			}
		} catch (IOException | ParseException e) { log.warn("{}", e);
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
				String[] words = line.trim().split(" ");
				
				FileContentVo content = new FileContentVo();
				content.setRootDir(words[0]);
				if ( words[1].equals("true") ) content.setDbCheck(true);
				else content.setDbCheck(false);
				
				contents.add(content);
			}
			
		} catch (IOException e) { log.warn("{}", e);
		}
		return contents;
	}
	
	public List<FileContentVo> parse3(File file) {
		
		List<FileContentVo> contents = new ArrayList<>();
		try {
			List<String> lines = FileUtils.readLines(file);
			
			for(String line : lines) {
				if ( line.startsWith("#") ) continue;
				String[] words = line.trim().split(" ");
				
				FileContentVo content = new FileContentVo();
				Btime stBtime = Helpers.getBtime( words[0], new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"));
				content.setStBtime(stBtime);
				Btime etBtime = Helpers.getBtime( words[1], new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"));
				etBtime.sec = 59;
				etBtime.tenthMilli = 9999;
				content.setEtBtime(etBtime);
				
				content.setNetwork(words[2]);
				content.setStation(words[3]);
				content.setLocation(words[4]);
				content.setChannel(words[5]);
				
				contents.add(content);
			}
			
		} catch (IOException | ParseException e) { log.warn("{}", e);
		}
		return contents;
	}
}
