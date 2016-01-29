package app.kit.service.miniseed;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import app.kit.com.conf.MangoConf;
import app.kit.com.util.Helpers;
import app.kit.monitor.BckupWorker;
import app.kit.service.miniseed.WriteMiniSeed;
import app.kit.vo.FileContentVo;
import edu.sc.seis.seisFile.mseed.Btime;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class WriteMiniSeedTest {

	private WriteMiniSeed wms;
	private static ApplicationContext context;
	
    @Autowired
    public void init(ApplicationContext context) {
        WriteMiniSeedTest.context = context;
    }
	
	@Test
	public void write() throws ParseException {
		
		String network = "AK";
		String station = "ANM";
		String location = "";
		String channel = "BHE";
		String st = "2016-01-22T00:00:00.0000";
		String et = "2016-01-22T00:03:00.0000";
		String rootDir = "d:/temp";
		Btime stBtime = Helpers.getBtime(st, null);
		Btime etBtime = Helpers.getBtime(et, null);
		
		FileContentVo vo = new FileContentVo(network, station, location, channel, rootDir, stBtime, etBtime);

		wms = context.getBean(WriteMiniSeed.class, vo);
		wms.run();
	}

}
