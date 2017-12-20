package com.ifeng.liulu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	public static void main(String[] args)
	{
		String bigfile="E:\\data\\update\\smallg\\0.log";
	
		String line="";
		int subcount=0;
		byte[] bytearray=new byte[1024];
		try {
			FileReader fr=new FileReader(bigfile);
			BufferedReader br=new BufferedReader(fr);
		int i=0;
			
				while((line=br.readLine())!=null)
				{
					i++;
					 System.out.println("ÕýÔÚread "+i+"ÐÐ");
					 String[] aaa=line.split(" ");
					 System.out.println(aaa.length);
				}
			
		} catch( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
