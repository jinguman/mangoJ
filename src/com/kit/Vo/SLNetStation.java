package com.kit.Vo;

import edu.sc.seis.seisFile.mseed.Btime;
import lombok.Setter;

public class SLNetStation {

    public String network = null;
    public String station = null;
    @Setter public int seqnum = -1;
    @Setter public String stime = null;

    public SLNetStation(String network, String station, int seqnum, String stime) {

        this.network = network;
        this.station = station;
        this.seqnum = seqnum;
        if (stime != null) {
            this.stime = stime;
        }
    }

    public static String getSLTimeStamp(Btime btime) {

        StringBuffer sb = new StringBuffer();
        sb.append(btime.getYear());
        sb.append(',').append(btime.getJDay());
        sb.append(',').append(btime.getHour());
        sb.append(',').append(btime.getMin());
        sb.append(',').append(btime.getSec());

        return (sb.toString());
    }
    
    public String toString() {
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append(network);
    	sb.append(" ");
    	sb.append(station);
    	sb.append(" ");
    	sb.append(seqnum);
    	sb.append(" ");
    	sb.append(stime);
    	
    	return sb.toString();
    }
}