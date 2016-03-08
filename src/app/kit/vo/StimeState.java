package app.kit.vo;

import org.bson.Document;

import com.mongodb.client.MongoCursor;

import edu.sc.seis.seisFile.mseed.Btime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StimeState {

	private Btime stBtime;
	private Btime etBtime;

}
