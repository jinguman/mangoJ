package app.kit.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
public class InfoStation {

	@JacksonXmlProperty(localName="name") private String station;
	@JacksonXmlProperty private String network;
	@JacksonXmlProperty private String description;
	@JacksonXmlProperty(localName="begin_seq") private String beginSeq;
	@JacksonXmlProperty(localName="end_seq") private String endSeq;
	@JacksonXmlProperty(localName="stream_check") private String streamCheck;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	private InfoStream[] stream;
}
