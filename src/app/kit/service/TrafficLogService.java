package app.kit.service;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TrafficLogService {

	public static void write(String title, String host, String context) {
		log.info("{} {} {}", title, host, context);
	}
}
