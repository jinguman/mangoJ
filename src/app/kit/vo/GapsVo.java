package app.kit.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class GapsVo {

	private String network;
	private String station;
	private String location;
	private String channel;
	private float sampleRate;
	
	private int day;
	private Map<String, Integer> hour = new HashMap<>();
	private Map<String, Integer> minute = new HashMap<>();
	
	public GapsVo(String network, String station, String location, String channel, String h, String m, float sampleRate, int nsamp) {
		
		this.network = network;
		this.station = station;
		this.location = location;
		this.channel = channel;
		this.sampleRate = sampleRate;
		
		add(h, m, sampleRate, nsamp);
	}
	
	public void add(String h, String m, float sampleRate, int nsamp) {
		addDay(nsamp);
		addHour(h, nsamp);
		addMinute(h, m, nsamp);
		this.sampleRate = sampleRate;
	}
	
	public void addDay(int nsamp) {
		this.day += nsamp;
	}
	
	public void addHour(String h, int nsamp) {
		if ( !hour.containsKey(h) ) {
			hour.put(h, nsamp);
		} else {
			int temp = hour.get(h);
			temp += nsamp;
			hour.put(h, temp);
		}
	}
	
	public void addMinute(String h, String m, int nsamp) {
		String tempKey = h + "." + m;
		if ( !minute.containsKey(tempKey) ) {
			minute.put(tempKey, nsamp);
		} else {
			int temp = minute.get(tempKey);
			temp += nsamp;
			minute.put(tempKey, temp);
		}
	}
	
	public void clear() {
		hour.clear();
		minute.clear();
	}
}
