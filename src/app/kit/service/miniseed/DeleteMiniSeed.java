package app.kit.service.miniseed;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.UpdateResult;

import app.kit.service.mongo.MongoSimpleClientService;
import app.kit.service.mongo.TraceGapsDao;
import app.kit.vo.FileContentVo;
import app.kit.vo.Gaps;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.Btime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class DeleteMiniSeed implements Runnable {

	@Autowired private MongoSimpleClientService service;
	@Autowired private TraceGapsDao dao;
	private FileContentVo content;
	private int current;
	private int total;
	private Gaps gaps = new Gaps();
	
	public DeleteMiniSeed(FileContentVo content, int current, int total) {
		this.content = content;
		this.current = current;
		this.total = total;
	}
	
	@Override
	public void run() {
		
		log.debug("Request({}/{}). {}.{}.{}.{} {} - {}", current, total, content.getNetwork(), content.getStation(), content.getLocation(), content.getChannel(), 
				Trace.getBtimeToStringYMDHMS(content.getStBtime()), Trace.getBtimeToStringYMDHMS(content.getEtBtime()));
		delete();
	}
	
	public boolean delete() {
		
		Btime stBtime = content.getStBtime();
		Btime etBtime = content.getEtBtime();
		
		while(etBtime.afterOrEquals(stBtime)) {

			Trace trace = new Trace(content.getNetwork(), content.getStation(), content.getLocation(), content.getChannel(), stBtime);
			UpdateResult result = service.unsetTrace(trace);
			
			if ( result.getModifiedCount() > 0 || result.getUpsertedId() != null ) {
				int value = dao.getTraceGapsValueM(trace.getNetwork(), trace.getStation(), trace.getLocation(), trace.getChannel(), trace.getStBtime());
				trace.setNumSamples(value*-1);
				gaps.put(trace);
			}
			Trace.getAddBtime(stBtime, Calendar.MINUTE, 1);		
		}
		service.insertGaps(gaps);
		
		log.info("Delete complete({}/{}). file: {}, {}", current, total, content.getNetwork(), content.getStation(), content.getLocation(), content.getChannel(), 
				Trace.getBtimeToStringYMDHMS(content.getStBtime()), Trace.getBtimeToStringYMDHMS(content.getEtBtime()));
		
		return true;
	}

}
