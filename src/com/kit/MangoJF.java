package com.kit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MseedClient.MseedSimpleClient;
import com.kit.Util.PropertyManager;

public class MangoJF {

    public static void main( String[] args ) {
    	// Run configuration.. vm argument: -Dlog4j.configuration="file:./home/config/log4j.xml"
    	Logger logger = LoggerFactory.getLogger(MangoJF.class);
    	logger.info("{}","MangoJF start..");
    	
    	Map<String, Object> indexMap = new ConcurrentHashMap<>();
    	PropertyManager pm = new PropertyManager();
	
    	String filename = "d:/ANM.HGZ.2015.337.00.00.00";
    	
    	MseedSimpleClient msc = new MseedSimpleClient(pm, indexMap);
    	msc.read(filename);
    }
}
