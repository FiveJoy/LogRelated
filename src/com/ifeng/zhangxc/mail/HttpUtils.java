package com.ifeng.zhangxc.mail;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.mozilla.universalchardet.UniversalDetector;



public class HttpUtils {
	/**
	 * 用字节流方式抓取页面
	 */
	public static String downloadPageUseInputStream(String urlstr,String proxyIp,String proxyPort,int connectTimeout,int readTimeout) throws IOException{
		if(urlstr==null||urlstr.isEmpty()){
			return null;
		}
		Proxy proxy=null;
		if(proxyIp!=null&&proxyPort!=null){
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByAddress(proxyIp.getBytes()), Integer.valueOf(proxyPort)));
		}
		URL url = new URL( urlstr);
		HttpURLConnection conn = null;
		if(proxy!=null){
			conn = (HttpURLConnection) url.openConnection (proxy);
		}else{
			conn = (HttpURLConnection) url.openConnection ();
		}
        conn.setUseCaches(false);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        InputStream in = null;
    	boolean isRetry = true;
    	int timesOfRetry = 1;
    	//通信连接，重试三次
    	while(isRetry){
    		try{
    			in = conn.getInputStream();
    		}catch (FileNotFoundException e1){
    			//NotFound连接直接以异常形式抛出
    			throw e1;
    		}catch (SocketTimeoutException e2){
    			if(timesOfRetry==3){
    				throw e2;
    			}
    			++timesOfRetry;
    			continue;
    		}catch (IOException e3){
    			throw e3;
    		}
    		isRetry = false;
    		
    	}

        //int GOODSIZE = 8192; 
        isRetry = true;
    	timesOfRetry = 1;
    	int expectedLength = 200*1024;
    	byte[] buf = new byte[expectedLength];
    	int total = 0;
    	while(isRetry){
    		int n;
    		try {
    			while ((n = in.read (buf, total, buf.length - total)) != -1) {
    				total += n;
    				if (total == buf.length) {
    					int c = in.read ();
    					if (c == -1)
    						break; // EOF, we're done
    					else {
    						//System.err.println("扩容");
    						byte[] newbuf = new byte[buf.length * 2];
    						System.arraycopy (buf, 0, newbuf, 0, buf.length);
    						buf = newbuf;
    						buf[total++] = (byte) c;
    					}
    				}                    
    			}
    			
    			if(in!=null){
    				in.close ();
    			}
    		} catch(SocketTimeoutException e1){
    			buf = new byte[expectedLength];
    			total = 0;
    			if(timesOfRetry==3){
    				throw e1;
    			}
    			++timesOfRetry;
    			continue;
    			
    		}catch (IOException e) {
    			throw e;
    		}
    		isRetry = false;

    	}
        if(conn!=null){
        	conn.disconnect();
        }
        
        if (total != buf.length) {
            byte[] newbuf = new byte[total];
            System.arraycopy (buf, 0, newbuf, 0, total);
            buf = newbuf;
        }
        
		// 检测编码
		String strCharSet = "";
		// 解析网页得到编码
		// (1)
		UniversalDetector detector = new UniversalDetector(null);
		// (2)
		// byte[] buf1=new byte[4096];
		int len = 0;
		while (!detector.isDone() && len < buf.length) {
			if (buf.length - len > 4096)
				detector.handleData(buf, len, 4096);
			else
				detector.handleData(buf, len, buf.length - len);
			len += 4096;
		}
		// detector.handleData(buf, 0, total);
		// (3)
		detector.dataEnd();
		// (4)
		strCharSet = detector.getDetectedCharset();
		if (strCharSet == null || strCharSet.equals("WINDOWS-1252")
				|| strCharSet.equals("KOI8-R")
				|| strCharSet.equals("x-gbk")
				|| strCharSet.equals("MACCYRILLIC")) {
			// 默认GBK
			strCharSet = "UTF-8";
		}
		// (5)
		detector.reset();
//System.err.println("size:"+buf.length);
//System.err.println("编码："+strCharSet);		
		String content = new String(buf, strCharSet);
		return content;
	}
	public static String doPostDefault(String url,String postData,int connectionTimeout,int readTimeOut) throws Exception
	{
		DataInputStream inputStream=null;
		HttpURLConnection con =null;
		DataOutputStream outputStream=null;
		try 
		{	
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			outputStream=new DataOutputStream(con.getOutputStream());
			outputStream.write(postData.getBytes("UTF-8"));
			outputStream.flush();	
		}catch (IOException e){
			throw e;
		}finally{
			try{
				if(outputStream!=null)
					outputStream.close();
			}catch (Exception e){
				throw e;
			}
		}
		try{
			inputStream=new DataInputStream(con.getInputStream());
			while(inputStream.available()==0){
				
			}
			byte byteArray[]=new byte[inputStream.available()];
			inputStream.read(byteArray);
			String data=new String(byteArray,"UTF-8");
			return data;
		}catch (IOException e){
			throw e;
		}finally{
			try{
				if(inputStream!=null)
					inputStream.close();
/*				if(con!=null){
					con.disconnect();
				}*/
			}catch (Exception e){
				throw e;
			}
		}
	}
	
	public static String doGet(String url,int connectionTimeout,int readTimeOut,String proxyIp,String proxyPort,Map<String,String> requestProperties) throws Exception
	{
		InputStream inputStream=null;
		HttpURLConnection con =null;
		
		try 
		{	
			URL dataUrl = new URL(url);
			Proxy proxy=null;
			if(proxyIp!=null&&proxyPort!=null){
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByName(proxyIp), Integer.valueOf(proxyPort)));
			}
			
			
			if(proxy!=null){
				con = (HttpURLConnection) dataUrl.openConnection (proxy);
			}else{
				con = (HttpURLConnection) dataUrl.openConnection ();
			}
			 con.setUseCaches(false);
		   if(requestProperties!=null){
			   for(String key:requestProperties.keySet()){
		    	   con.setRequestProperty(key, requestProperties.get(key));
		       }
		   }
	     
	    	
	    	con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setDoOutput(true);
			con.setDoInput(true);
			String coding=con.getContentEncoding();
			if(coding!=null&&coding.equals("gzip")){
				 inputStream = new GZIPInputStream(con.getInputStream());
			}else{
				inputStream=new DataInputStream(con.getInputStream());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));  
            String line = "";  
            StringBuffer sb=new StringBuffer();
            while((line = reader.readLine()) != null) {  
            	sb.append(line).append("\n"); 
            }  
			return sb.toString();
		}catch (IOException e){
			throw e;
		}finally{
			try{
			if(inputStream!=null)
				inputStream.close();
			}catch (Exception e){
				throw e;
			}
		}
	}
}
