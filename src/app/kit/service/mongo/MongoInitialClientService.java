package app.kit.service.mongo;

import java.util.List;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import app.kit.com.util.Helpers;
import app.kit.vo.SLState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MongoInitialClientService {

	@Autowired private ShardDao shardDao;
	@Resource(name="mongoDatabaseBean") private MongoDatabase database;
	@Autowired private SLState slState;
	
	public void getIndexes() {
		
		MongoCursor<String> cursor = database.listCollectionNames().iterator();
		while(cursor.hasNext()) {
			String s = cursor.next();

			MongoCollection<Document> col = database.getCollection(s);
			MongoCursor<Document> c =  col.listIndexes().iterator();
			
			while(c.hasNext()) {
				Document d = c.next();
				slState.addIndex(d.getString("ns"), d.getString("name"));

			}
		}
	}
	
	public void getShardCollections() {
		
		List<String> keys = shardDao.getShardCollections();

		for(String key : keys)
			//indexMap.put(key, true);
			slState.addShard(key);
	}
	
	public void getShardRange() {
		
		List<String> keys = shardDao.getShardRange();

		for(String key : keys)
			//indexMap.put(key, true);
			slState.addShardRange(key);
	}
	
	public void doEtIndex(String network, String station, String location, String channel, String year, String month, boolean isBackground) {
		
		String collectionName = Helpers.getTraceCollectionName(network, station, location, channel, year, month);
		MongoCollection<Document> collection = database.getCollection(collectionName);
		
		String ns = collection.getNamespace().getFullName();
		String idxName = channel + ".et_1";
		if ( slState.isIndex(ns, idxName)) return;
		slState.addIndex(collection.getNamespace().getFullName(), idxName);
		
		// make index
		Document doc = new Document(channel+".et",1);
		IndexOptions indexOptions = new IndexOptions();
		indexOptions.background(false);
		indexOptions.background(isBackground);
		
		try {
			String str = collection.createIndex(doc, indexOptions);
			log.debug("Create index. col: {}, idx: {}, rtn: {}", collectionName, doc.toJson(), str);
		} catch (MongoException e) {
			if ( e.getCode() == 67 ) {
				// too many indexes for collection
				log.warn("Too many index. col: {}, idx: {}", collectionName, doc.toJson());
			}
		}
	}
	
	public void doShard(String network, String station, String location, String channel, String year, String month) {

		String collectionName = Helpers.getTraceCollectionName(network, station, location, channel, year, month);
		MongoCollection<Document> collection = database.getCollection(collectionName);

		// shardCollection
		String ns = collection.getNamespace().getFullName();
		String collectionKey = "_id";
		
		if ( !slState.isShard(ns , collectionKey)) {
			slState.addShard(ns, collectionKey);
			shardDao.shardCollection(collectionName, new Document("_id",1));
		}
	}
}
