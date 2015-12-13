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
import org.bson.types.Binary;

import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;

public class Helpers {

	public static void printJson(Document document) {
		JsonWriter jsonWriter = new JsonWriter(new StringWriter(), new JsonWriterSettings(JsonMode.SHELL, true));

		new DocumentCodec().encode(jsonWriter, document,
				EncoderContext.builder().isEncodingCollectibleDocument(true).build());
		System.out.println(jsonWriter.getWriter());
		System.out.flush();
	}
	
	public static String getCurrentUTC(SimpleDateFormat sdf) {

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
		
		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		String rtn = toFormat.format(ca.getTime());
		int beginIndex = strDate.lastIndexOf(".");
		if ( beginIndex > 0 ) rtn += strDate.substring(beginIndex, strDate.length());
		
		return rtn;
	}

	public static String convertDate(String strDate, SimpleDateFormat fromFormat, SimpleDateFormat toFormat) throws ParseException {
		
		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		String rtn = toFormat.format(ca.getTime());
		
		return rtn;
	}
	
	public static String convertDateBefore1Min(String strDate, SimpleDateFormat fromFormat, SimpleDateFormat toFormat) throws ParseException {
		
		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		ca.add(Calendar.MINUTE, -1);
		String rtn = toFormat.format(ca.getTime());
		
		return rtn;
	}
	
	public static String getYearString(String strDate, SimpleDateFormat fromFormat) throws ParseException {

		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int year = ca.get(Calendar.YEAR);

		return String.format("%04d", year);
	}
	
	public static String getJdateString(String strDate, SimpleDateFormat fromFormat) throws ParseException {
		
		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int day = ca.get(Calendar.DAY_OF_YEAR);

		return String.format("%03d", day);
	}

	public static String getMonthString(String strDate, SimpleDateFormat fromFormat) throws ParseException {

		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int month = ca.get(Calendar.MONTH) + 1;

		return String.format("%02d", month);
	}

	public static String getHourString(String strDate, SimpleDateFormat fromFormat) throws ParseException {

		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int hour = ca.get(Calendar.HOUR);

		return String.format("%02d", hour);
	}

	public static String getMinuteString(String strDate, SimpleDateFormat fromFormat) throws ParseException {

		// Don't care TIMEZONE
		Calendar ca = Calendar.getInstance();
		ca.setTime(fromFormat.parse(strDate));
		
		int min = ca.get(Calendar.HOUR);

		return String.format("%02d", min);
	}
	
	public static String getTraceCollectionName(String network, String station, String location, String channel, String year, String month) {

		StringBuffer sb = new StringBuffer();
		if ( !network.isEmpty() ) sb.append(network);
		//sb.append("_");
		//if ( !station.isEmpty() ) sb.append(station);
		//sb.append("_");
		//if ( !location.isEmpty() ) sb.append(location);
		if ( !channel.isEmpty() ) {
			sb.append("_");
			sb.append(channel.charAt(0));
		}
		
		if ( !year.isEmpty() || !month.isEmpty() ) sb.append("_");		
		if ( !year.isEmpty() ) sb.append(year);
		//if ( !month.isEmpty() ) sb.append(month);

		return sb.toString(); 
	}
	
	public static String getTraceGapsKey(String network, String station, String location, String channel, String st) {
		StringBuffer sb = new StringBuffer();
		sb.append(network).append("_").append(station).append("_").append(location).append("_").append(channel)
			.append("_").append(st);
		
		return sb.toString();
	}

	public static Btime getBtime(String strDate, SimpleDateFormat format) throws ParseException {
		
		Calendar ca = Calendar.getInstance();
		ca.setTime(format.parse(strDate));
		
		int year = ca.get(Calendar.YEAR);
		int jday = ca.get(Calendar.DAY_OF_YEAR);
		int hour = ca.get(Calendar.HOUR_OF_DAY);
		int min = ca.get(Calendar.MINUTE);
		int sec = ca.get(Calendar.SECOND);
		
		int tenthMilli = 0;
		int beginIndex = strDate.lastIndexOf(".");
		if ( beginIndex > 0 ) {
			double d = Double.parseDouble(strDate.substring(beginIndex, strDate.length()));
			tenthMilli = (int)(Math.round(d * 10000) % 10000);
		}
		
		Btime bt = new Btime(year, jday, hour, min, sec, tenthMilli);
		return bt;
	}
	
	public static double getEpochTime(Btime bt) {
		
		Calendar ca = bt.convertToCalendar();
		long l = ca.getTimeInMillis();
		l = l / 1000; // remove precision
		
		// recover precision
		double d = l;
		int tenthMilli = bt.getTenthMilli();
		d += (double)tenthMilli / 10000.0;
		
		return d;
	}
	
	public static Btime getBtimeAddSamples(Btime bt, float sampleRate, int samples) {
		
		double d = getEpochTime(bt);
		d += samples * ( 1.0 / sampleRate);
		Btime b = new Btime(d);

		return b;
	}
	
	public static Document dRecordToDoc(DataRecord dr, String stStr, String etStr) {
		
		String networkCode = dr.getHeader().getNetworkCode().trim();
        String stationIdentifier = dr.getHeader().getStationIdentifier().trim();
        String channelIdentifier = dr.getHeader().getChannelIdentifier().trim();
        String locationIdentifier = dr.getHeader().getLocationIdentifier().trim();

        float sampleRate = dr.getHeader().getSampleRate();
        int numSamples = dr.getHeader().getNumSamples();

        Binary data = new Binary(dr.toByteArray());

        Document d = new Document()
        		.append("st", stStr)
				.append("n", numSamples)
				.append("s", sampleRate)
				.append("et", etStr)
				.append("d", data)
				.append("network", networkCode)
				.append("station", stationIdentifier)
				.append("location", locationIdentifier)
				.append("channel", channelIdentifier);
		
		return d;
	}
	
	public static long getDiffByMinute(String stStr, String etStr, SimpleDateFormat format) throws ParseException {
		
		Calendar ca1 = Calendar.getInstance();
		ca1.setTime(format.parse(stStr));
		
		Calendar ca2 = Calendar.getInstance();
		ca2.setTime(format.parse(etStr));
		
		return getDiffByMinute(ca1, ca2); 
	}

	public static long getDiffByMinute(Btime st, Btime et) throws ParseException {
		
		Calendar ca1 = st.convertToCalendar();
		Calendar ca2 = et.convertToCalendar();
		
		return getDiffByMinute(ca2, ca1);
	}
	
	public static long getDiffByMinute(Calendar caSt, Calendar caEt) {
		
		caSt.set(Calendar.SECOND, 0);
		caSt.set(Calendar.MILLISECOND, 0);
		
		caEt.set(Calendar.SECOND, 0);
		caEt.set(Calendar.MILLISECOND, 0);
		
		long diffSec = (caEt.getTimeInMillis() - caSt.getTimeInMillis()) / 1000;
		
		return diffSec/60;
	}

	public static Btime getNextSharpMinute(Calendar ca, int amount) {
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		ca.add(Calendar.MINUTE, amount);
		ca.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		return new Btime(ca.getTime());
	}
	
	public static Btime getNextSharpMinute(Btime bt, int amount) {

		return getNextSharpMinute(bt.convertToCalendar(), amount);
	}
	
	public static Btime getNextSharpMinute(String str, int amount, SimpleDateFormat format) throws ParseException {
		
		Calendar ca = Calendar.getInstance();
		ca.setTime(format.parse(str));
		ca.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		return getNextSharpMinute(ca, amount);
	}
}
