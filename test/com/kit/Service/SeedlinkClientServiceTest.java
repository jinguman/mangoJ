package com.kit.Service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import app.kit.service.seedlink.SeedlinkClientService;
import app.kit.vo.InfoSeedlink;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;

public class SeedlinkClientServiceTest {

	private SeedlinkClientService service;
	
	@Before
	public void setup() {
		
		service = new SeedlinkClientService();
		service.setHost(SeedlinkReader.DEFAULT_HOST);
		service.setPort(SeedlinkReader.DEFAULT_PORT);
	}
	
	@Test
	public void getStationListFromStreamsInfo() throws UnknownHostException, SeedlinkException, SeedFormatException, IOException {
		
		String network = "4F";
		InfoSeedlink info = service.getInfoSeedlink();
		String[] stations = info.getStations(network);
		
		for(String station: stations) {
			System.out.println(station);
		}
		
		
	}
}
