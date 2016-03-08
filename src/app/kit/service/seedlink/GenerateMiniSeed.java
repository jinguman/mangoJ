package app.kit.service.seedlink;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.kit.com.util.Helpers;
import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.sc.seis.seisFile.mseed.Blockette;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import lombok.extern.slf4j.Slf4j;

/**
 * Generate or Modify miniSEED packet
 * @author jman
 *
 */
@Service
@Slf4j
public class GenerateMiniSeed {

	 


	/**
	 * Trim the data packet
	 * @param stReqBtime The request time of start
	 * @param etReqBtime The request time of end
	 * @param dr Data record of miniSEED
	 * @param isForceNew {@code true} make a new Data record, {@code false} reuse input Data record
	 * @return  Data record
	 */
	public DataRecord trimPacket(Btime stReqBtime, Btime etReqBtime, DataRecord dr, boolean isForceNew) {
		
		try {
			DataHeader header = dr.getHeader();
			Btime stPacketBtime = header.getStartBtime();
			Btime etPacketBtime = header.getLastSampleBtime();
			float sampleRate = header.getSampleRate();

			// Check range
			int result = checkRangePacket(stReqBtime, etReqBtime, stPacketBtime, etPacketBtime); 
			switch (result) {
				case 0:
					//logger.debug("Data range invalid. req: {} - {}, packet: {} - {},  before: {}, after: {}", stReqBtime.toString(), etReqBtime.toString(), 
					//				stPacketBtime.toString(), etPacketBtime.toString());
					return null;
				case 1:
					return dr;
			}
 
			// get Raw Data
			DecompressedData decomData = dr.decompress();
            int[] temp = decomData.getAsInt();
            
            int lTrimDelta = 0;
			int rTrimDelta = 0;
            
			// case1.       |stPacketBtime         |etPacketBtime
			//                    |stReqBtime
			// 요청시작시간이 시작패킷시간보다 뒤에 있고
			if ( stReqBtime.after(stPacketBtime) ) {
				double tmp = (Helpers.getEpochTime(stReqBtime) - Helpers.getEpochTime(stPacketBtime)) * sampleRate;
				tmp = Math.round(tmp * 10000000)/10000000;
				lTrimDelta = (int)( Math.floor(tmp) ) +1;
			}
			
			// case2.       |stPacketBtime         |etPacketBtime
			//                                 |etReqBtime      
			// 패킷종료시간이 요청종료시간보다 뒤에 있고
			if ( etPacketBtime.after(etReqBtime) ) {
				double tmp = (Helpers.getEpochTime(etReqBtime) - Helpers.getEpochTime(stPacketBtime)) * sampleRate;
				tmp = Math.round(tmp * 10000000)/10000000;
				rTrimDelta = temp.length - (int)( Math.floor(tmp) ) -1;
			}
			
			//logger.debug("Calculate delta for cut. ltrim: {}, rtrim: {}", lTrimDelta, rTrimDelta);

			// cut 
			int[] temp2 = new int[temp.length - lTrimDelta - rTrimDelta]; 
			System.arraycopy(temp, lTrimDelta, temp2, 0, temp2.length);
			//logger.debug("Data cut by request. req: {} - {}, packet: {} - {},  before: {}, after: {}, alpha: {}", stReqBtime.toString(), etReqBtime.toString(), 
			//		stPacketBtime.toString(), etPacketBtime.toString(), temp.length, temp2.length);
			
			//logger.debug("Original DataRecord. {}", dr.toString());
			
			if ( temp2.length == 0 ) return null;
			
			// Get steim Frame size
			int steimFrameSize = getSteimFrameSize(dr.getDataSize());
			
	        // steim encoding
	        SteimFrameBlock steimData = null;
	        steimData = Steim2.encode(temp2, steimFrameSize, temp2[0]);
	        
	        // Modify data record
	        if ( !isForceNew ) {
	        	if (steimData.getNumSamples() == temp2.length ) {
	        		
	        		// Modify header
	    			header.setNumSamples((short)temp2.length);
	    	        header.setStartBtime(Helpers.getBtimeAddSamples(stPacketBtime, sampleRate, lTrimDelta));
	        		
		        	dr.setData(steimData.getEncodedData());
		        	//logger.debug("Modify DataRecord. {}", dr.toString());
			        return dr;
		        }
	        }
	        	        
			// Renew data record
			
			// Header
			int headerSize = 8;	// 1000 Blockette size
			DataHeader reheader = new DataHeader(header.getSequenceNum(), 'D', false);
			reheader.setStationIdentifier(dr.getHeader().getStationIdentifier());
			reheader.setChannelIdentifier(dr.getHeader().getChannelIdentifier());
			reheader.setNetworkCode(dr.getHeader().getNetworkCode());
			reheader.setLocationIdentifier(dr.getHeader().getLocationIdentifier());
			reheader.setNumSamples((short) temp2.length);
			reheader.setSampleRate(dr.getHeader().getSampleRate());
	        reheader.setStartBtime(Helpers.getBtimeAddSamples(stPacketBtime, sampleRate, lTrimDelta));
	        headerSize += 64;

	        // Record
	        DataRecord record = new DataRecord(reheader);
	        
	        // Blockette
	        Blockette[] blockettes = dr.getBlockettes();
	        for(Blockette b: blockettes) {
	        	if (b.getType() == 1000 ) continue;
	        	record.addBlockette(b);
	        	headerSize += b.getSize();
	        }
	        
	        // Find steimFrameSize
	        int cutRatio = (int) Math.floor((temp.length - temp2.length ) / temp.length); 
	        steimFrameSize = steimFrameSize * cutRatio;
	        while(true) {
	        	steimFrameSize++;
	        	
	        	steimData = null;
	 	        steimData = Steim2.encode(temp2, steimFrameSize, temp2[0]);
	 	        
	 	        if (steimData.getNumSamples() == temp2.length ) break;
	 	        
	        }

	        // Calculate seed len
	        int dataRecordSize = (steimFrameSize*64) + headerSize;
	        byte seed = (byte)get2Power(dataRecordSize);
	        //logger.debug("Size: steimframe: {}, dataRecordSize: {}, seed: {}", steimFrameSize, dataRecordSize, seed);

	        // 1000 Blockette
	        Blockette1000 blockette1000 = new Blockette1000();
	        blockette1000.setEncodingFormat((byte)B1000Types.STEIM2);
	        blockette1000.setWordOrder((byte)1);
	        blockette1000.setDataRecordLength(seed);
	        record.addBlockette(blockette1000);
	        
	        //steimData = null;
	        //steimData = Steim2.encode(temp2, steimFrameSize, temp2[0]);
	        record.setData(steimData.getEncodedData());
			
	        //logger.debug("New DataRecord. {}", record.toString());
	        
	        return record;
	        
		} catch (CodecException | SeedFormatException | IOException e) {
			log.error("{}", e);
			return null;
		} 
	}
	
	public DataRecord trimPacket(String stStr, String etStr, DataRecord dr, boolean isForceNew) {

		SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		try {

			// Get request Btime
			Btime stReqBtime = Helpers.getBtime(stStr, sdfToSecond);
			Btime etReqBtime = Helpers.getBtime(etStr, sdfToSecond);
			
			return trimPacket(stReqBtime, etReqBtime, dr, isForceNew);

		} catch (ParseException e) {
			log.error("{}", e);
			return null;
		} 
    }
	
	/**
	 * 
	 * @param stReqBtime
	 * @param etReqBtime
	 * @param stPacketBtime
	 * @param etPacketBtime
	 * @return 0(범위내에 존재하지 않음), 1(범위내에 존재함), 2(일부가 범위속에 존재함)
	 */
	public int checkRangePacket(Btime stReqBtime, Btime etReqBtime, Btime stPacketBtime, Btime etPacketBtime) {
		
		// 요청시작시간이 요청종료시간의 뒤에 있을 경우
		if ( stReqBtime.afterOrEquals(etReqBtime) ) {
			//logger.warn("Range invalid. start time must be before endtime. stReq: " + stReqBtime.toString() + ", etReq: " + etReqBtime.toString());
			return 0;
		}

		//                                       |stPacketBtime         |etPacketBtime
		//       |stReqBtime            |etReqBtime
		// 패킷시작시간이 요청종료시간의 뒤에 있을 경우
		if ( stPacketBtime.afterOrEquals(etReqBtime) ) {
			//logger.debug("Range invalid. request time within miniseed packet time. Req: " + stReqBtime.toString() + " ~ " + etReqBtime.toString() 
			//				+ ", packet: " + stPacketBtime.toString() + " ~ " + etPacketBtime.toString()
			//		);
			return 0;
		}
		// 요청시작시간이 패킷종료시간의 뒤에 있을 경우
		if ( stReqBtime.afterOrEquals(etPacketBtime)) {
			//logger.debug("Range invalid. request time within miniseed packet time. Req: " + stReqBtime.toString() + " ~ " + etReqBtime.toString() 
			//+ ", packet: " + stPacketBtime.toString() + " ~ " + etPacketBtime.toString()
			//		);
			return 0;
		}

		// check range. 시작패킷시간이 요청시작시간보다 뒤에 있고, 요청종료시간이 종료패킷시간보다 뒤에 있을 경우
		//              |stPacketBtime         |etPacketBtime
		//       |stReqBtime                           |etReqBtime
		if ( stPacketBtime.afterOrEquals(stReqBtime) && etReqBtime.afterOrEquals(etPacketBtime)) {
			return 1;
		}
		
		return 2;
	}
	
	public int getSteimFrameSize(int dataSize) {
		return dataSize/64;
	}
	
	public int get2Power(int value) {
		
		int power = 0;
		int powerValue = 0; 
		while(powerValue <= value) {
			power++;
			powerValue = (int) Math.pow(2, power);
		}
		return power;
	}
	
	public List<DataRecord> splitPacketPerMinute(DataRecord dr) {
		
		List<DataRecord> drLists = new ArrayList<DataRecord>();
		
		Btime st = dr.getHeader().getStartBtime();
		Btime et = dr.getHeader().getPredictedNextStartBtime();
		
		try {
			long l = Helpers.getDiffByMinute(st, et);
			
			if ( l > 0 ) {
				
				// start
				Btime tempBtime = Helpers.getNextSharpMinute(st, 1);
				//Btime tempBtimeBeforeMilliSec = Helpers.getBtimeBeforeMilliSecond(tempBtime);
				
				//System.out.println(tempBtime.toString() + ", " + tempBtimeBeforeMilliSec.toString());
				
				DataRecord trimDr = trimPacket(st, tempBtime, dr, true);
				if ( trimDr != null ) drLists.add(trimDr);
				
				// middle
				int n = 1;
				while( n < l ) {
					
					trimDr = trimPacket(Helpers.getNextSharpMinute(st, n), Helpers.getNextSharpMinute(st, n+1), dr, true);
					if ( trimDr != null ) drLists.add(trimDr);
					n++;
				}
					
				// end
				trimDr = trimPacket(Helpers.getNextSharpMinute(st, n), et, dr, true);
				if ( trimDr != null ) drLists.add(trimDr);
				
			} else {
				drLists.add(dr);
			}
			
		} catch (Exception e) {
			drLists.add(dr);
			log.warn("Error in parsing dateformat. Can't split datarecord per minute.");			
		}
		
		return drLists;
	}
}
