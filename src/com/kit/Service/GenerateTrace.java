package com.kit.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Util.PropertyManager;

import lombok.Setter;


public class GenerateTrace implements Runnable{

	private BlockingQueue<Document> queue;

	private Calendar startTime = Calendar.getInstance();
	private Calendar endTime = Calendar.getInstance();
	@Setter private int nsamp;
	@Setter private String network;
	@Setter private String[] stations;
	@Setter private String location = "";

	@Setter private String[] channels;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private int plusTime = 2; //seconds
	
	private PropertyManager pm;

	final Logger logger = LoggerFactory.getLogger(GenerateTrace.class);

	public GenerateTrace(BlockingQueue<Document> queue) {
		this.queue = queue;
		//initMongoClient();
	}
	
	public GenerateTrace(BlockingQueue<Document> queue, PropertyManager pm) {
		this.queue = queue;
		this.pm = pm;
		//initMongoClient();
		plusTime = pm.getIntegerProperty("gt.plustime");
	}
	
	public void run()  {
		// TODO Auto-generated method stub
		try {
			
			startTime.setTime(formatter.parse(pm.getStringProperty("gt.starttime")));
			endTime.setTime(formatter.parse(pm.getStringProperty("gt.endtime")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		int cnt = 0;
		int logThreshold = pm.getIntegerProperty("gt.logthreshold");
		while(true) {
			
			for(String station : stations) {
				for (String channel : channels) {
					
					Document d = new Document()
									.append("st", formatter.format(startTime.getTime()))
									.append("n", nsamp * plusTime )
									.append("c", 1)
									.append("s", nsamp);

					Calendar ca = (Calendar) startTime.clone();
					ca.add(Calendar.SECOND, plusTime);

					//d.append("d", getRandomList(nsamp * plusTime))
							d.append("et", formatter.format(ca.getTime()))
							.append("network", network)
							.append("station", station)
							.append("location", location)
							.append("channel", channel);

					//Helpers.printJson(d);
					int queueThreshold = pm.getIntegerProperty("gt.queuethreshold");
					while(queue.size() > queueThreshold) {
						try {
							Thread.sleep(1000);
							logger.debug("<< Waiting queue: {}", queue.size());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					try {
						queue.put(d);
						if ( cnt > logThreshold ) {
							logger.debug("< Put queue: {}", queue.size());
							cnt = 0;
						}
						cnt++;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
			}
			startTime.add(Calendar.SECOND, plusTime);
			
			
			if ( endTime.compareTo(startTime) < 0 ) break;
		}
	}
	
	private List<Integer> getRandomList(int number) {
		
		List<Integer> lists = new ArrayList<Integer>(); 
		
		for(int i=0; i<number; i++) {
			lists.add((int)(Math.random()*10000));
		}
		return lists;
	}

}
