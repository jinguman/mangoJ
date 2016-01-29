package app.kit.vo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.sc.seis.seisFile.mseed.Btime;
import lombok.Data;

@Component
@Qualifier("slState")
@Data
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
	
	// { NETWORK : [{
	//			STATION: [{
	//					LOCATION:CHANNEL
	//					},{
	//					LOCATION:CHANNEL
	//			}]
	//			
	//Helpers.printJson(streamsInfoDoc);
	private Document streamsInfoDoc;
	
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
	
	public void addStream(SLNetStation slnetStation) {
		
		String network = slnetStation.getNetwork();
		String station = slnetStation.getStation();
		int seqnum = slnetStation.getSeqnum();
		String stime = slnetStation.getStime();
		
		String key = network + "_" + station;
		if ( !streamMap.containsKey(key)) {
			streamMap.put(key, slnetStation);
		} else {
			SLNetStation value = streamMap.get(key);
			value.setSeqnum(seqnum);
			value.setStime(stime);
		}
	}
	
	public int findStreamSeqnum(String network, String station) {
		
		String key = network + "_" + station;
		if ( streamMap.containsKey(key) ) {
			SLNetStation value = streamMap.get(key);
			return value.getSeqnum();
		} else 
			return -1;
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
	
	public void saveStreams(File file) throws IOException {

		List<String> streams = getAllStreamStr();
		FileUtils.writeLines(file, streams);
	}
	
	public void restoreStreams(File file) throws IOException {
		
		List<String> streams = FileUtils.readLines(file);
		for(String line : streams) {
			SLNetStation slnetStation = toParse(line);
			addStream(slnetStation);
		}
	}
}
