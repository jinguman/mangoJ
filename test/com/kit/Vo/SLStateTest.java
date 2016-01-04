package com.kit.Vo;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import edu.sc.seis.seisFile.mseed.Btime;

public class SLStateTest {

	@Test
	public void Stream() {
		
		SLState state = new SLState();
		
		String network = "NET";
		String station = "STA";
		int seqnum = 10;
		Btime btime = new Btime(2015, 365, 10, 12, 13, 1234);
		state.addStream(network, station, seqnum, btime);
		
		seqnum = 11;
		Btime btime2 = new Btime(2015, 365, 12, 12, 15, 1234);
		state.addStream(network, station, seqnum, btime2);
		
		List<String> lists = state.getAllStreamStr();
		for(String s : lists) {
			SLNetStation slNetStation = SLState.toParse(s);
			assertEquals(s, slNetStation.toString());
		}
	}
	
	@Test
	public void Shard() {
		
		SLState state = new SLState();
		
		String ns = "NET_STA";
		String collectionKey = "ck";
		
		state.addShard(ns, collectionKey);
		
		assertEquals(true, state.isShard(ns, collectionKey));
		assertEquals(false, state.isShard(ns + "t", collectionKey));
	}
}
