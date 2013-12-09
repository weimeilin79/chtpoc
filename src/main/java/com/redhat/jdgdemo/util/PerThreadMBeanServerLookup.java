package com.redhat.jdgdemo.util;

import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.infinispan.jmx.MBeanServerLookup;

public class PerThreadMBeanServerLookup implements MBeanServerLookup {
	 static ThreadLocal<MBeanServer> threadMBeanServer = new ThreadLocal<MBeanServer>();

	   public MBeanServer getMBeanServer(Properties properties) {
	      return getThreadMBeanServer();
	   }

	   public static MBeanServer getThreadMBeanServer() {
	      MBeanServer beanServer = threadMBeanServer.get();
	      if (beanServer == null) {
	         beanServer = MBeanServerFactory.createMBeanServer();
	         threadMBeanServer.set(beanServer);
	      }
	      return beanServer;
	   }
}
