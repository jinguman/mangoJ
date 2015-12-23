package com.kit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.kit.Util.Helpers;

import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.SeedFormatException;

public class Test {

	public static void main(String[] args) throws ParseException, SeedFormatException, IOException {
		
		//>>>>>>>>> stReadDRBtime >>>>>> 2015-12-15T00:00:06.5184 .. BTime(2015:349:0:0:6.5184)
		//>>>>>>>>> stReadDRBtime >>>>>> 2015-12-15T00:00:07.0084 .. BTime(2015:349:0:0:7.84)
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		
		Btime bt = Helpers.getBtime("2015-12-16T12:23:34.0084", format);

		String str = bt.getYear() + "." + bt.getJDay() + "." + bt.getHour() + "." + bt.getMin() + "." + bt.getSec();
		
		double d = Helpers.getEpochTime(bt);
		
		System.out.println(str + ", " + bt.toString() + ", " + d);

	}
	
	
	

}
