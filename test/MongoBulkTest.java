import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

public class MongoBulkTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		MongoDatabase db = client.getDatabase("test");
		
		MongoCollection<Document> col = db.getCollection("user");
		
		
		List<WriteModel<Document>> docs = new ArrayList<WriteModel<Document>>();
		
		// #1
		String station = "Sta";
		String location = "Loc";
		String ymd = "2016-01-21T02:30:57.8654";
		Document key = new Document("_id", station + "_" + location + "_" + ymd);
		Document doc = new Document("$addToSet", new Document("CHA", "INNERDATA"));
		UpdateOneModel<Document> model = new UpdateOneModel<Document>
			(		key, 
					doc, 
					new UpdateOptions().upsert(true)
					);
		docs.add(model);
		
		// #2
		station = "Sta";
		location = "Loc";
		ymd = "2016-01-21T02:30:57.8654";
		key = new Document("_id", station + "_" + location + "_" + ymd);
		doc = new Document("$addToSet", new Document("CHA", "INNERDATA2"));
		UpdateOneModel<Document> model2 = new UpdateOneModel<Document>
		(		key, 
				doc, 
				new UpdateOptions().upsert(true)
				);
		docs.add(model2);
		
		// #3 
		station = "Sta";
		location = "Loc";
		ymd = "2016-01-21T02:30:57.8654";
		key = new Document("_id", station + "_" + location + "_" + ymd);
		doc = new Document("$addToSet", new Document("CHA", "INNERDATA3"));
		UpdateOneModel<Document> model3 = new UpdateOneModel<Document>
		(		key, 
				doc, 
				new UpdateOptions().upsert(true)
				);
		docs.add(model3);

		com.mongodb.bulk.BulkWriteResult result = col.bulkWrite(docs, new BulkWriteOptions().ordered(true));
		
		List<BulkWriteUpsert> aa = result.getUpserts();
		
		
		
		System.out.println(result.toString());
		
		for( BulkWriteUpsert upsert : aa) {
			System.out.println(upsert.toString());
		}
	}
}
