package app.kit.vo;

import java.util.Date;

import lombok.Data;

@Data
public class SessionVo {

	private String sessionKey;
	private int sessionCnt;
	private Date insertTime;
	
	public SessionVo(String key) {
		this.sessionKey = key;
		this.insertTime = new Date();
		this.sessionCnt = 1;
	}
	
	public void addSessionCnt() {
		++sessionCnt;
		this.insertTime = new Date();
	}
	
	public void withdrawSessionCnt() {
		if ( sessionCnt > 0 ) {
			--sessionCnt;
			this.insertTime = new Date();
		}
	}
}
