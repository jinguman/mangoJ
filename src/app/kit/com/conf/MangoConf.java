package app.kit.com.conf;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import app.kit.com.util.MangoJCode;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;

/**
 * Configuration class
 * @author jman
 *
 */
@Configuration
@Component
@Qualifier("mangoConf")
@PropertySource("classpath:/conf/mango.properties")
@ComponentScan("app.kit, app.kit.controller, app.kit.service, app.kit.vo, app.kit.handler")
@ImportResource("classpath:/conf/ApplicationContext.xml")
public class MangoConf {

	@Autowired
	private PropertiesConfiguration prop;

	public String getMongoUri() {
		return prop.getString("mongo.uri", "mongodb://localhost");
	}
	
	public String getMongoDatabase() {
		return prop.getString("mongo.database", "trace");
	}
	
	public int getScThread() {
		return prop.getInt("sc.thread", 1);
	}
	
	public String getScNetwork(int n) {
		return prop.getString("sc." + n + ".network");
	}
	public String getScStation(int n) {
		return prop.getString("sc." + n + ".station");
	}
	public String getScChannel(int n) {
		return prop.getString("sc." + n + ".channel");
	}
	
	public String getScHost(int n) {
		return prop.getString("sc." + n + ".host", getScDefaultHost());
	}
	public String getScDefaultHost() {
		return prop.getString("sc.host", SeedlinkReader.DEFAULT_HOST);
	}
	
	public int getScPort(int n) {
		return prop.getInt("sc." + n + ".port", getScDefaultPort());
	}
	public int getScDefaultPort() {
		return prop.getInt("sc.port", SeedlinkReader.DEFAULT_PORT);
	}
	
	public boolean isMcSharpMinute() {
		return prop.getBoolean("mc.sharpMinute", true);
	}
	
	public int getScQueueLimit() {
		return prop.getInt("sc.queuelimit", 50000);
	}
	
	public int getScRestartSec() {
		return prop.getInt("mi.restartsec", 5);
	}
	
	public boolean isMcShard() {
		return prop.getBoolean("mc.shard", false);
	}
	
	public boolean isMcIndex() {
		return prop.getBoolean("mc.index", false);
	}
	
	public boolean isMcWriteGapStats() {
		return prop.getBoolean("mc.writeGapStats", false);
	}
	
	public int getMcRestartSec() {
		return prop.getInt("mc.restartsec", 5);
	}
	
	public int getMcLogThreshold() {
		return prop.getInt("mc.logthreshold", 500);
	}
	
	public int getMcThread() {
		return prop.getInt("mc.thread", 10);
	}
	
	public int getMiThread() {
		return prop.getInt("mi.thread", 10);
	}
	
	public boolean isMiBuildEntireList() {
		return prop.getBoolean("mi.buildentirelist", false);
	}
	
	public int getMiRestartSec() {
		return prop.getInt("mi.restartsec", 5);
	}
	
	public boolean isMiShard() {
		return prop.getBoolean("mi.shard", false);
	}
	
	public boolean isMiIndex() {
		return prop.getBoolean("mi.index", false);
	}
	
	public String getMfQueue() {
		return prop.getString("mf.queue", "../conf/mango.queue");
	}
	
	public boolean isMfResumeQueue() {
		return prop.getBoolean("mf.resumeQueue", false);
	}
	
	public int getAcThread() {
		return prop.getInt("ac.thread", 10);
	}
	
	public int getAcPort() {
		return prop.getInt("ac.port", 8080);
	}
	
	public int getAcSession() {
		return prop.getInt("ac.session", 10);
	}
	
	@Bean(name="mongoClientBean")
	public MongoClient getMongoClientBean() {
		return new MongoClient(new MongoClientURI(getMongoUri()));
	}
	
	@Bean(name="mongoDatabaseBean")
	public MongoDatabase getMongoDatabaseBean() {
		return getMongoClientBean().getDatabase(getMongoDatabase());
	}
	
	@Bean(name="mongoDatabaseAdmin")
	public MongoDatabase getMongoDatabaseAdminBean() {
		return getMongoClientBean().getDatabase("admin");
	}

	@Bean(name="mongoDatabaseConfig")
	public MongoDatabase getMongoDatabaseConfigBean() {
		return getMongoClientBean().getDatabase("config");
	}
	
	@Bean(name="mongoCollectionStats")
	public MongoCollection<Document> getMongoCollectionStatsBean() {
		return getMongoDatabaseBean().getCollection(MangoJCode.COLLECTION_TRACE_STATS);
	}
	
	@Bean(name="mongoCollectionGaps")
	public MongoCollection<Document> getMongoCollectionGapsBean() {
		return getMongoDatabaseBean().getCollection(MangoJCode.COLLECTION_TRACE_GAPS);
	}

	@Bean(name="updateOptionsTrue")
	public UpdateOptions getUpdateOptionsTrue() {
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);
		return options;
	}
	
	@Bean(name="updateOptionsFalse")
	public UpdateOptions getUpdateOptionsFalse() {
		UpdateOptions options = new UpdateOptions();
		options.upsert(false);
		return options;
	}
}
