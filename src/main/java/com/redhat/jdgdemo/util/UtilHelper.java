package com.redhat.jdgdemo.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UtilHelper {
	
	public static byte[] readClasspathResource(InputStream is) throws IOException {
	      
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
