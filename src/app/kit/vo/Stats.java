package app.kit.vo;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class Stats {

	@Getter private Map<String, StatsVo> map;
	
	public Stats() {
		map = new HashMap<String, StatsVo>();
	}
	
	public void clear() {
		map.clear();
	}
	
	public void put(Trace trace) {
		String key = getKey(trace);
		if ( !map.containsKey(key) ) {
			StatsVo vo = new StatsVo(trace.getNetwork(),trace.getStation(), trace.getLocation(), trace.getChannel(), trace.getStBtime(), trace.getEtBtime());
			map.put(key, vo);
		} else {
			StatsVo vo = map.get(key);
			if ( vo.getStBtime().after(trace.getStBtime()) ) vo.setStBtime(trace.getStBtime());
			if ( vo.getEtBtime().before(trace.getEtBtime()) ) vo.setEtBtime(trace.getEtBtime());
		}
	}
	
	private String getKey(Trace trace) {
		StringBuffer sb = new StringBuffer();
		sb.append(trace.getNetwork()).append("_");
		sb.append(trace.getStation()).append("_");
		sb.append(trace.getLocation()).append("_");
		sb.append(trace.getChannel());
		return sb.toString();
	}
	
	@Override
	public String toString() {
	
		StringBuffer sb = new StringBuffer();
		
		for(String key : map.keySet()) {
			StatsVo vo = map.get(key);
			sb.append(key).append(":").append(vo.toString()).append(" / ");
		}
		
		return "Stats(" + sb.toString() + ")";

	}
}
