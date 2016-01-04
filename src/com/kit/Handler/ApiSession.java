package com.kit.Handler;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

public class ApiSession {

	private Map<String, Integer> map;
	@Setter int limitCnt;
	
	public ApiSession() {
		this.map = new HashMap<>();
	}
	
	public void addSession(String key) {
		if ( map.containsKey(key) ) {
			int cnt = map.get(key);
			
		} else {
			
		}
	}
	
	public void eraseSession(String key) {
		
	}
}
