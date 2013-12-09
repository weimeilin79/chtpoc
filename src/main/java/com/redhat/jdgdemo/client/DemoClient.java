package com.redhat.jdgdemo.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.remote.ProtobufMetadataManager;

import com.redhat.jdgdemo.marshaller.AuthorMarshaller;
import com.redhat.jdgdemo.marshaller.BookMarshaller;
import com.redhat.jdgdemo.model.Author;
import com.redhat.jdgdemo.model.Book;

public class DemoClient {
	public static final String JMX_DOMAIN = ProtobufMetadataManager.class.getSimpleName();
	public static final String TEST_CACHE_NAME = "chtpoc";
	private RemoteCacheManager remoteCacheManager;
	private RemoteCache<Integer, Book> remoteCache;
	 
	 
	 
	 public void startClient() throws Exception{
		   org.infinispan.client.hotrod.configuration.ConfigurationBuilder clientBuilder = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder();
		   clientBuilder.addServer().host("127.0.0.1").port(15233);
		   clientBuilder.marshaller(new ProtoStreamMarshaller());
		   remoteCacheManager = new RemoteCacheManager(clientBuilder.build());
		   remoteCache = remoteCacheManager.getCache(TEST_CACHE_NAME);

		   SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);
		   serCtx.registerProtofile("/library.protobin");
		
		   serCtx.registerMarshaller(Book.class, new BookMarshaller());
		   serCtx.registerMarshaller(Author.class, new AuthorMarshaller());

	 }
	 
	 public static void main(String[] args){
		 
		 System.out.println("Starting HotRod Client");
		 DemoClient client = new DemoClient();
		 try {
			client.startClient();
			client.createBook();
			client.getBooks();
			client.searchBookWithName();
			
			client.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 
	 }
	 
	 public void createBook(){
		   	System.out.println("Creating Book......");
		   	
		   	Author authorChristina = new Author("Christina ","Lin");
	       	Author authorJagabee = new Author("Jagabee","Hsieh");	       	
	       	Author authorTigger = new Author("Tigger","Disney");	       	
	       	Author authorPuji = new Author("Puji","Lin");
	       	
	       	Set<Author> set1 = new HashSet<Author>();
	       	set1.add(authorChristina);
	       	set1.add(authorJagabee);
	       	Book book1 = new Book("How to JBoss","How to write program with JBoss",79,set1);
	       	
	       	Set<Author> set2 = new HashSet<Author>();
	       	set2.add(authorTigger);
	       	set2.add(authorPuji);
	       	Book book2 = new Book("How to be a cat","Becomming a perfect cat",109,set2);
	       	remoteCache.put(1, book1);
	       	remoteCache.put(2, book2);
	}
	 
	public void getBooks(){
		System.out.println("Getting Books......");
		System.out.println(remoteCache.get(1));
		System.out.println(remoteCache.get(2));
	       
	}
	
	public void searchBookWithName() throws Exception {
		  System.out.println("Searching Books......");
	      QueryFactory qf = Search.getQueryFactory(remoteCache);
	      Query query = qf.from(Book.class).having("title").like("How *").toBuilder().build();
	      List<Book> list = query.list();
	      System.out.println("Found "+list.size()+" book(s)");
	      for(Book book:list){
	    	  System.out.println(book);
	      }
	     
	   }
	 
	 
	 public void release(){
		 killRemoteCacheManager(remoteCacheManager);
		 
	 }
	 
	 /**
	    * Kills a remote cache manager.
	    *
	    * @param rcm the remote cache manager instance to kill
	    */
	   public static void killRemoteCacheManager(RemoteCacheManager rcm) {
	      try {
	         if (rcm != null) rcm.stop();
	      } catch (Throwable t) {
	         t.printStackTrace();
	      }
	   }
	   
	 /**
	    * Kills a group of remote cache managers.
	    *
	    * @param rcm
	    *           the remote cache manager instances to kill
	    */
	   public static void killRemoteCacheManagers(RemoteCacheManager... rcms) {
	      if (rcms != null) {
	         for (RemoteCacheManager rcm : rcms) {
	            try {
	               if (rcm != null)
	                  rcm.stop();
	            } catch (Throwable t) {
	            	t.printStackTrace();
	            }
	         }
	      }

	   }
	 

}
