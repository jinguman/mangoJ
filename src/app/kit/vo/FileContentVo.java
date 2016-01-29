package app.kit.vo;

import java.text.DecimalFormat;

import app.kit.com.util.Helpers;
import edu.sc.seis.seisFile.mseed.Btime;
import lombok.Data;

@Data
public class FileContentVo {

	private Btime stBtime;
	private Btime etBtime;
	private String network;
	private String station;
	private String location;
	private String channel;
	private String rootDir;
	private String fullPathDir;
	private String filename;
	private String fullPathFilename;
	private boolean dbCheck;	// for restore
	
	DecimalFormat threeZero = new DecimalFormat("000");
    DecimalFormat fourZero = new DecimalFormat("0000");

    public FileContentVo() {}
    
	public FileContentVo(String network, String station, String location, String channel, String rootDir, Btime stBtime, Btime etBtime) {
		this.network = network;
		this.station = station;
		this.location = location;
		this.channel = channel;
		this.rootDir = rootDir;
		this.stBtime = stBtime;
		this.etBtime = etBtime;

		make();
	}
    
	public void make() {
		if ( !rootDir.endsWith("/") ) rootDir += "/";
		fullPathDir = rootDir 
				+ "/" + new String(fourZero.format(stBtime.year)) 
				+ "/" + new String(threeZero.format(stBtime.jday))
				+ "/" + network
				+ "/" + station + "/";
		
		filename = Helpers.getFileName(network, station, location, channel, stBtime);
		fullPathFilename = fullPathDir + filename;
	}
}
