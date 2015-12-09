package com.kit.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Util.Helpers;

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

public class GenerateMiniSeed {

	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	final Logger logger = LoggerFactory.getLogger(GenerateMiniSeed.class);

	public DataRecord trimPacket(String stStr, String etStr, DataRecord dr, boolean isForceNew) {

		try {
			DataHeader header = dr.getHeader();

			// Get packet information
			Btime stPacketBtime = header.getStartBtime();
			Btime etPacketBtime = header.getLastSampleBtime();
			int sampleRate = Math.round(header.getSampleRate());

			// Get request Btime
			Btime stReqBtime = Helpers.getBtime(stStr, sdfToSecond);
			Btime etReqBtime = Helpers.getBtime(etStr, sdfToSecond);

			// Check range
			int result = checkRangePacket(stReqBtime, etReqBtime, stPacketBtime, etPacketBtime); 
			switch (result) {
				case 0:
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
				lTrimDelta = (int) (Math.floor((Helpers.getEpochTime(stPacketBtime) - Helpers.getEpochTime(stReqBtime))* sampleRate)) * -1;
			}
			
			// case2.       |stPacketBtime         |etPacketBtime
			//                                 |etReqBtime      
			// 패킷종료시간이 요청종료시간보다 뒤에 있고
			if ( etPacketBtime.after(etReqBtime) ) {
				rTrimDelta = (int) Math.floor(( Helpers.getEpochTime(etPacketBtime) - Helpers.getEpochTime(etReqBtime) ) * sampleRate); 
			}
			
			logger.debug("Calculate delta for cut. ltrim: {}, rtrim: {}", lTrimDelta, rTrimDelta);

			// cut 
			int[] temp2 = new int[temp.length - lTrimDelta - rTrimDelta]; 
			System.arraycopy(temp, lTrimDelta, temp2, 0, temp2.length);
			logger.debug("Data cut by request. before: {}, after: {}", temp.length, temp2.length);
			
			logger.debug("Original DataRecord. {}", dr.toString());
			
			// Get steim Frame size
			int steimFrameSize = getSteimFrameSize(dr.getDataSize());
			
			// Modify header
			header.setNumSamples((short)temp2.length);
	        header.setStartBtime(Helpers.getBtimeAddSamples(stPacketBtime, sampleRate, lTrimDelta));
			
	        // steim
	        SteimFrameBlock steimData = null;
	        steimData = Steim2.encode(temp2, steimFrameSize, temp2[0]);
	        
	        // Modify data record
	        if ( !isForceNew ) {
	        	if (steimData.getNumSamples() == temp2.length ) {
		        	dr.setData(steimData.getEncodedData());
		        	logger.debug("Modify DataRecord. {}", dr.toString());
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
	        logger.debug("Size: steimframe: {}, dataRecordSize: {}, seed: {}", steimFrameSize, dataRecordSize, seed);

	        // 1000 Blockette
	        Blockette1000 blockette1000 = new Blockette1000();
	        blockette1000.setEncodingFormat((byte)B1000Types.STEIM2);
	        blockette1000.setWordOrder((byte)1);
	        blockette1000.setDataRecordLength(seed);
	        record.addBlockette(blockette1000);
	        
	        //steimData = null;
	        //steimData = Steim2.encode(temp2, steimFrameSize, temp2[0]);
	        record.setData(steimData.getEncodedData());
			
	        logger.debug("New DataRecord. {}", record.toString());
	        
	        return record;
	        
		} catch (ParseException e) {
			logger.error("{}", e);
			return null;
		} catch (CodecException e) {
			logger.error("{}", e);
			return null;
		} catch (SeedFormatException e) {
			logger.error("{}", e);
			return null;
		} catch (IOException e) {
			logger.error("{}", e);
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
			logger.warn("Range invalid. start time must be before endtime. stReq: " + stReqBtime.toString() + ", etReq: " + etReqBtime.toString());
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
		
		// 
		
		return drLists;
	}
}
