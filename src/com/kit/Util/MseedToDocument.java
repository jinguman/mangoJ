package com.kit.Util;

import org.bson.Document;
import org.bson.types.Binary;

import edu.sc.seis.seisFile.mseed.DataRecord;

public class MseedToDocument {

	public static Document toDocument(DataRecord dr, String stStr, String etStr) {
		
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
}
