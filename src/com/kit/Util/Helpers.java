package com.kit.Util;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

public class Helpers {

	public static void printJson(Document document) {
		JsonWriter jsonWriter = new JsonWriter(new StringWriter(), new JsonWriterSettings(JsonMode.SHELL, true));

		new DocumentCodec().encode(jsonWriter, document,
				EncoderContext.builder().isEncodingCollectibleDocument(true).build());
		System.out.println(jsonWriter.getWriter());
		System.out.flush();
	}
	
	public static String getCurrentUTC(SimpleDateFormat sdf) {
		
		//TimeZone tz = TimeZone.getTimeZone("Greenwich");
		//sdf.setTimeZone(tz);
		
		Date date = new Date();
		date.setTime(date.getTime() - (long)(60*60*9*1000));
		
		return sdf.format(date);
	}
	
	/**
	 * Convert to Standard format(yyyy-MM-dd'T'HH:mm:SS.SSSS) date from custom format
	 * @param date
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static String convertDatePerfectly(String strDate, SimpleDateFormat fromFormat, SimpleDateFormat toFormat) throws ParseException {
		
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		String rtn = toFormat.format(ca.getTime());
		int beginIndex = strDate.lastIndexOf(".");
		if ( beginIndex > 0 ) rtn += strDate.substring(beginIndex, strDate.length());
		
		return rtn;
	}

	public static String convertDate(String strDate, SimpleDateFormat fromFormat, SimpleDateFormat toFormat) throws ParseException {
		
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		String rtn = toFormat.format(ca.getTime());
		
		return rtn;
	}
	
	public static String getYearString(String strDate, SimpleDateFormat fromFormat) throws ParseException {

		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int year = ca.get(Calendar.YEAR);

		return String.format("%04d", year);
	}
	
	public static String getJdateString(String strDate, SimpleDateFormat fromFormat) throws ParseException {
		
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int day = ca.get(Calendar.DAY_OF_YEAR);

		return String.format("%03d", day);
	}

	public static String getMonthString(String strDate, SimpleDateFormat fromFormat) throws ParseException {

		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int month = ca.get(Calendar.MONTH) + 1;

		return String.format("%02d", month);
	}

	public static String getTraceCollectionName(String network, String station, String location, String year, String month) {

		StringBuffer sb = new StringBuffer();
		if ( !network.isEmpty() ) sb.append(network);
		sb.append("_");
		if ( !station.isEmpty() ) sb.append(station);
		sb.append("_");
		if ( !location.isEmpty() ) sb.append(location);

		if ( !year.isEmpty() || !month.isEmpty() ) sb.append("_");		
		if ( !year.isEmpty() ) sb.append(year);
		//if ( !month.isEmpty() ) sb.append(month);

		return sb.toString(); 
	}
		
}
