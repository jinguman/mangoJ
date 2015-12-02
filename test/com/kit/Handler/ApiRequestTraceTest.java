package com.kit.Handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.kit.Util.Helpers;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;

public class ApiRequestTraceTest {

/*
from obspy import read
st = read('d:/test.original.mseed')
st += read('d:/test.split.mseed')
print st[0].stats
st.plot(automerge=False)
*/
	
	
	@Test
	public void splitSlPacket() {
		
		// make fake sl packet
		// 1:31:44.6484 ~ 1:31:56.2684, sampleRate: 50, nSamples: 581
		String outFilename = "d:/test.original.mseed";
		String outFilename2 = "d:/test.split.mseed";
        int seq = 1;
        byte seed4096 = (byte)12;
        
        int[] data = new int[581];
        // make some fake data, use sqrt so more data will be "small"
        for (int i = 0; i < data.length; i++) {
            data[i] = (int)(Math.round(Math.sqrt(Math.random())*2000)) * (Math.random() > 0.5? 1 : -1);
        }

        try {
        
	        DataHeader header = new DataHeader(seq++, 'D', false);
	        header.setStationIdentifier("FAKE");
	        header.setChannelIdentifier("BHZ");
	        header.setNetworkCode("XX");
	        header.setLocationIdentifier("00");
	        header.setNumSamples((short) data.length);
	        header.setSampleRate(50f);
	        Btime btime = Helpers.getBtime("2015-01-01T01:31:44.6484", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	        header.setStartBtime(btime);
	
	        DataRecord record = new DataRecord(header);
	        Blockette1000 blockette1000 = new Blockette1000();
	        blockette1000.setEncodingFormat((byte)B1000Types.STEIM2);
	        blockette1000.setWordOrder((byte)1);
	        blockette1000.setDataRecordLength(seed4096);
	        record.addBlockette(blockette1000);
	        SteimFrameBlock steimData = null;
	
	        steimData = Steim2.encode(data, 63);        
	        record.setData(steimData.getEncodedData());
	
	        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename)));
	        record.write(out);
	        out.close();
	        System.out.println("Wrote miniseed to "+outFilename+", "+(data.length*4)+" compressed to "+steimData.numNonEmptyFrames()*64
	                           +" record size="+record.getRecordSize());

	        // confirm 
	        System.out.println("Origin: " + header.getStartTime() + " ~ " + header.getEndTime());

	        // split
	        ApiRequestTrace apt = new ApiRequestTrace(null, null);
	        apt.splitSlPacket("2015-01-01T01:31:47:00", "2015-01-01T01:31:52", record);	// 앞뒤 다 자르는 케이스
	        //apt.splitSlPacket("2015-01-01T01:31:47:00", "2015-01-01T01:31:57", record);	// 앞만 자르는 케이스
	        //apt.splitSlPacket("2015-01-01T01:31:40:00", "2015-01-01T01:31:52", record);	// 뒤만 자르는 케이스
	        
	        // confirm split...
	        System.out.println("Start time: " + record.getHeader().getStartTime());
	        System.out.println("End time: " + record.getHeader().getEndTime());
	        
	        // file write
	        DataOutputStream out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename2)));
	        record.write(out2);
	        out2.close();
	        System.out.println("Wrote miniseed to "+outFilename2+", "+(data.length*4)+" compressed to "+steimData.numNonEmptyFrames()*64
	                           +" record size="+record.getRecordSize());
                           
        } catch (Exception e ) {
        	System.out.println(e);
        }
	                           
	}
	
	//@Test
	public void write() {
        String outFilename = "d:/test.mseed";
        int seq = 1;
        byte seed4096 = (byte)12;
        byte seed512 = (byte)9;
        
        int[] data = new int[512];
        // make some fake data, use sqrt so more data will be "small"
        for (int i = 0; i < data.length; i++) {
            data[i] = (int)(Math.round(Math.sqrt(Math.random())*2000)) * (Math.random() > 0.5? 1 : -1);
        }

        System.out.println(data[0] + ", " + data[1]);
        
        try {
        	DataHeader header = new DataHeader(seq++, 'D', false);
            header.setStationIdentifier("FAKE");
            header.setChannelIdentifier("BHZ");
            header.setNetworkCode("XX");
            header.setLocationIdentifier("00");
            header.setNumSamples((short)data.length);
            header.setSampleRate(.05f);
            Btime btime = new Btime(new Date());
            header.setStartBtime(btime);
            
            DataRecord record = new DataRecord(header);
            Blockette1000 blockette1000 = new Blockette1000();
            blockette1000.setEncodingFormat((byte)B1000Types.STEIM2);
            blockette1000.setWordOrder((byte)1);
            blockette1000.setDataRecordLength(seed4096);
            record.addBlockette(blockette1000);
            SteimFrameBlock steimData = null;
            
            steimData = Steim2.encode(data, 63);
            
            record.setData(steimData.getEncodedData());
            
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename)));
            record.write(out);
            out.close();
            System.out.println("Wrote miniseed to "+outFilename+", "+(data.length*4)+" compressed to "+steimData.numNonEmptyFrames()*64
                               +" record size="+record.getRecordSize());
	        
            // decompress
            int[] de = Steim2.decode(record.getData(), data.length, false);
            
            System.out.println(de[0] + ", " + de[1]);
            
            // 
            DecompressedData decomData = record.decompress();
            int[] temp = decomData.getAsInt();
            
            
        } catch (Exception e) {
        	System.out.println(e);
        }
	}
}
