package com.kit.Vo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import app.kit.vo.SLNetStation;
import app.kit.vo.SLState;
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
	
	@Test
	public void StreamSaveAndRestore() throws IOException {
		
		File file = new File("d:/streams.txt");
		
		SLState state = new SLState();
		
		String network = "NET";
		String station = "STA";
		int seqnum = 10;
		Btime btime = new Btime(2015, 365, 10, 12, 13, 1234);
		
		state.addStream(network, station, seqnum, btime);
		
		network = "NET2";
		station = "STA2";
		state.addStream(network, station, seqnum, btime);
		
		state.saveStreams(file);
		
		state.addStream(network, station, 11, btime);
		state.saveStreams(file);
		
		SLState state2 = new SLState();
		state2.restoreStreams(file);
		
		List<String> lines = state2.getAllStreamStr();
		for(String s : lines) {
			System.out.println(s);
		}
		
	}
}
