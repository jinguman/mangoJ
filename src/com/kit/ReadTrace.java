package com.kit;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.kit.Service.GenerateMiniSeed;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedRecord;

public class ReadTrace {

	public static void main(String[] args) {

		DataInputStream ls = null;
		DataOutputStream dos = null;
		
		String filename = "d:/SEO.HHZ.2015.1204.00.00.00";
		String filename2 = "d:/ANM.HGZ.2015.337.00.00.00.ccc";

		String stStr = "2015-12-03T14:00:10.0000";
		String etStr = "2015-12-03T14:10:10.5000";
		
		GenerateMiniSeed gm = new GenerateMiniSeed();
		
		try {
			dos = new DataOutputStream(new FileOutputStream(filename2));
			DataInput di = new DataInputStream(new FileInputStream(filename));
			
			while(true) {
				DataRecord dr = (DataRecord) SeedRecord.read(di);
				//DataRecord record = gm.trimPacket(stStr, etStr, dr, false);
				System.out.println(dr.toString());
				System.out.println(dr.getHeader().getSequenceNum());
				//if ( record != null ) record.write(dos);
			}
			
		} catch (EOFException e) {
			
			System.out.println("EOF, so done.");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			
			try {
				if (dos != null) dos.close();
				if (ls != null) ls.close();
			} catch (Exception ee) {}
		}
	}

}
