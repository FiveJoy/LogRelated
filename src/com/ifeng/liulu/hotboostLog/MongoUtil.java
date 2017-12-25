package com.ifeng.liulu.hotboostLog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoUtil {

	private static final Log LOG = LogFactory.getLog(MongoUtil.class);
	public static MongoClient client = null;
	public static DB mongoDatabase = null;
	public static DBCollection collection = null; // ��ͬ��SQL�еı�
	public static String dbName = "hotboosthistory";// ��ͬ��SQL�е����ݿ�
	public static String colName = "hotboost0";

	/**
	 * �������
	 * 
	 * @param ServerIP
	 * @param ServerPort
	 * @param DBName
	 * @param collectionName
	 */
	public static void getConnect() {
		// ������ӽڵ�
		List<ServerAddress> addresses = new ArrayList<ServerAddress>();
		ServerAddress hostAddr = new ServerAddress("10.80.2.150", 10001);
		ServerAddress slaveAddr1 = new ServerAddress("10.80.3.150", 10001);
		ServerAddress slaveAddr2 = new ServerAddress("10.80.4.150", 10001);
		addresses.add(hostAddr);
		addresses.add(slaveAddr1);
		addresses.add(slaveAddr2);

		// �����֤
		List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
		MongoCredential credential = MongoCredential.createCredential("root", "admin", "!@#$%^&*".toCharArray());
		credentialList.add(credential);

		// ����client
		client = new MongoClient(addresses, credentialList);

		// �������ݿ�
		mongoDatabase = client.getDB(dbName);

		// ����collection
		collection = mongoDatabase.getCollection(colName);
		LOG.info("Mongo connect successfully.");
	}

	/*
	 * ΪMongo��������,ÿ��collectionֻ��ִ��һ��
	 */
	public static void createIndex(String index_name) {

		// ����ʱ������-����30��ɾ��TTL
		/*
		 * int EXPIRETIME=60*60*30; DBObject index = new BasicDBObject(index_name, 1);
		 * DBObject expireTime = new BasicDBObject("expireAfterSeconds", EXPIRETIME);
		 * MongoUtil.collection.createIndex(index,expireTime);
		 */

		// ������ͨ����
		DBObject index = new BasicDBObject(index_name, 1);
		MongoUtil.collection.createIndex(index);
	}

	/**
	 * �ر�����(ȷ����ǰ���̲�����ʹ�ø����Ӳ�ʹ�ã�����ᱨ��) ʵ������²�����Ҫ��ȡ�÷��� ���ý��̽�������Զ��ͷ�����
	 */

	public static void close() {
		if (client != null) {
			client.close();
			client = null;
		}
	}
}
