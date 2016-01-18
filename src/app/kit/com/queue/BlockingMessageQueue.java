package app.kit.com.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import app.kit.vo.Trace;
import lombok.extern.slf4j.Slf4j;

/**
 * SPIS Message File Queue Class
 * @author sleepy10
 *
 */
@Component
@Qualifier("blockingMessageQueue")
@Slf4j
public class BlockingMessageQueue {
	
	private BlockingQueue<List<Trace>> cache = new LinkedBlockingQueue<List<Trace>>();

	public List<Trace> take() throws InterruptedException {
		return cache.take();
	}
	
	public int size() {
		return cache.size();
	}
	
	public void put(List<Trace> traces) throws InterruptedException {
		cache.put(traces);
	}
	
	public List<Trace> remove() {
		return cache.remove();
	}
	
	/**
	 * Load to queue from the file
	 * @param queueDir Queue Path
	 */
	@SuppressWarnings("unchecked")
	public void load(File file) {
		
		cache.clear();
		
		if (! file.exists() ) {
			log.info("No exist file. Nothing to load. file: {}", file.getAbsolutePath());
			return;
		}
		
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		
		try {
			fin = new FileInputStream(file);
			ois = new ObjectInputStream(fin);
			cache = (BlockingQueue<List<Trace>>) ois.readObject();
			
		} catch(IOException | ClassNotFoundException e) {
			log.error("{}", e);
		} finally {
			if ( ois != null ) {
				try { ois.close();
				} catch (IOException e) { log.error("{}", e);
				}
			}
			if ( fin != null ) {
				try { fin.close();
				} catch (IOException e) { log.error("{}", e);
				}
			}
		}
		FileUtils.deleteQuietly(file);
	}
	
	/**
	 * Save the contents of queue to the file
	 */
	public void save(File file) {
		
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(file);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(cache);
		} catch (IOException e) {
			log.error("{}", e);
		} finally {
			if ( oos != null ) {
				try { oos.close();
				} catch (IOException e) { log.error("{}", e);
				}
			}
			if ( fout != null ) {
				try { fout.close();
				} catch (IOException e) { log.error("{}", e);
				}
			}
		}
	}
	
	public void clear() {
		cache.clear();
	}
}
