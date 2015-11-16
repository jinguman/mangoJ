package com.kit.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

public class DocumentToBinary {

	private static Codec<Document> DOCUMENT_CODEC = new DocumentCodec();
	
	public static InputStream toInputStream(final Document document) {
		BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
		BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
		DOCUMENT_CODEC.encode(writer, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
		return new ByteArrayInputStream(outputBuffer.toByteArray());
	}

	public static Document fromInputStream(final InputStream input) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		BsonBinaryReader bsonReader = new BsonBinaryReader(ByteBuffer.wrap(outputStream.toByteArray()));
		return DOCUMENT_CODEC.decode(bsonReader, DecoderContext.builder().build());
	}

}
