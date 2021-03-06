package com.kit.Util;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import edu.sc.seis.seisFile.mseed.Btime;


public class HelpersTest {

	@Test
	public void getEpochTime() {
		
		Btime bt = new Btime(1970, 1, 0, 1, 1, 8450);
		double d = Helpers.getEpochTime(bt);
		assertEquals(61.845, d, 0);
	}
	
	
	@Test
	public void getDiffByMinute() throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");
		
		String st1 = "2015-01-01T01:17:20.1234";
		String st2 = "2015-01-01T01:19:10.1234";
		
		long diff = Helpers.getDiffByMinute(st1, st2, format);
		assertEquals(2, diff);
		
		st1 = "2015-01-01T01:17:20.0000";
		st2 = "2015-01-01T01:16:21.9999";
		
		diff = Helpers.getDiffByMinute(st1, st2, format);
		assertEquals(-1, diff);
	}
	
	@Test
	public void getNextSharpMinute() throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");
		String st1 = "2015-01-01T01:14:54.5500";
		
		Btime bt = Helpers.getNextSharpMinute(st1, 1, format);
		Btime bt2 = Helpers.getNextSharpMinute(bt, -1);
		Btime bt3 = Helpers.getNextSharpMinute(bt2, 1);
		
		assertEquals(bt, bt3);
	}
}
