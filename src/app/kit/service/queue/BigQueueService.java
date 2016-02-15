package app.kit.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.kit.com.queue.BlockingBigQueue;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BigQueueService {

	@Autowired private BlockingBigQueue queue;
	
	// every 5minutes = 5 * 60 * 1000
	@Scheduled(fixedRate = 300000)
	public void gc() {
		queue.gc();
		log.debug("Queue gc execute..");
	}
}
