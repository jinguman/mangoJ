package app.kit.vo;

import org.bson.Document;

import com.mongodb.client.MongoCursor;

import edu.sc.seis.seisFile.mseed.Btime;
import lombok.Data;

@Data
public class CursorState extends StimeState {

	private MongoCursor<Document> cursor;
	public CursorState(Btime stBtime, Btime etBtime, MongoCursor<Document> cursor) {
		super(stBtime, etBtime);
		this.cursor = cursor;
	}

}
