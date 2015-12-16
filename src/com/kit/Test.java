package com.kit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.kit.Util.Helpers;

import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.SeedFormatException;

public class Test {

	public static void main(String[] args) throws ParseException, SeedFormatException, IOException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		
		
		Btime bt = Helpers.getBtime("20151216", format);

		String str = bt.getYear() + "." + bt.getJDay() + "." + bt.getHour() + "." + bt.getMin() + "." + bt.getSec();
		
		System.out.println(str + ", " + bt.toString());
	}
	
	
	

}
