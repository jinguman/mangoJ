package com.kit.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import static org.junit.Assert.*;


public class GenerateMiniSeedTest {

	@Test
	public void splitPacketPerMinute() throws SeedFormatException, IOException, UnsupportedCompressionType, CodecException {

		// Generate DataRecord
		int seq = 1;
		byte seed4096 = (byte) 12;

		int[] data = new int[1024];
		// make some fake data, use sqrt so more data will be "small"
		for (int i = 0; i < data.length; i++) {
			data[i] = (int) (Math.round(Math.sqrt(Math.random()) * 2000)) * (Math.random() > 0.5 ? 1 : -1);
		}

		DataHeader header = new DataHeader(seq++, 'D', false);
		header.setStationIdentifier("FAKE");
		header.setChannelIdentifier("BHZ");
		header.setNetworkCode("XX");
		header.setLocationIdentifier("00");
		header.setNumSamples((short) data.length);
		header.setSampleRate(100f);
		//Btime btime = new Btime(new Date());
		Btime btime = new Btime(2015,344,4,23,58,1230);
		header.setStartBtime(btime);

		DataRecord record = new DataRecord(header);
		Blockette1000 blockette1000 = new Blockette1000();
		blockette1000.setEncodingFormat((byte) B1000Types.STEIM2);
		blockette1000.setWordOrder((byte) 1);
		blockette1000.setDataRecordLength(seed4096);
		record.addBlockette(blockette1000);
		SteimFrameBlock steimData = null;

		steimData = Steim2.encode(data, 63, data[0]);
		
		record.setData(steimData.getEncodedData());
		
		System.out.println("- ORIGINAL ---------------------------");
		System.out.println(record.toString());
		System.out.println("--------------------------------------");

		// test
		GenerateMiniSeed gm = new GenerateMiniSeed();
		List<DataRecord> records = gm.splitPacketPerMinute(record);
		
		System.out.println("[Data] " + record.getHeader().getStartTime() + " - " + record.getHeader().getEndTime());
		int sum = 0;
		for(DataRecord dr : records) {
			//System.out.println(dr.toString());
			DecompressedData deData = dr.decompress();
			System.out.println("[Split] " + dr.getHeader().getStartTime() + " - " + dr.getHeader().getEndTime() 
					+ ": nsamp: " + dr.getHeader().getNumSamples() + ", data: " + deData.getAsInt().length);
			sum += dr.getHeader().getNumSamples();
			
		} 
		
		assertEquals(data.length, sum);

	}

}

