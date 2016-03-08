package app.kit.com.util;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Test;

import app.kit.com.util.Helpers;
import app.kit.vo.StimeState;
import edu.sc.seis.seisFile.mseed.Btime;


public class HelpersTest {

	//@Test
	public void now() throws InterruptedException {
		
		while(true) {
			System.out.println(Helpers.getCurrentUTCBtime());
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void getEpochTime() {
		
		Btime bt = new Btime(1970, 1, 0, 1, 1, 8450);
		double d = Helpers.getEpochTime(bt);
		assertEquals(61.845, d, 0);
	}
	
	
	@Test
	public void getDiffByMinute() throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
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
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String st1 = "2015-01-01T01:14:54.5500";
		
		Btime bt = Helpers.getNextSharpMinute(st1, 1, format);
		Btime bt2 = Helpers.getNextSharpMinute(bt, -1);
		Btime bt3 = Helpers.getNextSharpMinute(bt2, 1);
		
		assertEquals(bt, bt3);
	}
	
	@Test
	public void getStFromFileName() throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String st1 = "2015-01-01";
		String s1 = Helpers.getStFromFileName(st1, format);
		assertEquals("2015-01-01T00:00:00.0000", s1);
		
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH");
		String st2 = "2015-12-31T12";
		String s2 = Helpers.getStFromFileName(st2, format2);
		assertEquals("2015-12-31T12:00:00.0000", s2);
		
		SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		String st3 = "2015-10-31T12:23";
		String s3 = Helpers.getStFromFileName(st3, format3);
		assertEquals("2015-10-31T12:23:00.0000", s3);
				
	}
	
	@Test
	public void getEtFromFileName() throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String st1 = "2015-01-01";
		String s1 = Helpers.getEtFromFileName(st1, format);
		assertEquals("2015-01-02T00:00:00.0000", s1);
		
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH");
		String st2 = "2015-12-31T12";
		String s2 = Helpers.getEtFromFileName(st2, format2);
		assertEquals("2015-12-31T13:00:00.0000", s2);
		
		SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		String st3 = "2015-10-31T12:23";
		String s3 = Helpers.getEtFromFileName(st3, format3);
		assertEquals("2015-10-31T12:24:00.0000", s3);
				
	}
	
	@Test
	public void getBtime() {
		
		Btime bt = new Btime(1999, 57, 12, 49, 50, 8450);
		
		assertEquals("1999",Helpers.getStrYearBtime(bt));
		assertEquals("057",Helpers.getStrJDayBtime(bt));
		assertEquals("12",Helpers.getStrHourBtime(bt));
		assertEquals("49",Helpers.getStrMinBtime(bt));
	}
	
	@Test
	public void splitSttimePerYearTest() {
		
		Btime stBtime = new Btime(2000, 1, 2, 1, 2 ,1200);
		Btime etBtime = new Btime(2001, 1, 2, 1, 2 ,1200);
		
		List<StimeState> lists = Helpers.splitSttimePerYear(stBtime, etBtime);
		
		for(StimeState s : lists) {
			System.out.println(s);
		}
	}
}
