package com.kit.Vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mysql.fabric.RangeShardMapping;

import edu.sc.seis.seisFile.mseed.Btime;

public class SLState {

	// index map
	// 1. INDEX(ns.index.indexName : boolean)
	//    trace.PB_E_2015.HNE.et_1 : true
	//
	// 2. SHARD(ns.shardCollection.shardName : boolean)
	//    trace.PB_E_2015._id : true
	//
	// 3. SHARD RANGE(ns.shardRange.rangeName : boolean)
	//    trace.AZ_BASP_00_2015.shardRange.ATAG : true
	private Map<String, Boolean> indexMap = new ConcurrentHashMap<>(); 
	private Map<String, Boolean> shardMap = new ConcurrentHashMap<>();
	private Map<String, Boolean> shardRangeMap = new ConcurrentHashMap<>();
	private Map<String, SLNetStation> streamMap = new ConcurrentHashMap<>();
	
	public void addIndex(String ns, String idxName) {
		
		String key = ns + idxName;
		if ( !indexMap.containsKey(key)) {
			indexMap.put(key, true);
		}
	}
	
	public boolean isIndex(String ns, String idxName) {
		String key = ns + idxName;
		
		Boolean bo = indexMap.get(key);
		if ( bo == null ) bo = false;
		return bo;
	}
	
	public void addShard(String ns, String collectionKey) {
		String key = ns + collectionKey;
		
		if ( !shardMap.containsKey(key)) {
			shardMap.put(key, true);
		}
	}
	
	public void addShard(String key) {
		if ( !shardMap.containsKey(key)) {
			shardMap.put(key, true);
		}
	}
	
	public boolean isShard(String ns, String collectionKey) {
		String key = ns + collectionKey;
		
		Boolean bo = shardMap.get(key);
		if ( bo == null ) bo = false;
		return bo;
	}
	
	public void addShardRange(String ns, String rangeKey) {
		String key = ns + rangeKey;
		
		if ( !shardRangeMap.containsKey(key)) {
			shardRangeMap.put(key, true);
		}
	}
	
	public void addShardRange(String key) {
		if ( !shardRangeMap.containsKey(key)) {
			shardRangeMap.put(key, true);
		}
	}
	
	public boolean isShardRange(String ns, String rangeKey) {
		String key = ns + rangeKey;
		
		Boolean bo = shardRangeMap.get(key);
		if ( bo == null ) bo = false;
		return bo;
	}
	
	public void addStream(String network, String station, int seqnum, Btime btime) {
		
		String key = network + "_" + station;
		if ( !streamMap.containsKey(key) ) {
			SLNetStation value = new SLNetStation(network, station, seqnum, SLNetStation.getSLTimeStamp(btime));
			streamMap.put(key, value);
		} else {
			SLNetStation value = streamMap.get(key);
			value.setSeqnum(seqnum);
			value.setStime(SLNetStation.getSLTimeStamp(btime));
		}
	}
	
	public List<String> getAllStreamStr() {
		
		List<String> lists = new ArrayList<>();
		for (String key: streamMap.keySet()) {

			SLNetStation value = streamMap.get(key);
			lists.add(value.toString());
		}
		return lists;
	}
	
	public static SLNetStation toParse(String line) {
		
		String[] words = line.split(" ");
		return new SLNetStation(words[0], words[1], Integer.parseInt(words[2]), words[3]); 
	}
}
