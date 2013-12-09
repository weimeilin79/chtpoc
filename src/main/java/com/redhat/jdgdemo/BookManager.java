/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.jdgdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.remote.ProtobufMetadataManager;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.redhat.jdgdemo.marshaller.AuthorMarshaller;
import com.redhat.jdgdemo.marshaller.BookMarshaller;
import com.redhat.jdgdemo.model.Author;
import com.redhat.jdgdemo.model.Book;


/**
 * @author Christina Lin
 */
public class BookManager {
	//public static final String JMX_DOMAIN = ProtobufMetadataManager.class.getSimpleName();

	//public static final String JMX_DOMAIN = ProtobufMetadataManager.class.getSimpleName();
	
    private static final String PROPERTIES_FILE = "jdg.properties";
    private RemoteCacheManager cacheManager;
    private RemoteCache<String, Object> cache;

    public BookManager() {
    	try {
	    	ConfigurationBuilder builder = new ConfigurationBuilder();
	    	
	        builder.withProperties(jdgProperty());
	        
	        builder.marshaller(new ProtoStreamMarshaller());
	        cacheManager = new RemoteCacheManager(builder.build());
	        ObjectName objName = new ObjectName("jboss.infinispan:type=RemoteQuery,name=\"chtpoc\",component=ProtobufMetadataManager");
	
	        //initialize server-side serialization context via JMX
	        byte[] descriptor = readClasspathResource("/library.protobin");
	        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer();;
	        mBeanServer.invoke(objName, "registerProtofile", new Object[]{descriptor}, new String[]{byte[].class.getName()});
	        
	        SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(cacheManager);
    	
		
			serCtx.registerProtofile("/library.protobin");
		
			serCtx.registerMarshaller(Book.class, new BookMarshaller());
			serCtx.registerMarshaller(Author.class, new AuthorMarshaller());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DescriptorValidationException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        
    }
    
    

    
	   public void setData(){
		   cache = cacheManager.getCache("books");
	       if(cache.size()==0) {
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
	       	
	       	cache.put("1", book1);
	       	cache.put("2", book2);
	       }
	   }

	public void doSearch(){
		   QueryFactory qf = Search.getQueryFactory(cache);
		   Query query = qf.from(Book.class)
		               .having("title").like("%How%").toBuilder()
		               .build();

		   List<Book> list = query.list();
		   for(Book book:list){
			   System.out.println("book");
		   }
	}
	
	public Book getFromCache(String index){
		return (Book) cache.get(index);
	}
	
    public void stop() {
        cacheManager.stop();
    }

    public static void main(String[] args) {
        BookManager manager = new BookManager();
        
        System.out.println("Start input book data into DataGrid......");
        manager.setData();
        
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        System.out.println("Start to Search Data Grid......");
        //manager.doSearch();
        
        System.out.println("Get [1] from Cache : "+manager.getFromCache("1"));
        System.out.println("Done! Client Closing");
        manager.stop();
    }

    public static Properties jdgProperty() {
        Properties props = new Properties();
        try {
            props.load(BookManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return props;
    }
    
    private byte[] readClasspathResource(String classPathResource) throws IOException {
        InputStream is = getClass().getResourceAsStream(classPathResource);
        try {
           ByteArrayOutputStream os = new ByteArrayOutputStream();
           byte[] buf = new byte[1024];
           int len;
           while ((len = is.read(buf)) != -1) {
              os.write(buf, 0, len);
           }
           return os.toByteArray();
        } finally {
           is.close();
        }
     }
}
