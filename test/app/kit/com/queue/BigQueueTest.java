package app.kit.com.queue;

import java.io.IOException;

import org.junit.Test;

import app.kit.com.queue.bigqueue.BigQueueImpl;
import app.kit.com.queue.bigqueue.IBigQueue;

public class BigQueueTest {

	@Test
	public void test() throws IOException {
		
		String queueDir = "d:/";
		String queueName = "test.queue";
		IBigQueue queue = new BigQueueImpl(queueDir, queueName);
		
		queue.enqueue(new String("test1").getBytes());
		queue.enqueue(new String("test2").getBytes());
		queue.enqueue(new String("test3").getBytes());
		
		System.out.println(queue.size());
		
		System.out.println(new String(queue.dequeue()));
		System.out.println(new String(queue.dequeue()));
		System.out.println(new String(queue.dequeue()));
	}
}
