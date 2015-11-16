package com.kit;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Controller.ApiNettyController;
import com.kit.Controller.ApiSparkController;
import com.kit.Util.PropertyManager;

public class MangoJA {

	public static void main( String[] args ) throws IOException {
    	Logger logger = LoggerFactory.getLogger(MangoJA.class);
    	logger.info("{}","MangoJA start..");
		
    	// get property
    	PropertyManager pm = new PropertyManager();
    	
		//ApiSparkController asc = new ApiSparkController();
		ApiNettyController controller = new ApiNettyController(pm);
		controller.run();
		
    }
	

}
