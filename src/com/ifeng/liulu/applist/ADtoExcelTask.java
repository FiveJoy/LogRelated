/**
 * 
 */
package com.ifeng.liulu.applist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.TextUtils;

import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

/**
 * @author liulu5
 *
 * @descreption 将存储在redis的APP细节转存到文本文件
 */
public class ADtoExcelTask {
	// 将APP名称，APP描述、类别ID、类别名称、作者名称，作者对产品介绍、APK发布时间、APPTags，下载量
	// 在转回
	private static Jedis jedis;
	private static String PLACE = "$";

	private static FileOutputStream fos;
	// private static FileOutputStream foscut;
	private static int filecutNum = 9;

	public static void main(String[] args) {

		initJedis();

		jedis.select(2);
		Set<String> app_set = jedis.keys("*");
		HashMap<String, String> map = new HashMap<>();
		int count = 0;
		Iterator it = app_set.iterator();
		String excelHead = "APP名称" + "\t" + "APP描述" + "\t" + "APP类别号" + "\t" + "APP类别" + "\t" + "APP作者" + "\t"
				+ "APP作者描述" + "\t" + "APP发布时间" + "\t" + "APP下载量" + "\t" + "凤凰新闻客户端用户对该APP的使用人数";

		try {
			fos = new FileOutputStream(new File("E:\\data\\appdetailALL.txt"), true);
			fos.write(excelHead.getBytes());
			fos.write("\r\n".getBytes());
			/*
			 * for(int i=0;i<filecutNum;i++) { foscut[i]=new FileOutputStream(new
			 * File("E:\\data\\appdetail"+i+".txt"),true);
			 * foscut[i].write(excelHead.getBytes()); foscut[i].write("\r\n".getBytes()); }
			 */

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (it.hasNext()) {
			try {

				String value = jedis.get((String) it.next());
				int index = value.indexOf("#");

				// (!=null)?():"#";
				Gson gson = new Gson();
				String app_use_count = value.substring(0, index);
				AppDetail appDetail = gson.fromJson(value.substring(index + 1), AppDetail.class);
				String app_name = (appDetail.getAppName() != null) ? (appDetail.getAppName()) : PLACE;
				String app_description = replaceBlank(
						(appDetail.getDescription() != null) ? (appDetail.getDescription()) : PLACE);
				String app_categoryId = (appDetail.getCategoryId() != null) ? (appDetail.getCategoryId()) : PLACE;
				String app_categoryName = replaceBlank(
						(appDetail.getCategoryName() != null) ? (appDetail.getCategoryName()) : PLACE);
				String app_authorName = (appDetail.getAuthorName() != null) ? (appDetail.getAuthorName()) : "#";
				String app_editorIntro = replaceBlank(
						(appDetail.getEditorIntro() != null) ? (appDetail.getEditorIntro()) : PLACE);
				String app_apkPublishTime = TimeStamp2Date(
						(appDetail.getApkPublishTime() != null) ? (appDetail.getApkPublishTime()) : PLACE,
						"yyyy-MM-dd HH:mm:ss");
				String app_appTags = (appDetail.getAppTags() != null) ? (appDetail.getAppTags()) : PLACE;
				String app_downloadcount = appDetail.getAppDownCount();

				count++;

				System.out.println("NO." + count + " " + app_name);
				String app_detail = null;
				if (map.containsKey(app_name)) {
					String[] detail = map.get(app_name).split("\t");
					app_use_count = String.valueOf(Integer.valueOf(detail[8]) + Integer.valueOf(app_use_count));
					app_detail = app_name + "\t" + app_description + "\t" + app_categoryId + "\t" + app_categoryName
							+ "\t" + app_authorName + "\t" + app_editorIntro + "\t" + app_apkPublishTime + "\t"
							+ app_downloadcount + "\t" + app_use_count;
				} else {
					app_detail = app_name + "\t" + app_description + "\t" + app_categoryId + "\t" + app_categoryName
							+ "\t" + app_authorName + "\t" + app_editorIntro + "\t" + app_apkPublishTime + "\t"
							+ app_downloadcount + "\t" + app_use_count;

				}
				map.put(app_name, app_detail);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// 遍历完redis并获取到redis,然后使用hashmap去重及使用数相加
		// 开始写入文件了
		Iterator<Entry<String, String>> map_it = map.entrySet().iterator();
		int filecut = 0;
		int pos = -1;
		System.out.println(map.size());
		while (map_it.hasNext()) {

			String detail = (String) map_it.next().getValue();
			filecut++;
			try {
				fos.write(detail.getBytes());
				fos.write("\r\n".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try {
			fos.close();

			// writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 使用scanner遍历redisDB中的app信息
	}

	public static void initJedis() {
		jedis = new Jedis("10.90.9.71", 6379, 100000);
		jedis.auth("6i6FxegQb8FyPqypS7pM");

	}

	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\r|\n|\t");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	public static String TimeStamp2Date(String timestampString, String formats) {
		if (timestampString == PLACE)
			return PLACE;
		if (TextUtils.isEmpty(formats))
			formats = "yyyy-MM-dd HH:mm:ss";
		Long timestamp = Long.parseLong(timestampString) * 1000;
		String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
		return date.substring(0, 10);
	}

}
