package com.ifeng.liulu.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.liulu.mongoaction.MongoUtil;
import com.ifeng.liulu.util.LoadConfig;
import com.ifeng.liulu.util.TimeUtil;
import com.ifeng.zhangxc.mail.SendWebMail;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import redis.clients.jedis.Jedis;

public class LogProcessor {
	static String[] keys = { "docid", "simid", "updateTurns", "unUpdateTurns", "hotParameter", "priorScore",
			"globalScore", "verticalScore", "qualityEvalLevel", "sourceEvalLevel", "isBills", "time" };

	protected static final Log LOG = LogFactory.getLog(LogProcessor.class);
	private static Jedis jedis = null;
	private static final int WAIT_FOR_UPDATE = 1000 * 60 * 60 * 2;////
	// 休息120min，等待当前文件更新，文件不出意外会60min写入一次。写文件会花费多长时间啊，这个也应当算在这里面，
	// 其实不关心写入花费时间也可以。肯定会阻塞读取的。但是可能时间过长造成报警，先写着看看吧
	private static final int WAIT_FOR_GENER_TIMES = 3;// 如果文件不存在，将会不断休息30min，如果这个次数下还没生成，则报警。
	private static String start_date = "";
	private static String end_date = "";
	private static String cur_date = "";
	private static boolean END_MARK = false;

	private static FileReader fr = null;
	private static BufferedReader br = null;

	private static int invalid = 0;

	private static int EXPIRETIME = 60 * 60 * 30;// 数据留存时间 暂定30day --60s*60*30

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

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

		// 建立时间索引-设置30天删除TTL
		DBObject index = new BasicDBObject("time", 1);
		DBObject expireTime = new BasicDBObject("expireAfterSeconds", EXPIRETIME);

		// MongoUtil.collection.createIndex(index,
		// expireTime);-----此行代码只需执行一次，多次执行会报Create错

		// 开始读取
		// String file_path="updateHistory.log."+cur_date;
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

	private static String PrepareToReadLogFile() throws InterruptedException, IOException, ParseException {
		// 这个函数是在等待30min以后出现的
		// 一方面，如果文件更新了，表示还是当天的没处理好。
		// 另一方面如果没更新，检查新文件是否存在，如果存在的话返回

		// 先检查文件是否存在。

		// if文件没变，就不new了，变了的话就New一下。
		// 个人认为，prepare就是用来准备文件的，准备好了就退出，没准备好就解决，怎么解决无非是等一等。

		// 获取当前时间
		String file_name_suffix = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		if (!today.equals(cur_date))
			file_name_suffix = "." + cur_date;
		// 获取文件路径
		//

		LOG.info("prepare logfile of " + cur_date);
		String line = null;
		int listener = 0;
		// String
		// file_dir_path=LoadConfig.lookUpValueByKey("log_local_path");log_online_path
		String file_path = LoadConfig.lookUpValueByKey("log_online_path") + LoadConfig.lookUpValueByKey("file_name_pre")
				+ file_name_suffix;
		File logfile = new File(file_path);

		while (!logfile.exists()) {
			if (!logfile.exists()) {

				// 文件不存在，可能是还没生成，休息30min
				LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep 120min;目前已经等待" + listener
						+ "个120min");
				Thread.sleep(WAIT_FOR_UPDATE);
				if (listener > WAIT_FOR_GENER_TIMES) {
					LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep 120min;目前已经等待" + listener
							+ "个120min");
					LOG.info("等待时间过久，将发送邮件报警");
					sendAlarmEmail(file_path);
				}
				listener++;
			}
		}
		if (fr == null) {
			// 说明才刚刚开始准备，所以直接new即可
			LOG.info("prepare tools of extract...");
			fr = new FileReader(logfile);
			br = new BufferedReader(fr);
			line = br.readLine();
		} else {
			line = br.readLine();
			if (line != null) {
				// 说明文件里面还有日志！直接返回line交给下一个函数继续读取就好
				// 下一个函数一直读 读到line=null就睡觉。
				LOG.info("the file of " + cur_date + " has been updated so go on");

			} else {
				// 说明没有东西了直接换下一个文件就可以，更改curdate然后在外层对line是不是Null判断，如果是，就重新准备continue呗
				LOG.info(file_path + "'s invalid lines count=" + invalid);
				br.close();
				fr.close();

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
			if (count == 1000) {
				Long end = System.currentTimeMillis();
				LOG.info("Mongo insert 1000 items  successfully spend " + (end - start) / 1000 + "s");
				count = 0;
				start = System.currentTimeMillis();
			}
		}
		LOG.info("当前全部数据已经解析完成。");
	}

	public static HashMap<String, Object> extractLogToJson(String line) {

		HashMap<String, Object> info_map = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 2017-12-06 00:05:14
		try {
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
			LOG.info("source log extract wrong : " + line, e);

		}

		return info_map;
	}

}
