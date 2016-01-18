package app.kit.vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import edu.sc.seis.seisFile.mseed.Btime;
import lombok.Data;

@Data
public class InfoSeedlink {

	@JacksonXmlProperty private String software;
	@JacksonXmlProperty private String organization;
	@JacksonXmlProperty private String started;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	private InfoStation[] station;
	
	public String[] getStations(String network) {
		
		List<String> list = new ArrayList<>();
		
		for(InfoStation infoStation : station) {
			if ( infoStation.getNetwork().equals(network)) list.add(infoStation.getStation());
		}
		
		String[] stations = new String[list.size()];
		list.toArray(stations); 
		
		return stations;
	}
	
	public List<Trace> getTraces(Btime btime) {
		
		List<Trace> traces = new ArrayList<>();
		
		for(InfoStation infoStation : station) {
				
			String network = infoStation.getNetwork();
			String station = infoStation.getStation();
			
			for(InfoStream infoStream : infoStation.getStream()) {
				
				String location = infoStream.getLocation();
				String channel = infoStream.getChannel();
				
				Trace trace = new Trace(network, station, location, channel, btime);
				traces.add(trace);
			}
		}
		return traces;
	}
	
	public List<Trace> getTraces(String net, Btime btime) {
		
		List<Trace> traces = new ArrayList<>();
		
		for(InfoStation infoStation : station) {
			if ( infoStation.getNetwork().equals(net)) {
				
				String network = infoStation.getNetwork();
				String station = infoStation.getStation();
				
				for(InfoStream infoStream : infoStation.getStream()) {
					
					String location = infoStream.getLocation();
					String channel = infoStream.getChannel();
					
					Trace trace = new Trace(network, station, location, channel, btime);
					traces.add(trace);
				}
			}
		}
		return traces;
	}
	
}
