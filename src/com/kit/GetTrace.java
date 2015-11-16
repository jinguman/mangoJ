package com.kit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;

public class GetTrace {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		MongoDatabase database = client.getDatabase("trace");
		
		MongoCollection<Document> collection = database.getCollection("AV_ZRO__201511");
		
		Document query = new Document();
		query.append("_id", new Document("$gte","ZRO_2015-11-13T05:38"))
			.append("EHZ.et", new Document("$lte","2015-11-13T05:40:12"));
		
		Document proj = new Document();
		proj.append("EHZ", 1);
		
		List<Document> docs = new ArrayList<Document>();
		MongoCursor<Document> cursor = collection.find(query).projection(proj).iterator();
		
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			//doc.remove("BHN.d");
			//doc.remove("BHE.d");
			//doc.remove("BHZ.d");
			System.out.println(doc.get("_id"));
		}
		
		/*
		for(Document doc : docs) {
			List<Document> d = (List<Document>) doc.get("BHN");
			for(Document t : d) {
				System.out.println(t);
				

				Binary bytes = (Binary) t.get("d");
				
				try {
					DataRecord dr = (DataRecord)SeedRecord.read(bytes.getData());
					
					DecompressedData decomData;
					try {
						decomData = dr.decompress();
						int[] temp = decomData.getAsInt();
						
						System.out.println(dr.getHeader().getStartTime() + ", " + temp.length);
						
					} catch (CodecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
					
					
					
					
					
				} catch (SeedFormatException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		}*/
	}

}
