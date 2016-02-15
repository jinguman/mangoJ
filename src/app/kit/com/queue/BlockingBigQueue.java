package app.kit.com.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

import app.kit.com.queue.bigqueue.BigQueueImpl;
import app.kit.com.queue.bigqueue.IBigQueue;
import app.kit.vo.Trace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlockingBigQueue {

	private IBigQueue cache;
	
	public BlockingBigQueue(String queueDir, String queueName) {
		try {
			cache = new BigQueueImpl(queueDir, queueName);
		} catch (IOException e) {
			log.error("{}", e);
		} 
	}

	public void gc() {
		try {
			cache.gc();
		} catch(IOException e) {
			log.error("{}", e);
		}
	}
	
	public List<Trace> take() {

		List<Trace> traces = null;
		byte[] bytes = null;
		try {
			bytes = cache.dequeue();
		} catch (IOException e1) {
			log.error("{}", e1);
		}
		if ( bytes == null ) return null;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		
		try {
		  in = new ObjectInputStream(bis);
		  traces = (List<Trace>) in.readObject(); 
		  
		} catch (ClassNotFoundException | IOException e) {
			log.error("{}", e);
		} finally {
		  try {
		    bis.close();
		  } catch (IOException ex) {
			  log.error("{}", ex);
		  }
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException ex) {
			  log.error("{}", ex);
		  }
		}
		
		return traces;
	}
	
	public long size() {
		return cache.size();
	}
	
	public void put(List<Trace> traces) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(traces);
			cache.enqueue(bos.toByteArray());

		} catch (IOException e) {
			log.error("{}", e);
		} finally {
			if ( out != null ) {
				try {
					out.close();
				} catch (IOException e) {
					log.error("{}", e);
				}
			}
			
			if ( bos != null ) {
				try {
					bos.close();
				} catch (IOException e) {
					log.error("{}", e);
				}
			}
		}
		
	}
	
	
}
