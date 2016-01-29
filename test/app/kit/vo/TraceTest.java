package app.kit.vo;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import edu.sc.seis.seisFile.mseed.Btime;

public class TraceTest {

	@Test
	public void addBtime() {
		Btime bt = new Btime(1999, 365, 23, 59, 59, 9999);
		String str = bt.toString();
		Trace.getAddBtime(bt, Calendar.MINUTE, 1);
		Trace.getAddBtime(bt, Calendar.MINUTE, 1);
		Trace.getAddBtime(bt, Calendar.MINUTE, -1);
		Trace.getAddBtime(bt, Calendar.MINUTE, -1);
		String str2 = bt.toString();
		
		assertEquals(str, str2);
	}
}
