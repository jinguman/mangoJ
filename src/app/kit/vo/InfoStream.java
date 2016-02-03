package app.kit.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoStream {

	@JacksonXmlProperty private String location;
	@JacksonXmlProperty(localName="seedname") private String channel;
	@JacksonXmlProperty private String type;
	@JacksonXmlProperty(localName="begin_time") private String beginTime;
	@JacksonXmlProperty(localName="end_time") private String endTime;
	@JacksonXmlProperty private String begin_recno;
	@JacksonXmlProperty private String end_recno;
	@JacksonXmlProperty private String gap_check;
	@JacksonXmlProperty private String gap_treshold;
}
