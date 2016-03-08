package app.kit.com.util;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import org.bson.types.Binary;

import app.kit.vo.StimeState;
import app.kit.vo.Trace;
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
	
	public static String getCurrentUTCString() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = new Date();
		date.setTime(date.getTime() - (long)(60*60*9*1000));
		
		return format.format(date);
	}
	
	public static Btime getCurrentUTCBtime() {
		return new Btime(new Date());
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
		if ( !channel.isEmpty() ) sb.append("_").append(channel.charAt(0));
		if ( !year.isEmpty() ) sb.append("_").append(year);		

		return sb.toString(); 
	}
	
	/**
	 * 
	 * @param network network identifier
	 * @param station station identifier
	 * @param location location identifier
	 * @param channel channel identifier
	 * @param st yyyy-MM-dd
	 * @return TRACE_GAPS collections's key
	 */
	public static String getTraceGapsKey(String network, String station, String location, String channel, String st) {
		StringBuffer sb = new StringBuffer();
		sb.append(network).append("_").append(station).append("_").append(location).append("_").append(channel)
			.append("_").append(st);
		
		return sb.toString();
	}

	public static String getTraceGapsKey(String network, String station, String location, String channel, Btime bt) {
		StringBuffer sb = new StringBuffer();
		sb.append(network).append("_").append(station).append("_").append(location).append("_").append(channel)
			.append("_").append(Trace.getBtimeToStringYMD(bt));
		return sb.toString();
	}
	
	public static Btime getBtime(String strDate, SimpleDateFormat format) throws ParseException {
		
		if ( format == null ) format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
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
	
	public static long getEpochTimeLong(Btime bt) {
		
		double d = getEpochTime(bt);
		
		return (long)d;
	}
	
	public static Btime getBtimeBeforeOneSample(Btime bt, float sampleRate) {
		
		double d = Helpers.getEpochTime(bt);
		d = d - (1/sampleRate);
		return new Btime(d);
	}
	
	public static Btime getBtimeAddSamples(Btime bt, float sampleRate, int samples) {
		
		double d = getEpochTime(bt);
		d += samples * ( 1.0 / sampleRate);
		Btime b = new Btime(d);

		return b;
	}
	
	public static long getDiffByMinute(String stStr, String etStr, SimpleDateFormat format) throws ParseException {
		
		Calendar ca1 = Calendar.getInstance();
		ca1.setTime(format.parse(stStr));
		
		Calendar ca2 = Calendar.getInstance();
		ca2.setTime(format.parse(etStr));
		
		return getDiffByMinute(ca1, ca2); 
	}

	public static long getDiffByMinute(Btime st, Btime et) {
		
		Calendar ca1 = st.convertToCalendar();
		Calendar ca2 = et.convertToCalendar();
		
		return getDiffByMinute(ca1, ca2);
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
	
	public static String getStFromCalendar(String filename, Calendar ca) {
		
		int year = ca.get(Calendar.YEAR);
		int month = ca.get(Calendar.MONTH) + 1;
		int day = ca.get(Calendar.DAY_OF_MONTH);
		int hour = ca.get(Calendar.HOUR_OF_DAY);
		int min = ca.get(Calendar.MINUTE);
		StringBuffer sb = new StringBuffer();
		
		if ( filename.length() >= 10) {
			sb.append(String.format("%04d", year));
			sb.append("-");
			sb.append(String.format("%02d", month));
			sb.append("-");
			sb.append(String.format("%02d", day));
		}
		
		if ( filename.length() >= 13 ) {
			sb.append("T");
			sb.append(String.format("%02d", hour));
		} else {
			sb.append("T");
			sb.append("00");
		}
		
		if ( filename.length() >= 16 ) {
			sb.append(":");
			sb.append(String.format("%02d", min));
			sb.append(":");
			sb.append("00");
			sb.append(".0000");
		} else {
			sb.append(":");
			sb.append("00");
			sb.append(":");
			sb.append("00");
			sb.append(".0000");
		}
		return sb.toString();
	}
	
	public static String getStFromFileName(String filename, SimpleDateFormat format) throws ParseException {
		
		// 2015-12-16 : 2015-12-16T00:00:00.0000 - 2015-12-17T00:00:00:0000
		// 2015-12-16T09 : 2015-12-16T09:00:00.0000 - 2015-12-16T10:00:00:0000
		// 2015-12-16T09:04 : 2015-12-16T09:04:00.0000 - 2015-12-16T09:05:00:0000

		Calendar ca = Calendar.getInstance();
		ca.setTime(format.parse(filename));
		
		return getStFromCalendar(filename, ca);
	}
	
	public static String getEtFromFileName(String filename, SimpleDateFormat format) throws ParseException {
		
		// 2015-12-16 : 2015-12-16T00:00:00.0000 - 2015-12-17T00:00:00:0000
		// 2015-12-16T09 : 2015-12-16T09:00:00.0000 - 2015-12-16T10:00:00:0000
		// 2015-12-16T09:04 : 2015-12-16T09:04:00.0000 - 2015-12-16T09:05:00:0000

		Calendar ca = Calendar.getInstance();
		ca.setTime(format.parse(filename));
		
		if ( filename.length() == 10) {
			ca.add(Calendar.DAY_OF_MONTH, 1);
			return getStFromCalendar(filename, ca);
		}
		
		if ( filename.length() == 13 ) {
			ca.add(Calendar.HOUR_OF_DAY, 1);
			return getStFromCalendar(filename, ca);
		}
		
		if ( filename.length() == 16 ) {
			ca.add(Calendar.MINUTE, 1);
			return getStFromCalendar(filename, ca);
		}
		
		return null;
	}
	
	public static String getFileName(String network, String station, String location, String channel, Btime bt) {
		
		StringBuffer sb = new StringBuffer();
		
		if ( !network.equals("-")) sb.append(network).append(".");
		if ( !station.equals("-")) sb.append(station).append(".");
		if ( !location.equals("-")) sb.append(location).append(".");
		if ( !channel.equals("-")) sb.append(channel).append(".");
		
		sb.append(String.format("%04d", bt.year)).append(".");
		sb.append(String.format("%03d", bt.jday)).append(".");
		sb.append(String.format("%02d", bt.hour)).append(".");
		sb.append(String.format("%02d", bt.min)).append(".");
		sb.append(String.format("%02d", bt.sec));
		
		return sb.toString();
	}

	public static String getStrYearBtime(Btime bt) {
		return String.format("%04d", bt.getYear());
	}
	public static String getStrJDayBtime(Btime bt) {
		return String.format("%03d", bt.getJDay());
	}
	public static String getStrJDayFirstCharBtime(Btime bt) {
		return String.format("%03d", bt.getJDay()).substring(0, 1);
	}
	public static String getStrHourBtime(Btime bt) {
		return String.format("%02d", bt.getHour());
	}
	public static String getStrMinBtime(Btime bt) {
		return String.format("%02d", bt.getMin());
	}
	
	public static Comparator<Document> traceCompare = new Comparator<Document>() {

		@Override
		public int compare(Document d1, Document d2) {
			String s1 = d1.getString("st");
			String s2 = d2.getString("st");
			return s1.compareTo(s2);
		}
	};
	
	public static List<StimeState> splitSttimePerYear(Btime stBtime, Btime etBtime) {
		List<StimeState> list = new ArrayList<>();
		
		if ( (etBtime.year - stBtime.year) == 0 ) {
			list.add(new StimeState(stBtime, etBtime));
		} else {
			
			int year = stBtime.year;
			Btime endTemp = new Btime(year, getLastJdate(year), 23, 59, 59, 9999);
			Btime startTemp = new Btime(year+1, 1, 0, 0, 0, 0);
			list.add(new StimeState(stBtime, endTemp));
			while((year+1)<etBtime.year) {
				
				++year;
				endTemp = new Btime(year, getLastJdate(year), 23, 59, 59, 9999);
				list.add(new StimeState(startTemp, endTemp));
				startTemp = new Btime(year+1, 1, 0, 0, 0, 0);
			}
			
			
			list.add(new StimeState(startTemp, etBtime));
		}
		
		return list;
	}
	
	public static int getLastJdate(int year) {
		int jdate = 365;
		if(0 ==(year % 4) && 0 !=(year % 100) || 0 == (year % 400)) {
			jdate++;
		}
		return jdate;
	}
}

