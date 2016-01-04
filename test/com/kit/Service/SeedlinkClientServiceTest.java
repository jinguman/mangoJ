package com.kit.Service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.kit.Util.PropertyManager;

import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;

public class SeedlinkClientServiceTest {

	private SeedlinkClientService scs;
	
	@Before
	public void setup() {
		
		PropertyManager pm = new PropertyManager();
		scs = new SeedlinkClientService(null, pm);
		scs.setHost(SeedlinkReader.DEFAULT_HOST);
		scs.setPort(SeedlinkReader.DEFAULT_PORT);
	}
	
	@Test
	public void getStationListFromStreamsInfo() throws UnknownHostException, SeedlinkException, SeedFormatException, IOException {
		
		String network = "4F";
		List<String> stations = scs.getStationListFromStreamsInfo(network);
		
		for(String station: stations) {
			System.out.println(station);
		}
		
		
	}
}
