package com.ifeng.liulu.storm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.rmi.CORBA.Tie;

import org.apache.http.util.TextUtils;

public class TimeUtil {

	public static String TimeStamp2Date(String timestampString,String formats)
	{
		if (TextUtils.isEmpty(formats))
	        formats = "yyyy-MM-dd HH:mm:ss";
		Long timestamp = Long.parseLong(timestampString) * 1000;
	    String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
	    return date;
	}
	public static boolean isTheSameDay(String timestamp,String _date) {
		String action_date=TimeStamp2Date(timestamp, "yyyy-MM-dd HH:mm:ss");
		if(action_date.contains(_date))
			return true;
		else return false;
	}
	public static boolean isToday(String time)
	{
		//String action_date=TimeStamp2Date(timestamp, "yyyy-MM-dd HH:mm:ss");
		Date date=new Date(System.currentTimeMillis());
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String _date=simpleDateFormat.format(date);
		if(_date.contains(time))
			return true;
		else return false;
	}
	public static String getDate()//获取当天日期----2017-10-17这样�?
	{
		Date date=new Date(System.currentTimeMillis());
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String _date=simpleDateFormat.format(date);
		return _date.substring(0,10);
	}
	public static void main(String[] args){
		String date="1497775470";
		
		System.out.println(isTheSameDay("1497775470", "2017-06-18"));
		
	}

}
