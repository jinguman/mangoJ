

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import app.kit.service.seedlink.GenerateMiniSeed;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedRecord;

public class ReadTrace {

	public static void main(String[] args) {

		DataInputStream ls = null;
		DataOutputStream dos = null;
		
		//String filename = "d:/KS.DAG2..HHN.2016.063.00.00.00";
		String filename = "d:/KS.ADO2..HGZ.2016.061.09.10.00";
		String filename2 = "d:/KS.DAG2..HHN.2016.063.00.00.00.ccc";

		String stStr = "2016-03-03T00:00:46.0000";
		String etStr = "2016-03-03T00:00:53.0000";
		
		GenerateMiniSeed gm = new GenerateMiniSeed();
		
		try {
			dos = new DataOutputStream(new FileOutputStream(filename2));
			DataInput di = new DataInputStream(new FileInputStream(filename));
			
			while(true) {
				DataRecord dr = (DataRecord) SeedRecord.read(di);
				//DataRecord record = gm.trimPacket(stStr, etStr, dr, false);
				System.out.println(dr.getHeader().getStartTime() + " ~ " + dr.getHeader().getEndTime() + ", " + dr.getHeader().getNumSamples());
				//System.out.println(dr.toString());
				//System.out.println(dr.getHeader().getSequenceNum());
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
