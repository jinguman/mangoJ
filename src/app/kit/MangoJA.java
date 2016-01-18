package app.kit;

import java.io.IOException;

import app.kit.com.daemon.IMangoLoader;
import app.kit.com.daemon.MangoApiLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MangoJA {

	private void startMangoApi() throws Exception {
		IMangoLoader loader = new MangoApiLoader();
		loader.startEngine();
	}
	
	public static void main( String[] args ) throws IOException {
    	
		// vm argument: -Dlogback.configurationFile="file:./home/conf/logback.xml"
    	//Logger logger = LoggerFactory.getLogger(MangoJC.class);
    	
    	MangoJA main = new MangoJA();
		try {
			main.startMangoApi();
		} catch (Exception e) {
			log.error("{}", e);
		}
		
    }
	

}
