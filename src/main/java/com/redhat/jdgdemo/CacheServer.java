package com.redhat.jdgdemo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.infinispan.Cache;
import org.infinispan.commons.equivalence.ByteArrayEquivalence;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.remote.ProtobufMetadataManager;
import org.infinispan.server.hotrod.HotRodServer;

import com.redhat.jdgdemo.model.Author;
import com.redhat.jdgdemo.model.Book;
import com.redhat.jdgdemo.util.PerThreadMBeanServerLookup;
import com.redhat.jdgdemo.util.TestHelper;
import com.redhat.jdgdemo.util.UtilHelper;

public class CacheServer {
	public static final String JMX_DOMAIN = ProtobufMetadataManager.class.getSimpleName();

	   public static final String TEST_CACHE_NAME = "chtpoc";
	   protected EmbeddedCacheManager cacheManager;
	   protected Cache<Object, Object> cache;
	   
	   private HotRodServer hotRodServer;
	  

	   protected EmbeddedCacheManager createCacheManager() throws Exception {
	      GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder().nonClusteredDefault();
	      gcb.globalJmxStatistics()
	            .enable()
	            .allowDuplicateDomains(true)
	            .jmxDomain(JMX_DOMAIN)
	            .mBeanServerLookup(new PerThreadMBeanServerLookup());

	      
	      System.out.println("jmxDomain:["+JMX_DOMAIN+"]");
	      
	      ConfigurationBuilder builder = new ConfigurationBuilder();
	      builder.dataContainer()
	            .keyEquivalence(ByteArrayEquivalence.INSTANCE)
	            .indexing().enable()
	            .addProperty("default.directory_provider", getLuceneDirectoryProvider())
	            .addProperty("lucene_version", "LUCENE_CURRENT");

	      //cacheManager = TestCacheManagerFactory.createCacheManager(gcb, new ConfigurationBuilder(), true);
	      cacheManager = new DefaultCacheManager(gcb.build());
	      //cacheManager.
	      cacheManager.defineConfiguration(TEST_CACHE_NAME, builder.build());
	      cache = cacheManager.getCache(TEST_CACHE_NAME);

	      return cacheManager;
	   }

	   
	   
	   public static void main(String[] args){
		   CacheServer cacheServer = new CacheServer();
		   try {
			System.out.println("Creating Local Cache Manager for HotRod Server.......");
			cacheServer.createCacheManager();
			
			System.out.println("Starting HotRod Server.......");
			cacheServer.startHotRodServer();
			
			
			waitForEnterPressed("Stop HotRod Server");
			System.out.println("Stopping HotRod Server.......");
			cacheServer.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
	   }
	   
	   
	   
	   public void startHotRodServer() throws Exception{
		   hotRodServer = TestHelper.startHotRodServer(cacheManager);
		   
		   System.out.println("HotRod Server started: with HOST:["+hotRodServer.getHost()+"] Port:["+hotRodServer.getPort()+"]");
		   
		   //Register MBean 
		   ObjectName objName = new ObjectName(JMX_DOMAIN + ":type=RemoteQuery,name="
                   + ObjectName.quote("DefaultCacheManager")
                   + ",component=" + ProtobufMetadataManager.OBJECT_NAME);

		   System.out.println("Registering objName:["+objName.getCanonicalName()+"]");
		   
		   //initialize server-side serialization context via JMX
		   //byte[] descriptor = UtilHelper.readClasspathResource(getClass().getResourceAsStream("/library.protobin")); 
		   byte[] descriptor = UtilHelper.readClasspathResource(getClass().getResourceAsStream("/chtpoc.protobin")); 
		   MBeanServer mBeanServer = PerThreadMBeanServerLookup.getThreadMBeanServer();
		   mBeanServer.invoke(objName, "registerProtofile", new Object[]{descriptor}, new String[]{byte[].class.getName()});

		   System.out.println("registerProtofile:[chtpoc.protobin] Done...");
		   
	   }
	   
	   
	   

	   protected String getLuceneDirectoryProvider() {
	      return "ram";
	   }

	   public void release() {
	      
	      killServers(hotRodServer);
	      System.out.println("HotRod Server STOPPED !");
	   }
	   

	   /**
	    * Kills a group of Hot Rod servers.
	    *
	    * @param servers the group of Hot Rod servers to kill
	    */
	   public static void killServers(HotRodServer... servers) {
	      if (servers != null) {
	         for (HotRodServer server : servers) {
	            try {
	               if (server != null) server.stop();
	            } catch (Throwable t) {
	            	t.printStackTrace();
	            }
	         }
	      }
	   }
	   
	   private static void waitForEnterPressed(String nextAction) {
	        try {
	            System.out.println("\nPress <Enter> to "+nextAction+"...");
	            System.in.read();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
}
