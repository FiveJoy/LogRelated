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
	// ��Ϣ120min���ȴ���ǰ�ļ����£��ļ����������60minд��һ�Ρ�д�ļ��Ứ�Ѷ೤ʱ�䰡�����ҲӦ�����������棬
	// ��ʵ������д�뻨��ʱ��Ҳ���ԡ��϶���������ȡ�ġ����ǿ���ʱ�������ɱ�������д�ſ�����
	private static final int WAIT_FOR_GENER_TIMES = 3;// ����ļ������ڣ����᲻����Ϣ30min�������������»�û���ɣ��򱨾���
	private static String start_date = "";
	private static String end_date = "";
	private static String cur_date = "";
	private static boolean END_MARK = false;

	private static FileReader fr = null;
	private static BufferedReader br = null;

	private static int invalid = 0;

	private static int EXPIRETIME = 60 * 60 * 30;// ��������ʱ�� �ݶ�30day --60s*60*30

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

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

		// ����ʱ������-����30��ɾ��TTL
		DBObject index = new BasicDBObject("time", 1);
		DBObject expireTime = new BasicDBObject("expireAfterSeconds", EXPIRETIME);

		// MongoUtil.collection.createIndex(index,
		// expireTime);-----���д���ֻ��ִ��һ�Σ����ִ�лᱨCreate��

		// ��ʼ��ȡ
		// String file_path="updateHistory.log."+cur_date;
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
		// ����������ڵȴ�30min�Ժ���ֵ�
		// һ���棬����ļ������ˣ���ʾ���ǵ����û����á�
		// ��һ�������û���£�������ļ��Ƿ���ڣ�������ڵĻ�����

		// �ȼ���ļ��Ƿ���ڡ�

		// if�ļ�û�䣬�Ͳ�new�ˣ����˵Ļ���Newһ�¡�
		// ������Ϊ��prepare��������׼���ļ��ģ�׼�����˾��˳���û׼���þͽ������ô����޷��ǵ�һ�ȡ�

		// ��ȡ��ǰʱ��
		String file_name_suffix = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		if (!today.equals(cur_date))
			file_name_suffix = "." + cur_date;
		// ��ȡ�ļ�·��
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

				// �ļ������ڣ������ǻ�û���ɣ���Ϣ30min
				LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep 120min;Ŀǰ�Ѿ��ȴ�" + listener
						+ "��120min");
				Thread.sleep(WAIT_FOR_UPDATE);
				if (listener > WAIT_FOR_GENER_TIMES) {
					LOG.info(cur_date + " 's file path" + file_path + " is not existed,sleep 120min;Ŀǰ�Ѿ��ȴ�" + listener
							+ "��120min");
					LOG.info("�ȴ�ʱ����ã��������ʼ�����");
					sendAlarmEmail(file_path);
				}
				listener++;
			}
		}
		if (fr == null) {
			// ˵���Ÿոտ�ʼ׼��������ֱ��new����
			LOG.info("prepare tools of extract...");
			fr = new FileReader(logfile);
			br = new BufferedReader(fr);
			line = br.readLine();
		} else {
			line = br.readLine();
			if (line != null) {
				// ˵���ļ����滹����־��ֱ�ӷ���line������һ������������ȡ�ͺ�
				// ��һ������һֱ�� ����line=null��˯����
				LOG.info("the file of " + cur_date + " has been updated so go on");

			} else {
				// ˵��û�ж�����ֱ�ӻ���һ���ļ��Ϳ��ԣ�����curdateȻ��������line�ǲ���Null�жϣ�����ǣ�������׼��continue��
				LOG.info(file_path + "'s invalid lines count=" + invalid);
				br.close();
				fr.close();

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
			if (count == 1000) {
				Long end = System.currentTimeMillis();
				LOG.info("Mongo insert 1000 items  successfully spend " + (end - start) / 1000 + "s");
				count = 0;
				start = System.currentTimeMillis();
			}
		}
		LOG.info("��ǰȫ�������Ѿ�������ɡ�");
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
