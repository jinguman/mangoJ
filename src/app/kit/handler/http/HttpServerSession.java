package app.kit.handler.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.kit.com.conf.MangoConf;
import app.kit.vo.SessionVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpServerSession {

	private Map<String, SessionVo> map = new HashMap<>();
	@Autowired private MangoConf conf;
	
	
	public boolean addSession(String key) {
		if ( !map.containsKey(key) ) {
			map.put(key, new SessionVo(key));
		} 
		
		SessionVo sessionVo = map.get(key);
		if ( sessionVo.getSessionCnt() > conf.getAcSession() ) return false;
		else {
			sessionVo.addSessionCnt();
			return true;
		}
	}
	
	public void eraseSession(String key) {
		if ( map.containsKey(key)) {
			SessionVo sessionVo = map.get(key);
			sessionVo.withdrawSessionCnt();
		}
	}
	
	public void printSession() {
		log.debug("Session status.");
		for(String k : map.keySet()) {
			log.debug("{}", map.get(k));
		}
	}
}
