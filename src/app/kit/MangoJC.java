package app.kit;

import app.kit.com.daemon.IMangoLoader;
import app.kit.com.daemon.MangoCollectorLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MangoJC {

	private void startMangoCollector() throws Exception {
		IMangoLoader loader = new MangoCollectorLoader();
		loader.startEngine();
	}
	
    public static void main( String[] args ) {

		// vm argument: -Dlogback.configurationFile="file:./home/conf/logback.xml"
    	//Logger logger = LoggerFactory.getLogger(MangoJC.class);
    	
    	MangoJC main = new MangoJC();
		try {
			main.startMangoCollector();
		} catch (Exception e) {
			log.error("{}", e);
		}
    }
}
