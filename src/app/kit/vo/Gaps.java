package app.kit.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class Gaps {

	@Getter private Map<String, GapsVo> map;
	
	public Gaps() {
		map = new HashMap<String, GapsVo>();
	}
	
	public void clear() {
		
		for(String key : map.keySet()) map.get(key).clear(); 
		map.clear();
	}
	
	public void put(Trace trace) {
		String key = getKey(trace);
		if ( !map.containsKey(key) ) {
			GapsVo vo = new GapsVo(trace.getNetwork(),trace.getStation(), trace.getLocation(), trace.getChannel(), 
					Trace.getBtimeToStringH(trace.getStBtime()), Trace.getBtimeToStringM(trace.getStBtime()), 
					trace.getSampleRate(), trace.getNumSamples());
			map.put(key, vo);
		} else {
			GapsVo vo = map.get(key);
			vo.add(Trace.getBtimeToStringH(trace.getStBtime()), Trace.getBtimeToStringM(trace.getStBtime()), 
					trace.getSampleRate(), trace.getNumSamples());
		}
	}
	
	private String getKey(Trace trace) {
		StringBuffer sb = new StringBuffer();
		sb.append(trace.getNetwork()).append("_");
		sb.append(trace.getStation()).append("_");
		sb.append(trace.getLocation()).append("_");
		sb.append(trace.getChannel()).append("_");
		sb.append(Trace.getBtimeToStringYMD(trace.getStBtime()));
		return sb.toString();
	}
	
}
