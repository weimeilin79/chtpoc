package com.redhat.jdgdemo.marshaller;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.protostream.MessageMarshaller;

import com.redhat.jdgdemo.model.Author;
import com.redhat.jdgdemo.model.Book;

public class BookMarshaller implements MessageMarshaller<Book> {
		@Override
	   public String getTypeName() {
	      return "com.redhat.jdgdemo.model.Book";
	   }

	   @Override
	   public Class<? extends Book> getJavaClass() {
	      return Book.class;
	   }

	   @Override
	   public void writeTo(ProtoStreamWriter writer, Book book) throws IOException {
	      writer.writeString("title", book.getTitle());
	      writer.writeString("description", book.getDescription());
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(book.getPublicationYear());
	      int year = cal.get(Calendar.YEAR);
	      writer.writeInt("publicationYear",year);
	      writer.writeCollection("authors", book.getAuthors(), Author.class);
	   }

	   @Override
	   public Book readFrom(ProtoStreamReader reader) throws IOException {
	      String title = reader.readString("title");
	      String description = reader.readString("description");
	      int publicationYear = reader.readInt("publicationYear");
	      Set<Author> authors = reader.readCollection("authors", new HashSet<Author>(), Author.class);
	      return new Book(title, description, publicationYear, authors);
	   }
}
