package app.kit.com.util;

import org.apache.commons.codec.binary.Base64;

public class MangoTokener {

	public static String decode(String key) {
		String wheelDecodedText = removeSalts(key);
		String decodedText = getWheel(wheelDecodedText);
		return new String(Base64.decodeBase64(decodedText.getBytes()));
	}
	
	public static String encode(String key) {
		String encodedText = new String(Base64.encodeBase64(key.getBytes()));
		String wheelEncodedText = getWheel(encodedText);
		return getSalts(wheelEncodedText);
	}
	
	private static String getWheel(String key) {
		return new String(getWheel(key.toCharArray()));
	}
	
	private static char[] getWheel(char[] chars) {
		for(int i=0; i<chars.length/2; i++) {
			char temp = chars[i*2+1];
			chars[i*2+1] = chars[i*2];
			chars[i*2] = temp;
		}
		return chars;
	}
	
	private static String getSalts(String key) {
		char[] chars = key.toCharArray();
		for(int i=0; i<chars.length; i++) {
			char c = chars[i];
			if ( ((int)c >= 97 && (int)c <= 121) || ((int)c >= 65 && (int)c <= 89) ) {
				chars[i] = (char) ((int)c + 1);
			}
			if ( (int)c == 90 || (int)c == 122) {
				chars[i] = (char) ((int)c - 25);
			}
		}
		return new String(chars);
	}
	
	private static String removeSalts(String key) {
		char[] chars = key.toCharArray();
		for(int i=0; i<chars.length; i++) {
			char c = chars[i];
			if ( ((int)c >= 98 && (int)c <= 122) || ((int)c >= 66 && (int)c <= 90) ) {
				chars[i] = (char) ((int)c - 1);
			}
			if ( (int)c == 97 || (int)c == 65) {
				chars[i] = (char) ((int)c + 25);
			}
		}
		return new String(chars);
	}
}
