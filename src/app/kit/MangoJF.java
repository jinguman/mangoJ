package app.kit;

import app.kit.com.daemon.IMangoLoader;
import app.kit.com.daemon.MangoFileLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MangoJF {

	private void startMangoFile() throws Exception {
		IMangoLoader loader = new MangoFileLoader();
		loader.startEngine();
	}
	
	
    public static void main( String[] args ) {

    	// vm argument: -Dlogback.configurationFile="file:./home/conf/logback.xml"
    	//Logger logger = LoggerFactory.getLogger(MangoJC.class);
    	
    	MangoJF main = new MangoJF();
		try {
			main.startMangoFile();
		} catch (Exception e) {
			log.error("{}", e);
		}
    }
}

