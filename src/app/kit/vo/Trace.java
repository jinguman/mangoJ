package app.kit.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.bson.Document;
import org.bson.types.Binary;

import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude={"bytes","stBtime","etBtime","stCalc","twoZero","fourZero"})
public class Trace implements Serializable {
	
	private static final long serialVersionUID = 3648234490208084606L;
	private String network = "";
	private String station = "";
	private String location = "";
	private String channel = "";
	private float sampleRate = 0;
	private int numSamples = 0;
	private byte[] bytes;
	private String stStr = "";
	private String etStr = "";
	private int seqnum = 0;
	private transient Btime stBtime;
	private transient Btime etBtime;
	private Calendar stCalc;
	
	DecimalFormat twoZero = new DecimalFormat("00");
    DecimalFormat fourZero = new DecimalFormat("0000");

    public Trace(String network, String station, String location, String channel, Btime btime) {
    	this.network = network;
    	this.station = station;
    	this.location = location;
    	this.channel = channel;
    	this.stBtime = new Btime(btime.getAsBytes());
    	this.etBtime = new Btime(btime.getAsBytes());
    	
    	stStr = getBtimeToStringYMDHMS(stBtime);
    	etStr = getBtimeToStringYMDHMS(etBtime);
    	stCalc = stBtime.convertToCalendar();
    }
    
	public Trace(DataRecord dr) {
		
		network = dr.getHeader().getNetworkCode().trim();
		station = dr.getHeader().getStationIdentifier().trim();
		location = dr.getHeader().getLocationIdentifier().trim();
		channel = dr.getHeader().getChannelIdentifier().trim();
		sampleRate = dr.getHeader().getSampleRate();
		numSamples = dr.getHeader().getNumSamples();
		bytes = dr.toByteArray();
		
		seqnum = dr.getHeader().getSequenceNum();
		stBtime = dr.getHeader().getStartBtime();
		etBtime = getEndBtime();
		
		stStr = getBtimeToStringYMDHMS(stBtime);
		etStr = getBtimeToStringYMDHMS(etBtime);
		
		stCalc = stBtime.convertToCalendar();
	}
	
	public Document toDocument() {
		
        return new Document()
        		.append("st", stStr)
				.append("n", numSamples)
				.append("s", sampleRate)
				.append("et", etStr)
				.append("d", new Binary(bytes));
	}
	
	public void clear() {
		bytes = null;
	}
	
	public String getStartYear() {
		return new String(fourZero.format(stBtime.year));
	}
	
	public String getStartMonth() {
		int month = stCalc.get(Calendar.MONTH) + 1;
		return twoZero.format(month);
	}
	
	public String getStartYYYYMMDDHHMMSS() {
		
		int month = stCalc.get(Calendar.MONTH) + 1;
		int day = stCalc.get(Calendar.DAY_OF_MONTH);
		
		return new String(fourZero.format(stBtime.year) + "-"
                + twoZero.format(month) + "-"
                + twoZero.format(day)
                + "T"
                + twoZero.format(stBtime.hour) + ":"
                + twoZero.format(stBtime.min));
	}
	
	public static String getBtimeToStringYMD(Btime btime) {
		
		Calendar ca = btime.convertToCalendar();
		int month = ca.get(Calendar.MONTH) + 1;
		int day = ca.get(Calendar.DAY_OF_MONTH);
		
		DecimalFormat twoZero = new DecimalFormat("00");
	    DecimalFormat fourZero = new DecimalFormat("0000");
		
		return new String(fourZero.format(btime.year) + "-"
                + twoZero.format(month) + "-"
                + twoZero.format(day));
	}
	
	//yyyy-MM-dd'T'HH:mm:SS.SSSS
	public static String getBtimeToStringYMDHMS(Btime btime) {
		
		Calendar ca = btime.convertToCalendar();
		int month = ca.get(Calendar.MONTH) + 1;
		int day = ca.get(Calendar.DAY_OF_MONTH);
        
		DecimalFormat twoZero = new DecimalFormat("00");
	    DecimalFormat fourZero = new DecimalFormat("0000");
		
        // return string in standard jday format
        return new String(fourZero.format(btime.year) + "-"
                + twoZero.format(month) + "-"
                + twoZero.format(day)
                + "T"
                + twoZero.format(btime.hour) + ":"
                + twoZero.format(btime.min) + ":"
                + twoZero.format(btime.sec) + "."
                + fourZero.format(btime.tenthMilli));
	}
	
	public static String getBtimeToStringH(Btime btime) {
		DecimalFormat twoZero = new DecimalFormat("00");
		return new String(twoZero.format(btime.hour));
	}
	
	public static String getBtimeToStringM(Btime btime) {
		DecimalFormat twoZero = new DecimalFormat("00");
		return new String(twoZero.format(btime.min));
	}
	
	private Btime getEndBtime() {

		Btime btime = new Btime(stBtime.getAsBytes());
        double numTenThousandths = (((double)getNumSamples() / getSampleRate()) * 10000.0);
        return projectTime(btime, numTenThousandths);
    }
	
	private Btime projectTime(Btime bTime, double tenThousandths) {
        int offset = 0; // leap year offset
        // check to see if this is a leap year we are starting on
        boolean is_leap = bTime.year % 4 == 0 && bTime.year % 100 != 0
                || bTime.year % 400 == 0;
        if(is_leap)
            offset = 1;
        // convert bTime to tenths of seconds in the current year, then
        // add that value to the incremental time value tenThousandths
        tenThousandths += ttConvert(bTime);
        // now increment year if it crosses the year boundary
        if((tenThousandths) >= (366 + offset) * 864000000.0) {
            bTime.year++;
            tenThousandths -= (365 + offset) * 864000000.0;
        }
        // increment day
        bTime.jday = (int)(tenThousandths / 864000000.0);
        tenThousandths -= (double)bTime.jday * 864000000.0;
        // increment hour
        bTime.hour = (int)(tenThousandths / 36000000.0);
        tenThousandths -= (double)bTime.hour * 36000000.0;
        // increment minutes
        bTime.min = (int)(tenThousandths / 600000.0);
        tenThousandths -= (double)bTime.min * 600000.0;
        // increment seconds
        bTime.sec = (int)(tenThousandths / 10000.0);
        tenThousandths -= (double)bTime.sec * 10000.0;
        // set tenth seconds
        bTime.tenthMilli = (int)tenThousandths;
        // return the resultant value
        return bTime;
    }

	private double ttConvert(Btime bTime) {
        double tenThousandths = bTime.jday * 864000000.0;
        tenThousandths += bTime.hour * 36000000.0;
        tenThousandths += bTime.min * 600000.0;
        tenThousandths += bTime.sec * 10000.0;
        tenThousandths += bTime.tenthMilli;
        return tenThousandths;
    }

	private void writeObject(final ObjectOutputStream out) throws IOException {
		
		out.write(stBtime.getAsBytes());
		out.write(etBtime.getAsBytes());
		out.writeUTF(network);
		out.writeUTF(station);
		out.writeUTF(location);
		out.writeUTF(channel);
		out.writeFloat(sampleRate);
		out.writeInt(numSamples);
		out.writeInt(seqnum);
		if ( bytes != null ) {
			out.writeInt(bytes.length);
			if ( bytes.length > 0 ) out.write(bytes);
		} else {
			out.writeInt(0);
		}
	}

	private void readObject(final ObjectInputStream in) throws IOException {

		byte[] buf = new byte[10];
		in.read(buf, 0, 10);
		this.stBtime = new Btime(buf);
		in.read(buf, 0, 10);
		this.etBtime = new Btime(buf);
		
		network = in.readUTF();
		station = in.readUTF();
		location = in.readUTF();
		channel = in.readUTF();
		sampleRate = in.readFloat();
		numSamples = in.readInt();
		seqnum = in.readInt();
		int byteLen = in.readInt();
		if ( byteLen > 0 ) {
			bytes = new byte[byteLen];
			in.read(bytes);
		}

		this.twoZero = new DecimalFormat("00");
	    this.fourZero = new DecimalFormat("0000");		
		stCalc = stBtime.convertToCalendar();
		stStr = getBtimeToStringYMDHMS(stBtime);
		etStr = getBtimeToStringYMDHMS(etBtime);
	}
}
