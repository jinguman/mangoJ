package app.kit.service.mongo;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.client.MongoCursor;

import app.kit.com.conf.MangoConf;
import app.kit.com.util.Helpers;
import app.kit.service.seedlink.GenerateMiniSeed;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class TraceDaoTest {

	@Autowired private TraceDao dao;
	@Autowired private GenerateMiniSeed gm;
	
	@Test
	public void test() throws SeedFormatException, IOException, ParseException {
	
		String network = "AK";
		String station = "BCP";
		String location = "";
		String channel = "BHE";

		String st = "2016-01-16T13:00:00.0000";
		String et = "2016-01-16T13:59:59.9999";
		
		int totSamples = 0;

		long startTime = System.currentTimeMillis();
		
		MongoCursor<Document> cursor = dao.getTraceCursor(network, station, location, channel, st, et);
		
		String filename = "d:/temp/" + Helpers.getFileName(network, station, location, channel, Helpers.getBtime(st, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")));
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
		while(cursor.hasNext()) {

			Document doc = cursor.next();
			Object o = doc.get(channel);
			
			if (o instanceof Document ) {

				Document sub = (Document) o;
				Binary binary = (Binary) sub.get("d");
				ByteBuf b = Unpooled.wrappedBuffer(binary.getData());
				DataRecord dr = (DataRecord)SeedRecord.read(b.array());

				if ( dr != null ) {
					//System.out.println(dr.getHeader().getStartTime() + "-" + dr.getHeader().getEndTime() + ", " + dr.getHeader().getNumSamples() + ", sample: " + dr.getHeader().getSampleRate());
					//totSamples += dr.getHeader().getNumSamples();
					//dr.write(dos);
					
					DataRecord dr2 = gm.trimPacket(st, et, dr, false);
					if ( dr2 != null ) {
						//System.out.println(dr2.getHeader().getStartTime() + "-" + dr2.getHeader().getEndTime() + ", " + dr2.getHeader().getNumSamples() + ", sample: " + dr2.getHeader().getSampleRate());
						totSamples += dr2.getHeader().getNumSamples();
						dr2.write(dos);
					}
					
					
				}
				
			} else if ( o instanceof ArrayList<?>) {
				
				List<Document> subs =  (List<Document>) o;
				Collections.sort(subs, compare);
				for(Document sub : subs) {
					Binary binary = (Binary) sub.get("d");
					ByteBuf b = Unpooled.wrappedBuffer(binary.getData());
					DataRecord dr = (DataRecord)SeedRecord.read(b.array());

					if ( dr != null ) {
						//System.out.println(dr.getHeader().getStartTime() + "-" + dr.getHeader().getEndTime() + ", " + dr.getHeader().getNumSamples() + ", sample: " + dr.getHeader().getSampleRate());
						//totSamples += dr.getHeader().getNumSamples();
						//dr.write(dos);
						
						DataRecord dr2 = gm.trimPacket(st, et, dr, false);
						if ( dr2 != null ) {
							System.out.println(dr2.getHeader().getStartTime() + "-" + dr2.getHeader().getEndTime() + ", " + dr2.getHeader().getNumSamples() + ", sample: " + dr2.getHeader().getSampleRate());
							totSamples += dr2.getHeader().getNumSamples();
							dr2.write(dos);
						}
					}
				}
			}
		}
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("took " + estimatedTime + " ms at nsamp: " + totSamples);
		dos.close();
	}
	
	public static Comparator<Document> compare = new Comparator<Document>() {

		@Override
		public int compare(Document d1, Document d2) {
			String s1 = d1.getString("st");
			String s2 = d2.getString("st");
			return s1.compareTo(s2);
		}
		
	};
}
