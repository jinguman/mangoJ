package com.kit.Util;

import org.junit.Test;

import edu.sc.seis.seisFile.mseed.Btime;


public class HelpersTest {

	@Test
	public void getEpochTime() {
		
		Btime bt = new Btime(1970, 1, 0, 1, 1, 8450);
		double d = Helpers.getEpochTime(bt);
		
		System.out.println(d);
	}
}
