package app.kit.vo;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class InfoStrreamTest {

	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		
		String streamsStr = "<stream locaion='t' seedname='e' />";
		
		XmlMapper mapper = new XmlMapper();
		InfoStream info = mapper.readValue(streamsStr, InfoStream.class);
		
		System.out.println(info.toString());
	}

}
