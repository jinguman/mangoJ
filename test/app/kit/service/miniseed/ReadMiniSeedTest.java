package app.kit.service.miniseed;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;
import app.kit.service.seedlink.GenerateMiniSeed;
import app.kit.vo.Trace;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;

//@RunWith(SpringJUnit4ClassRunner.class) 
//@ContextConfiguration(classes={MangoConf.class})
public class ReadMiniSeedTest {

	ReadMiniSeed readMS;
	//private static ApplicationContext context;
	
	/*
    @Autowired
    public void init(ApplicationContext context) {
        ReadMiniSeedTest.context = context;
    }
    */
	
	@Test
	public void test() throws SeedFormatException, IOException, UnsupportedCompressionType, CodecException {
		
		File file = new File("C:/Users/jman/Downloads/test.mseed");
		//boolean isDbCheck = false;
		//readMS = context.getBean(ReadMiniSeed.class, file, isDbCheck);
		//readMS.run();
		
		GenerateMiniSeed gm = new GenerateMiniSeed();
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream("C:/Users/jman/Downloads/test.mseed.out"));
		DataInput input;
		input = new DataInputStream(new FileInputStream(file));	
		while(true) {
			DataRecord dr = (DataRecord) SeedRecord.read(input);
			
			// split
			List<DataRecord> records = gm.splitPacketPerMinute(dr);
			for(DataRecord record : records) {
				//System.out.println(record.getHeader().getSampleRate());
				//System.out.println(record.getHeader().getSampleRateFactor());
        		//traces.add(new Trace(record));
				record.write(dos);
				
				dr.getHeader().setSampleRateMultiplier((short) 1);
				dr.getHeader().setSampleRateFactor((short) 100);
				
				DecompressedData decomData = dr.decompress();
				int[] ints = decomData.getAsInt();
				//for(int i=0; i<ints.length; i++) {
					//System.out.println("?>>>" + ints[i]);
				//}
        	}
			
			//System.out.println(dr);
			//DecompressedData decomData = dr.decompress();
			//System.out.println("?>>>" + decomData);
		}
	}


}
