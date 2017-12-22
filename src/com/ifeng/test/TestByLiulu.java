package com.ifeng.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TestByLiulu {
	static String[] keys = { "docid", "simid", "updateTurns", "unUpdateTurns", "hotParameter", "priorScore",
			"globalScore", "verticalScore", "qualityEvalLevel", "sourceEvalLevel", "isBills", "time" };

	public static String extractLogToJson(String line) {
		try {
			HashMap<String, Object> info_map = new HashMap<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// 2017-12-06 00:05:14
			info_map.put("time", sdf.parse(line.substring(line.indexOf("[") + 1, line.indexOf(","))));

			int index_1 = line.indexOf("UpdateHistory");
			int index_start = line.indexOf("- ", index_1) + 2;
			String[] details = line.substring(index_start).split(",");
			info_map.put("docid", details[0]);
			info_map.put("simid", details[2]);
			info_map.put("updateTurns", Integer.parseInt(details[3].substring(details[3].indexOf("=") + 1)));
			info_map.put("unUpdateTurns", Integer.parseInt(details[4].substring(details[4].indexOf("=") + 1)));
			int start_hotParameter = line.indexOf("hotParameter") + 13;
			int end__hotParameter = line.indexOf(",priorScore");
			String hotParameter_info = line.substring(start_hotParameter, end__hotParameter);
			info_map.put("hotParameter", hotParameter_info);
			details = line.substring(end__hotParameter - 1).split(",");
			info_map.put("priorScore", Double.parseDouble(details[0].substring(details[0].indexOf("=") + 1)));
			info_map.put("globalScore", Double.parseDouble(details[1].substring(details[1].indexOf("=") + 1)));

			int end_verticalScore = line.indexOf(",qualityEvalLevel");
			String verticalScore_info = line.substring(line.indexOf("verticalScore") + 14, end_verticalScore);
			info_map.put("verticalScore", verticalScore_info);

			details = line.substring(end_verticalScore + 1).split(",");
			info_map.put("qualityEvalLevel", Double.parseDouble(details[0].substring(details[0].indexOf("=") + 1)));
			info_map.put("sourceEvalLevel", details[1].substring(details[1].indexOf("=") + 1));
			info_map.put("isBills", Boolean.parseBoolean(details[2].substring(details[2].indexOf("=") + 1)));
		} catch (Exception e) {

		}

		return null;
	}

	private static boolean isSpecialTime(String cur_date) throws ParseException {
		Calendar ca = Calendar.getInstance();// 得到一个Calendar的实例
		Date today_date = new Date();
		ca.setTime(today_date); // 设置时间为当前时间
		ca.add(Calendar.DAY_OF_MONTH, -1); // 减1
		Date lastDay = ca.getTime(); // 结果
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		String laString = sdf1.format(lastDay);
		if (laString.equals(cur_date)) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			String cur_time = sdf.format(today_date);

			ca.setTime(today_date);
			ca.set(Calendar.HOUR_OF_DAY, 5);
			ca.set(Calendar.MINUTE, 0);

			if (today_date.before(ca.getTime())) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws ParseException {

	}
}
