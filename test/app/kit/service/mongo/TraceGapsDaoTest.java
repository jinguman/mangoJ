package app.kit.service.mongo;

import java.util.List;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;
import edu.sc.seis.seisFile.mseed.Btime;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class TraceGapsDaoTest {

	@Autowired private TraceGapsDao dao;
	
	//@Test
	public void findCursor() {
		
		String network = "N4";
		String station = "T42B";
		String location = "";
		String channel = "BHN";
		String st = "2016-01-17";
		
		List<Document> docs = dao.getTraceGaps(network, station, location, channel, st);
		
		for(Document d : docs) {
			System.out.println(d);
		}
		
		Document m = (Document) docs.get(0).get("m");
		
		Document m00 = (Document) m.get("00");
		
		System.out.println(m00);
		
		int sum = 0;
		for(int i = 0; i<60; i++) {
			String key = String.format("%02d", i);
			int a = m00.getInteger(key);
			
			sum += a;
			System.out.println(a);
		}
		
		System.out.println(sum);
	}

	@Test
	public void findGapValue() {
		
		String network = "AK";
		String station = "ATKA";
		String location = "";
		String channel = "BHN";
		String st = "2016-01-22";
		Btime bt = new Btime(2016, 22, 0, 0, 0, 0);
		
		System.out.println(dao.getTraceGapsValueD(network, station, location, channel, bt));
		System.out.println(dao.getTraceGapsValueH(network, station, location, channel, bt));
		System.out.println(dao.getTraceGapsValueM(network, station, location, channel, bt));

	}
	
}
