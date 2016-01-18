package app.kit.service.mongo;

import java.util.List;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class TraceStatsDaoTest {
	
	@Autowired private TraceStatsDao dao;
	
	@Test
	public void find() {
	
		String network = "AK";
		String station = "*";
		String location = "*";
		String channel = "*";
		String st = "2014-12-15T00:00:00.0000";
		String et = "2016-12-15T00:00:00.0000";
		
		List<Document> docs = dao.findTraceStats(network, station, location, channel, st, et);
		//List<Document> docs = dao.findTraceStats(new Document());
		
		
		for (Document doc : docs) {
			System.out.println(doc.toJson());
		}
	}

}
