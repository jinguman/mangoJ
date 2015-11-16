package com.kit.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

public class PropertyManager {

	@Setter @Getter private Properties prop;
	
	final Logger logger = LoggerFactory.getLogger(PropertyManager.class);
	
    public PropertyManager(){

        InputStream is = null;
        try {
            this.prop = new Properties();
            is = this.getClass().getResourceAsStream("/config/mango.properties");
            prop.load(is);
            is.close();

        } catch (FileNotFoundException e) {
            logger.error("{}","File not found. file: ./config/mango.properties");
        	//e.printStackTrace();
        } catch (IOException e) {
        	logger.error("{}","Failure loading file. file: ./config/mango.properties");
            //e.printStackTrace();
        }
    }
    
    public int getIntegerProperty(String key) {
    	String value = prop.getProperty(key);
    	return Integer.parseInt(value);
    }
    
    public String getStringProperty(String key) {
    	return prop.getProperty(key);
    }
    
    public String[] getStringListProperty(String key) {
    	return prop.getProperty(key).split(",");
    }
    
    public boolean getBooleanProperty(String key) {
    	String value = prop.getProperty(key);
    	return Boolean.valueOf(value);
    }
}
