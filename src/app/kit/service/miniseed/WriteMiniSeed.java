package app.kit.service.miniseed;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCursor;

import app.kit.com.util.Helpers;
import app.kit.service.mongo.TraceDao;
import app.kit.service.seedlink.GenerateMiniSeed;
import app.kit.vo.FileContentVo;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class WriteMiniSeed implements Runnable {

	@Autowired private TraceDao traceDao;
	@Autowired private GenerateMiniSeed gm;
	private FileContentVo content;
	private int current;
	private int total;
	
	public WriteMiniSeed(FileContentVo content, int current, int total) {
		this.content = content;
		this.current = current;
		this.total = total;
	}
	
	@Override
	public void run() {
		
		log.debug("Request({}/{}). {}.{}.{}.{} {} - {}", current, total, content.getNetwork(), content.getStation(), content.getLocation(), content.getChannel(), 
				Trace.getBtimeToStringYMDHMS(content.getStBtime()), Trace.getBtimeToStringYMDHMS(content.getEtBtime()));
		write();
	}
	
	public boolean write() {
		
		int totSample = 0;
		DataOutputStream dos = null;
		
		String network = content.getNetwork();
		String station = content.getStation();
		String location = content.getLocation();
		String channel = content.getChannel();
		String filename = content.getFilename();
		Btime stBtime = content.getStBtime();
		Btime etBtime = content.getEtBtime();
		
		try {	
			MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, stBtime, etBtime);
			if ( !cursor.hasNext() ) {
				log.debug("There is no data.");
				return false;
			}
			
			FileUtils.forceMkdir(new File(content.getFullPathDir()));
			dos = new DataOutputStream(new FileOutputStream(content.getFullPathFilename()));
			
			while(cursor.hasNext()) {
				Document doc = cursor.next();
				
				Object o = doc.get(channel);
				if (o instanceof Document ) {
					Document sub = (Document) o;
					totSample += writeDocument(sub, stBtime, etBtime, dos);
				} else if ( o instanceof ArrayList<?>) {
					List<Document> subs = (List<Document>) o;
					Collections.sort(subs, Helpers.traceCompare);
					for(Document sub : subs) {
						totSample += writeDocument(sub, stBtime, etBtime, dos);
					}
				}
			}
			log.debug("Write to file. {}.{}.{}.{}, {} - {}, name: {}, nsamp: {}", 
					network, station, location, channel, Trace.getBtimeToStringYMDHM(stBtime), Trace.getBtimeToStringYMDHM(etBtime), filename, totSample);
			
		} catch(IOException e) {
			log.warn("{}", e);
			return false;
		} catch (SeedFormatException e) {
			log.warn("{}", e);
			return false;
		} finally {
			if ( dos != null) {
				try { dos.close();
				} catch (IOException e) { log.warn("{}", e);
					return false;
				}
			} 
		}
		
		if ( totSample == 0 ) {
			try {
				FileUtils.forceDelete(new File(content.getFullPathFilename()));
				log.debug("Delete file. No samplings(file size 0). name: {}", content.getFullPathFilename());
			} catch (IOException e) {
				log.warn("Failed to delete file. {}", e);
			}
			return false;
		} 
		return true;
	}
	
	private int writeDocument(Document sub, Btime stBtime, Btime etBtime, DataOutputStream dos) throws SeedFormatException, IOException {
		
		Binary binary = (Binary) sub.get("d");
		ByteBuf b = Unpooled.wrappedBuffer(binary.getData());
		int totSample = 0;
		
		DataRecord dr = (DataRecord)SeedRecord.read(b.array());
		DataRecord dr2 = gm.trimPacket(stBtime, etBtime, dr, false);

		if ( dr2 != null ) {
			totSample = dr2.getHeader().getNumSamples();
			dr2.write(dos);
		}
		
		return totSample;
	}
}
