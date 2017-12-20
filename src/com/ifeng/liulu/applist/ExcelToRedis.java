package com.ifeng.liulu.applist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import redis.clients.jedis.Jedis;

public class ExcelToRedis {
	public static void Read(String file_path) {
		File file=new File(file_path);
		Jedis jedis=new Jedis("10.90.9.71", 6379,100000);
		jedis.auth("6i6FxegQb8FyPqypS7pM");
		try {
			FileReader fr=new FileReader(file);
			BufferedReader br=new BufferedReader(fr);
			String line="";  
			while((line=br.readLine())!=null) {
				Process(line,jedis);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void Process(String line,Jedis jedis) {
		String[] detail=line.split("\t");
		if(detail.length==5) {
			String key=detail[0];
			String value=detail[5];
			jedis.set(key, value);
		}	
	}
	public static void main(String[] args) {
		String file_path="E:\\data\\Extract\\appdetail0.txt";
		
	}
}
