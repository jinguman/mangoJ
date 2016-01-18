package app.kit.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
public class InfoStream {

	@JacksonXmlProperty private String location;
	@JacksonXmlProperty(localName="seedname") private String channel;
	@JacksonXmlProperty private String type;
	@JacksonXmlProperty(localName="begin_time") private String beginTime;
	@JacksonXmlProperty(localName="end_time") private String endTime;
}
