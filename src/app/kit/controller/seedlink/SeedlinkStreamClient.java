package app.kit.controller.seedlink;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import app.kit.com.conf.MangoConf;
import app.kit.com.queue.BlockingMessageQueue;
import app.kit.service.seedlink.SeedlinkClientService;
import app.kit.vo.InfoSeedlink;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class SeedlinkStreamClient implements Runnable {
	
	@Autowired private BlockingMessageQueue queue;
	@Autowired private MangoConf conf;
	@Autowired private SeedlinkClientService service;
	
	@Setter @Getter private String networks;
	@Setter @Getter private String stations = "*";
	@Setter @Getter private String channel = "???";
	@Setter @Getter private String host;
	@Setter @Getter private int port;
	private String year;
	InfoSeedlink info;
	private String[] networkList;
	
	public SeedlinkStreamClient(String networks, String stations, String channel, String host, int port, String year) {
		this.networks = networks;
		this.stations = stations;
		this.channel = channel;
		this.host = host;
		this.port = port;
		this.year = year;
		
		this.networkList = networks.split(",");
	}
	
	public void run() {

		log.info("SeedlinkStreamClient start.. {}", networks );
		
		while(true) {
			try {

				service.setHost(host);
				service.setPort(port);
				
				info = service.getInfoSeedlink();
				break;
			} catch (SeedlinkException | SeedFormatException | IOException e) {
				log.warn("{}", e);
				log.info("SeedlinkStreamClient restart after {} seconds.", conf.getScRestartSec());
				try {
					Thread.sleep(conf.getScRestartSec()*1000);
				} catch (InterruptedException e1) {
					log.error("{}", e1);
					return;
				}
			}
		}
		
		if ( info != null ) {
			try { put();
			} catch (InterruptedException e) { log.error("{}", e);
			}
		}
	}
	
	private void put() throws InterruptedException {
		
		Btime btime = new Btime(Integer.parseInt(year), 1, 0, 0, 0, 0);
		
		if ( conf.isMiBuildEntireList() ) {
			queue.put(info.getTraces(btime));
		} else {
			
			for(String net : networkList) {
				queue.put(info.getTraces(net, btime));
			}
		}
	}
}
