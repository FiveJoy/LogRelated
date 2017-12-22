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
	 * WAIT_FOR_UPDATE sleepʱ�� ���ã��ȴ���ǰ�ļ����£��ļ�60minд��һ�Ρ�д�ļ���Ż���5����
	 */
	private static final int WAIT_FOR_UPDATE = 1000 * 60 * 60;////
	private static final int WAIT_FOR_UPDATE_forlog = 60;// min
	/*
	 * WAIT_FOR_GENER_TIMES
	 * ���ȴ�����������ļ������ڣ����᲻����ϢWAIT_FOR_UPDATE��Щ���ӣ������������»�û���ɣ��򱨾�����
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
	 * EXPIRETIME ��������ʱ�� �ݶ�30day --60s*60*30
	 */
	private static int EXPIRETIME = 60 * 60 * 30;

	/*
	 * ͨ�����������ȡ��ȡ��ʼ���ڵ��ļ�
	 */
	public static boolean initLogDate(String[] dateProps) {
		if (dateProps.length == 0) {
			// Ĭ��ȡ��ǰ����
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
		while (!END_MARK) {// ���û�н����Ͳ�ֹͣ��
			long start = System.currentTimeMillis();

			try {
				String remain = PrepareToReadLogFile();
				if (remain == null)
					continue;
				else {
					// �Ѹոն���������һ�д����ȡ����
					beginToRead(remain);
				}
				Thread.sleep(WAIT_FOR_UPDATE);// ��Ϣ30min���ȴ���ǰ�ļ�����

				// ���cur_date��ǰһ�죬��ʱ�Ѿ��ǽ�����賿��˵���ļ��Ѿ�����
				if (isSpecialTime(cur_date)) {
					if (br != null) {
						br.close();
						br = null;
					}
					if (fr != null) {
						fr.close();
						fr = null;
					}
					// ��ʱ��
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
		// �����ǣ���ǰ�����cur_dateΪϵͳʱ���ǰһ�죬�ҵ���ϵͳʱ�����賿5��֮ǰ----�ر���=null
		Calendar ca = Calendar.getInstance();// �õ�һ��Calendar��ʵ��
		Date today_date = new Date();
		ca.setTime(today_date); // ����ʱ��Ϊ��ǰʱ��
		ca.add(Calendar.DAY_OF_MONTH, -1); // ��1
		Date lastDay = ca.getTime(); // ���
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
		// ����������ڵȴ�120min�Ժ���ֵ�
		// һ���棬����ļ������ˣ���ʾ���ǵ����û����á�
		// ��һ�������û���£�������ļ��Ƿ���ڣ�������ڵĻ�����
		// �ȼ���ļ��Ƿ���ڡ�
		// if�ļ�û�䣬�Ͳ�new�ˣ����˵Ļ���Newһ�¡�
		// prepare��������׼���ļ��ģ�׼�����˾��˳���û׼���þͽ������ô����޷��ǵ�һ�ȡ�

		// ��ȡ��ǰʱ��
		String file_name_suffix = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date _today_date = new Date();
		String today_date = sdf.format(_today_date);
		Date _cur_date = sdf.parse(cur_date);
		if (_cur_date.before(_today_date) && !cur_date.equals(today_date)) {// ˵����֮ǰ����־����׺����
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

				// �ļ������ڣ������ǻ�û���ɣ���Ϣ
				LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep " + WAIT_FOR_UPDATE_forlog
						+ "min ,Ŀǰ�Ѿ��ȴ�" + listener + "��" + WAIT_FOR_UPDATE_forlog + "min");
				Thread.sleep(WAIT_FOR_UPDATE_forlog);
				if (listener > WAIT_FOR_GENER_TIMES) {
					LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep " + WAIT_FOR_UPDATE_forlog
							+ "min ,Ŀǰ�Ѿ��ȴ�" + listener + "��" + WAIT_FOR_UPDATE_forlog + "min");
					LOG.info("�ȴ�ʱ����ã��������ʼ�����");
					sendAlarmEmail(file_path);
				}
				listener++;
			}
		}
		if (fr == null) {
			// ��ʼ�����µ���־�ļ�
			LOG.info("prepare tools of extract...");
			fr = new FileReader(logfile);
			br = new BufferedReader(fr);
			line = br.readLine();
		} else {
			line = br.readLine();
			if (line != null) {
				// ˵�����ļ�������־������Ҫ����
				LOG.info("the file of " + cur_date + " has been updated so go on");
			} else {
				// ˵�����������Ѿ�ȫ��д����ɣ����ļ����ٸ���
				LOG.info(file_path + "'s invalid lines count=" + invalid);
				br.close();
				fr.close();
				br = null;
				fr = null;
				LOG.info("the file of " + cur_date + " has been extracted!next!");
				// ��ʱ��
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
		// ��������
		String alarm_info = LoadConfig.lookUpValueByKey("alarm_info");
		// ��������
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
		LOG.info(file_path + " ��ǰȫ�������Ѿ�������ɡ�");
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
