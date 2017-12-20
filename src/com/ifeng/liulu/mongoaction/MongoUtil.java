package com.ifeng.liulu.mongoaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.liulu.log.StartLogProcess;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoUtil {

	private static final Log LOG = LogFactory.getLog(StartLogProcess.class);
	static {
		initMongo();
	}

	private static void initMongo() {

	}

	public static MongoClient client = null;
	public static DB mongoDatabase = null;
	public static DBCollection collection = null; // 等同于SQL中的表
	public static String dbName = "hotboosthistory";// 等同于SQL中的数据库
	public static String colName = "hotboost0";

	/**
	 * 获得链接
	 * 
	 * @param ServerIP
	 * @param ServerPort
	 * @param DBName
	 * @param collectionName
	 */
	public static void getConnect() {
		// 添加主从节点
		List<ServerAddress> addresses = new ArrayList<ServerAddress>();
		ServerAddress hostAddr = new ServerAddress("10.80.2.150", 10001);
		ServerAddress slaveAddr1 = new ServerAddress("10.80.3.150", 10001);
		ServerAddress slaveAddr2 = new ServerAddress("10.80.4.150", 10001);
		addresses.add(hostAddr);
		addresses.add(slaveAddr1);
		addresses.add(slaveAddr2);

		// 添加认证
		List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
		MongoCredential credential = MongoCredential.createCredential("root", "admin", "!@#$%^&*".toCharArray());
		credentialList.add(credential);

		// 设置client
		client = new MongoClient(addresses, credentialList);

		// 设置数据库
		mongoDatabase = client.getDB(dbName);

		// 设置collection
		collection = mongoDatabase.getCollection(colName);
		LOG.info("Mongo connect successfully.");
	}

}
