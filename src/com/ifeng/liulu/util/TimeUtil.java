package com.ifeng.liulu.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	public static String dateIncre(String cur, int incre) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date curDate = sdf.parse(cur);
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		cal.add(Calendar.DAY_OF_YEAR, incre);
		Date date = cal.getTime();
		cur = sdf.format(date);
		return cur;
	}

	public static void main(String[] args) throws ParseException {
		String n = "2017-10-31";
		System.out.println(dateIncre(n, 3));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(sdf.format(new Date()));
	}
}
