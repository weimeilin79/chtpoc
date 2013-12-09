package com.redhat.jdgdemo.marshaller;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import com.redhat.jdgdemo.model.Author;

public class AuthorMarshaller implements MessageMarshaller<Author> {

	@Override
	public Class<? extends Author> getJavaClass() {
		return Author.class;
	}

	@Override
	public String getTypeName() {
		return "com.redhat.jdgdemo.model.Author";
	}

	@Override
	public Author readFrom(ProtoStreamReader reader) throws IOException {
		String name = reader.readString("name");
	    String surname = reader.readString("surname");
	    return new Author(name,surname);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Author author) throws IOException {
		writer.writeString("name", author.getName());
	    writer.writeString("surname", author.getSurname());		
	}

}
