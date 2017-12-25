/**
 * 
 */
package com.ifeng.liulu.applist;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.ifeng.liulu.util.HttpUtils;

import parquet.org.slf4j.Logger;
import parquet.org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author liulu5
 *
 * @descreption ����"10.90.9.71"����13
 *              ��key���û�ID������value�����û���APPList�������г��ֹ���APPList������Ϣ��ȡ��
 *              ��ȡ��ΪHttpUtils ��ȡ����APP��Ϣ�洢��"10.90.9.71"����2�� keyΪAPP����
 *              "10.90.9.71"auth("6i6FxegQb8FyPqypS7pM");
 */
public class CrawlAppDetailTask {
	private static Jedis jedisFrom;
	private static Jedis jedisTo;
	private static int UappCount = 0;// ��¼���е��ڼ���app��
	private static int UserCount = 0;// ��¼���е��ڼ���User��
	public static String ask_url = "http://sj.qq.com/myapp/searchAjax.htm?kw=";
	protected static final Logger LOG = LoggerFactory.getLogger(CrawlAppDetailTask.class);
	private static int appcount = 1;// ������˿ͻ����û���appӵ����������app���м���

	public static void getAppDetailToJedis(String appName, Set<String> successSet, List<String> failList) {

		try {
			if (!successSet.contains(appName)) { // ˵��jedis����û�С�������ֱ�Ӽ�1
				LOG.info("begin to deal with " + appName);

				String back = HttpUtils.downloadPageUseInputStream(ask_url + URLEncoder.encode(appName, "utf-8"), null,
						null, 3000, 3000);
				String appDetail = extractAppDetail(back);

				if (appDetail == null) {
					failList.add(appName);
					LOG.info(appName + " fail,to failList");
				} else {
					jedisTo.set(appName, 1 + "#" + appDetail);
					successSet.add(appName);
					LOG.info(appName + " success to jedis");
				}
				if (failList.contains(appName)) {
					// ���failList�д����Ǹ�name�Ļ������Ƴ������ڳ�������failList�Ķ�����һ��ͺ���
					failList.remove(appName);
				}
			} else {// jedis�����У�����Ҫȡ��������appcount
				LOG.info(appName + " have been dealt with ,increase appcount and go on ");
				String count_detail = jedisTo.get(appName);
				int index = count_detail.indexOf("#");
				appcount = Integer.parseInt(count_detail.substring(0, index)) + 1;
				jedisTo.set(appName, appcount + "#" + count_detail.substring(index + 1));
			}
		} catch (Exception e) {
			failList.add(appName);
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Gson gson = new Gson();
		List<String> failList = new ArrayList<>();
		Set<String> successSet = new HashSet<>();
		initJedis();

		Set<String> uid_set = jedisFrom.keys("*");
		Iterator itofUserID = uid_set.iterator();

		successSet = jedisTo.keys("*");

		while (itofUserID.hasNext()) {
			try {
				String key = (String) itofUserID.next();
				String value = jedisFrom.get(key);// value��json����
				LOG.info("No." + UserCount + "'s applist:" + ":" + value);
				String[] app_names = value.split("\\[|\\,|\\]");// String[] app_names=gson.fromJson(value,
																// String[].class);���� ���� "[��������, ֧����, WPS Office]"
																// �м���пո�Ļ�����쳣
				for (int i = 1; i < app_names.length; i++) {
					String appName = app_names[i].trim();
					getAppDetailToJedis(appName, successSet, failList);
				}
				UappCount++;
				LOG.info(" have finished NO." + UappCount + " APP");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			UserCount++;
			LOG.info(" have finished NO." + UserCount + " User");
			haveARest();

		}

		LOG.info(" begin to deal with appname in failList,SIZE=" + failList.size());
		// ������֮�󣬽���failList�е�

		for (int i = 0; i < failList.size(); i++) {
			String name = failList.get(i);
			String back;
			LOG.info(failList.get(i));
			try {
				back = HttpUtils.downloadPageUseInputStream(ask_url + URLEncoder.encode(name, "utf-8"), null, null,
						3000, 3000);
				String appDetail = extractAppDetail(back);
				if (appDetail == null) {
					LOG.info(" failList deal with " + name + " fail");

				} else {
					LOG.info(" failList deal with " + name + " success");
					jedisTo.set(name, 1 + "#" + appDetail);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOG.info(" end to deal with appname in failList,SIZE=" + failList.size());
		}

		closeJedis();
	}

	public static String extractAppDetail(String parse) throws Exception {
		String regex = "\"appDetail\":(.*?),\"downloadRateDesc\"";// ������ʹ�÷�̰��ģʽ��
		Matcher matcher = Pattern.compile(regex).matcher(parse);
		if (matcher.find()) {
			String ret = matcher.group(1);
			return ret;
		} else {
			return null;
		}
	}

	private static void haveARest() {
		if (UappCount % 100 == 0)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private static void initJedis() {
		jedisFrom = new Jedis("10.90.9.71", 6379, 100000);
		jedisFrom.auth("6i6FxegQb8FyPqypS7pM");
		jedisFrom.select(13);

		jedisTo = new Jedis("10.90.9.71", 6379, 100000);
		jedisTo.auth("6i6FxegQb8FyPqypS7pM");
		jedisTo.select(2);
	}

	private static void closeJedis() {
		jedisFrom.close();
		jedisTo.close();

	}
}
