package app.kit;

import app.kit.com.daemon.IMangoLoader;
import app.kit.com.daemon.MangoInitLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MangoJI {

	private void startMangoInit(String year) throws Exception {
		IMangoLoader loader = new MangoInitLoader();
		((MangoInitLoader)loader).setYear(year);
		loader.startEngine();
	}
	
	
    public static void main( String[] args ) {

    	// vm argument: -Dlogback.configurationFile="file:./home/conf/logback.xml"
    	//Logger logger = LoggerFactory.getLogger(MangoJC.class);
    	
    	if ( args.length != 1) {
    		System.out.println("Usage: YYYY");
    		return;
    	}
    	
    	MangoJI main = new MangoJI();
		try {
			main.startMangoInit(args[0]);
		} catch (Exception e) {
			log.error("{}", e);
		}
    }
}

