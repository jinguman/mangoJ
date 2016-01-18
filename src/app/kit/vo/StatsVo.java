package app.kit.vo;

import edu.sc.seis.seisFile.mseed.Btime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsVo {
	private String network;
	private String station;
	private String location;
	private String channel;
	private Btime stBtime;
	private Btime etBtime;
}