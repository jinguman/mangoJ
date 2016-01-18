package app.kit.controller.seedlink;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import app.kit.com.conf.MangoConf;
import app.kit.service.seedlink.SeedlinkClientService;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class SeedlinkClient implements Runnable {

	final String EMPTY = SeedlinkReader.EMPTY;
	
	@Autowired private SeedlinkClientService service;
	@Autowired private MangoConf conf;

	@Setter @Getter private String networks;
	@Setter @Getter private String stations = "*";
	@Setter @Getter private String channel = "???";
	@Setter @Getter private String host;
	@Setter @Getter private int port;

	public SeedlinkClient() {}
	
	public SeedlinkClient(String networks, String stations, String channel, String host, int port) {
		this.networks = networks;
		this.stations = stations;
		this.channel = channel;
		this.host = host;
		this.port = port;
	}
	
	public void run() {
		
		try {
			while(true) {
				
				log.info("SeedlinkClient start. {}, {}:{}", networks, host, port);
				
				service.setNetworks(networks);
				service.setStations(stations);
				service.setChannel(channel);
				service.setHost(host);
				service.setPort(port);
				
				try {
					service.getTraceRaw();
				} catch (SeedlinkException | SeedFormatException | IOException e) {
					log.error("{} {}", "Failure in Seedlink. {}", networks, e);
				}
				
				log.info("SeedlinkClient restart after {} seconds. {}", conf.getScRestartSec() , networks);
				try {
					Thread.sleep(conf.getScRestartSec()*1000);
				} catch (InterruptedException e) {
					log.error("{} {}","Error during sleep.",e);
				}
			}
		} catch (InterruptedException e) {	
		} finally {
			// Thread종료시 수행 작성
			service.close();
		}
	}
}
