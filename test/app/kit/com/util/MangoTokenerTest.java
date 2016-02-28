package app.kit.com.util;

import org.junit.Test;

public class MangoTokenerTest {

	@Test
	public void getToken() {
		String key = "USR0400";
		String encodeText = MangoTokener.encode(key);
		System.out.println(encodeText);
		
		String decodeText = MangoTokener.decode(encodeText);
		System.out.println(decodeText);
		
		//char[] test = MangoTokener.getWheel(key.toCharArray());
		//System.out.println(new String(MangoTokener.getWheel(test)));
		
		//key = "abcdefghijklmopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~_+ ";
		//String encodeSalt = MangoTokener.getSalts(key);
		//System.out.println(encodeSalt);
		//String decodeSalt = MangoTokener.removeSalts(encodeSalt);
		//System.out.println(decodeSalt);
		//System.out.println(key);
	}
}
