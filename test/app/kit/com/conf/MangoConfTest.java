package app.kit.com.conf;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class MangoConfTest {

	@Autowired
	private MangoConf conf;
	
	@Resource(name="mongoDatabaseBean")
	private MongoDatabase db3;
	
	@Resource(name="mongoCollectionStats")
	private MongoCollection<Document> collectionStats;
	
	@Test
	public void test() {
		MongoDatabase db1 = conf.getMongoDatabaseBean();
		MongoDatabase db2 = conf.getMongoDatabaseBean();
	
		assertEquals(db1, db2);
		assertEquals(db1, db3);
	}

}
