package app.kit.com.queue;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.Btime;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class BlockingMessageQueueTest {

	@Autowired private BlockingMessageQueue queue;
	
	@Test
	public void SaveAndLoad() throws InterruptedException {
		Btime bt1 = new Btime(2015, 1, 1, 1, 1, 1);
		Trace trace1 = new Trace("net1", "sta1", "loc1", "cha1", bt1);
		
		Btime bt2 = new Btime(2015, 2, 2, 2, 2, 2);
		Trace trace2 = new Trace("net2", "sta2", "loc2", "cha2", bt2);
		
		List<Trace> traces1 = new ArrayList<>();
		traces1.add(trace1);
		traces1.add(trace2);
		
		queue.put(traces1);
		
		Btime bt3 = new Btime(2015, 3, 3, 3, 3, 3);
		Trace trace3 = new Trace("net3", "sta3", "loc3", "cha3", bt3);
		byte[] bytes = new byte[1];
		trace3.setBytes(bytes);
		List<Trace> traces2 = new ArrayList<>();
		traces2.add(trace3);
		queue.put(traces2);
		
		File file = new File("d:/temp.queue");
		queue.save(new File("d:/temp.queue"));
		queue.load(file);
		
		List<Trace> tracesLst = queue.take();
		assertEquals(trace1, tracesLst.get(0));
		assertEquals(trace2, tracesLst.get(1));
		
		tracesLst = queue.take();
		
		byte[] bytes3 = trace3.getBytes();
		assertEquals(bytes, bytes3);
		
		assertEquals(trace3, tracesLst.get(0));
	}

}
