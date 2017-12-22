package com.ifeng.liulu.hotboostLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.liulu.util.LoadConfig;
import com.ifeng.liulu.util.SendWebMail;
import com.ifeng.liulu.util.TimeUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class LogProcessor {
	protected static final Log LOG = LogFactory.getLog(LogProcessor.class);
	/*
	 * WAIT_FOR_UPDATE sleep时间 作用：等待当前文件更新，文件60min写入一次。写文件大概花费5分钟
	 */
	private static final int WAIT_FOR_UPDATE = 1000 * 60 * 60;////
	private static final int WAIT_FOR_UPDATE_forlog = 60;// min
	/*
	 * WAIT_FOR_GENER_TIMES
	 * 最大等待次数（如果文件不存在，将会不断休息WAIT_FOR_UPDATE这些分钟，如果这个次数下还没生成，则报警。）
	 */
	private static final int WAIT_FOR_GENER_TIMES = 3;
	private static String start_date = "";
	private static String end_date = "";
	private static String cur_date = "";
	private static boolean END_MARK = false;
	private static String file_path = null;

	private static FileReader fr = null;
	private static BufferedReader br = null;

	private static int invalid = 0;

	/*
	 * EXPIRETIME 数据留存时间 暂定30day --60s*60*30
	 */
	private static int EXPIRETIME = 60 * 60 * 30;

	/*
	 * 通过程序参数获取读取起始日期的文件
	 */
	public static boolean initLogDate(String[] dateProps) {
		if (dateProps.length == 0) {
			// 默认取当前日期
			Date date = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			start_date = dateformat.format(date);
		} else if (dateProps.length == 1) {
			start_date = dateProps[0];
		} else if (dateProps.length == 2) {
			start_date = dateProps[0];
			end_date = dateProps[1];
		}

		if (start_date == null || start_date.equals("")) {
			LOG.error("invalid start_date");
			return false;
		}
		if (dateProps.length == 2 && (end_date == null || end_date.equals(""))) {
			LOG.error("invalid end_date");
			return false;
		}
		cur_date = start_date;
		return true;

	}

	public static void StartProcessLog() {
		MongoUtil.getConnect();

		if (start_date == null || start_date.trim().equals("")) {
			LOG.error("invalid start_log_date,init start log date first!");
			return;
		}
		while (!END_MARK) {// 如果没有结束就不停止。
			long start = System.currentTimeMillis();

			try {
				String remain = PrepareToReadLogFile();
				if (remain == null)
					continue;
				else {
					// 把刚刚读出来的那一行传入读取函数
					beginToRead(remain);
				}
				Thread.sleep(WAIT_FOR_UPDATE);// 休息30min，等待当前文件更新

				// 如果cur_date是前一天，此时已经是今天的凌晨，说明文件已经更改
				if (isSpecialTime(cur_date)) {
					if (br != null) {
						br.close();
						br = null;
					}
					if (fr != null) {
						fr.close();
						fr = null;
					}
					// 加时间
					cur_date = TimeUtil.dateIncre(cur_date, 1);
				}

			} catch (InterruptedException | IOException | ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		try {
			br.close();
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static boolean isSpecialTime(String cur_date) throws ParseException {
		// 条件是：当前处理的cur_date为系统时间的前一天，且当天系统时间在凌晨5点之前----关闭流=null
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

	public static boolean isInTime(String sourceTime, String curTime) {
		if (sourceTime == null || !sourceTime.contains("-") || !sourceTime.contains(":")) {
			throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
		}
		if (curTime == null || !curTime.contains(":")) {
			throw new IllegalArgumentException("Illegal Argument arg:" + curTime);
		}
		String[] args = sourceTime.split("-");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		try {
			long now = sdf.parse(curTime).getTime();
			long start = sdf.parse(args[0]).getTime();
			long end = sdf.parse(args[1]).getTime();
			if (args[1].equals("00:00")) {
				args[1] = "24:00";
			}
			if (end < start) {
				if (now >= end && now < start) {
					return false;
				} else {
					return true;
				}
			} else {
				if (now >= start && now < end) {
					return true;
				} else {
					return false;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
		}

	}

	private static String PrepareToReadLogFile() throws InterruptedException, IOException, ParseException {
		// 这个函数是在等待120min以后出现的
		// 一方面，如果文件更新了，表示还是当天的没处理好。
		// 另一方面如果没更新，检查新文件是否存在，如果存在的话返回
		// 先检查文件是否存在。
		// if文件没变，就不new了，变了的话就New一下。
		// prepare就是用来准备文件的，准备好了就退出，没准备好就解决，怎么解决无非是等一等。

		// 获取当前时间
		String file_name_suffix = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date _today_date = new Date();
		String today_date = sdf.format(_today_date);
		Date _cur_date = sdf.parse(cur_date);
		if (_cur_date.before(_today_date) && !cur_date.equals(today_date)) {// 说明是之前的日志带后缀解析
			file_name_suffix = "." + cur_date;
		}
		String line = null;
		int listener = 0;
		file_path = LoadConfig.lookUpValueByKey("log_online_path") + LoadConfig.lookUpValueByKey("file_name_pre")
				+ file_name_suffix;
		File logfile = new File(file_path);
		LOG.info("prepare logfile of " + file_path);
		while (!logfile.exists()) {
			if (!logfile.exists()) {

				// 文件不存在，可能是还没生成，休息
				LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep " + WAIT_FOR_UPDATE_forlog
						+ "min ,目前已经等待" + listener + "个" + WAIT_FOR_UPDATE_forlog + "min");
				Thread.sleep(WAIT_FOR_UPDATE_forlog);
				if (listener > WAIT_FOR_GENER_TIMES) {
					LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep " + WAIT_FOR_UPDATE_forlog
							+ "min ,目前已经等待" + listener + "个" + WAIT_FOR_UPDATE_forlog + "min");
					LOG.info("等待时间过久，将发送邮件报警");
					sendAlarmEmail(file_path);
				}
				listener++;
			}
		}
		if (fr == null) {
			// 开始处理新的日志文件
			LOG.info("prepare tools of extract...");
			fr = new FileReader(logfile);
			br = new BufferedReader(fr);
			line = br.readLine();
		} else {
			line = br.readLine();
			if (line != null) {
				// 说明此文件仍有日志数据需要处理
				LOG.info("the file of " + cur_date + " has been updated so go on");
			} else {
				// 说明今日数据已经全部写入完成，此文件不再更新
				LOG.info(file_path + "'s invalid lines count=" + invalid);
				br.close();
				fr.close();
				br = null;
				fr = null;
				LOG.info("the file of " + cur_date + " has been extracted!next!");
				// 加时间
				cur_date = TimeUtil.dateIncre(cur_date, 1);
				invalid = 0;
				if (cur_date.equals(end_date))
					END_MARK = true;
			}
		}
		return line;
	}

	private static void sendAlarmEmail(String file_path) {
		String alarm_receiver = LoadConfig.lookUpValueByKey("alarm_email_receiver");
		// 发送正文
		String alarm_info = LoadConfig.lookUpValueByKey("alarm_info");
		// 发送主题
		String alarm_theme = LoadConfig.lookUpValueByKey("alarm_theme");
		String logErrorAlarmURL = LoadConfig.lookUpValueByKey("log_error_alarm_url");
		LOG.error(file_path + " hasn't generated normally ");
		SendWebMail.sendMail(alarm_receiver, alarm_info, alarm_theme, logErrorAlarmURL);

	}

	private static void beginToRead(String line) throws IOException {
		LOG.info("begin to read logfile of " + cur_date);
		int count = 0;
		Long start = System.currentTimeMillis();

		while (line != null) {
			HashMap<String, Object> info_map = extractLogToJson(line);
			DBObject object = new BasicDBObject(info_map);
			// object.put("id", "123");
			MongoUtil.collection.insert(object);

			line = br.readLine();
			count++;
			if (count == 10000) {
				Long end = System.currentTimeMillis();
				LOG.info("Mongo insert 10000 items  successfully spend " + (end - start) + "ms");
				count = 0;
				start = System.currentTimeMillis();
			}
		}
		LOG.info(file_path + " 当前全部数据已经解析完成。");
	}

	public static HashMap<String, Object> extractLogToJson(String line) {

		HashMap<String, Object> info_map = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 2017-12-06 00:05:14
		try {
			int index_1 = line.indexOf("UpdateHistory");
			int index_docid = line.indexOf("- ", index_1) + 2;
			int index_hotParameter = line.indexOf("hotParameter");

			String part_1 = line.substring(index_docid, index_hotParameter - 1);
			int index_priorScore = line.indexOf("priorScore");
			String part_2 = line.substring(index_hotParameter, index_priorScore - 1);
			int index_qualityEvalLevel = line.indexOf("qualityEvalLevel");
			String part_3 = line.substring(index_priorScore, index_qualityEvalLevel - 1);
			String part_4 = line.substring(index_qualityEvalLevel);

			String[] part1s = part_1.split(",");
			info_map.put("docid", part1s[0]);
			info_map.put("simid", part1s[2]);
			info_map.put("updateTurns", Integer.parseInt(part1s[3].substring(part1s[3].indexOf("=") + 1)));
			info_map.put("unUpdateTurns", Integer.parseInt(part1s[4].substring(part1s[4].indexOf("=") + 1)));

			info_map.put("hotParameter", part_2.substring(13));

			String[] part3s = part_3.split(",");
			info_map.put("priorScore", Double.parseDouble(part3s[0].substring(part3s[0].indexOf("=") + 1)));
			info_map.put("globalScore", Double.parseDouble(part3s[1].substring(part3s[1].indexOf("=") + 1)));

			info_map.put("verticalScore",
					URLEncoder.encode(part_3.substring(part_3.indexOf("verticalScore") + 14), "UTF-8"));

			String[] part4s = part_4.split(",");
			info_map.put("qualityEvalLevel", Double.parseDouble(part4s[0].substring(part4s[0].indexOf("=") + 1)));
			info_map.put("sourceEvalLevel", part4s[1].substring(part4s[1].indexOf("=") + 1));
			info_map.put("isBills", Boolean.parseBoolean(part4s[2].substring(part4s[2].indexOf("=") + 1)));

			info_map.put("time", sdf.parse(line.substring(line.indexOf("[") + 1, line.indexOf(","))));
		} catch (Exception e) {
			LOG.info("source log extract wrong : " + line, e);

		}

		return info_map;
	}

}
