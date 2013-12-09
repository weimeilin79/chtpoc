package com.redhat.jdgdemo.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;

@Indexed
public class Book {
   @Field String title;
   @Field String description;
   @Field @DateBridge(resolution=Resolution.YEAR) Date publicationYear;
   @IndexedEmbedded Set<Author> authors = new HashSet<Author>();

   public Book(String title, String description,Date publicationYear,Set<Author> authors){
	   
	   this.title = title;
	   this.description = description;
	   this.publicationYear = publicationYear;
	   this.authors = authors;
	   
   }
   
 public Book(String title, String description,int publicationYear,Set<Author> authors){
	   
	   this.title = title;
	   this.description = description;
	   Calendar calendar = Calendar.getInstance();
	   calendar.set(Calendar.YEAR, publicationYear + 1900);
	   this.publicationYear = calendar.getTime();
	   this.authors = authors;
	   
   }
   
 
 	

	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Date getPublicationYear() {
		return publicationYear;
	}
	
	public Set<Author> getAuthors() {
		return this.authors;
	}

	@Override
    public String toString() {
        return "Book [title=" + title + ", description=" + description + ", PublicationYear="+publicationYear+"authors="
                + authors + "]";
    }

	
 
}