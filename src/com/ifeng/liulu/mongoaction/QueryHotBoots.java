package com.ifeng.liulu.mongoaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.client.model.DBCollectionFindOptions;

public class QueryHotBoots {
	protected static final Log LOG = LogFactory.getLog(QueryHotBoots.class);

	public static void query(HashMap<String, Object> kvmap) {
		MongoUtil.getConnect();
		BasicDBObject query = new BasicDBObject();
		Iterator<Entry<String, Object>> iterator = (Iterator<Entry<String, Object>>) kvmap.entrySet();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			query.append(entry.getKey(), entry.getValue());
		}
		ReadPreference preference = ReadPreference.secondary();
		DBCollectionFindOptions options = new DBCollectionFindOptions();
		options.readPreference(preference);
		DBCursor cursor = MongoUtil.collection.find(query, options);
		while (cursor.hasNext()) {
			DBObject object = cursor.next();
			System.out.println(object.toString());
		}
	}

	public static void main(String[] args) {
		MongoUtil.getConnect();
		/*
		 * String key = "updateTurns";
		 * 
		 * BasicDBObject value = new BasicDBObject(); value.append("$gte", 20);
		 * value.append("$lte", 50);
		 */
		// String key = "docid";
		// String value = "41941181";
		String key = "isBills";
		boolean value = false;
		DBObject query = new BasicDBObject(key, value);
		ReadPreference preference = ReadPreference.secondary();
		DBCollectionFindOptions options = new DBCollectionFindOptions();
		options.readPreference(preference);
		DBCursor cursor = MongoUtil.collection.find(query, options);
		// DBCursor cursor = new DBCursor(MongoUtil.collection, null, null, null);
		if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject object = cursor.next();
				System.out.println(object.toString());
				LOG.info("??");
			}
			LOG.info("why");
		} else {
			LOG.info("null");
		}
		LOG.info("end");

		// System.out.println(MongoUtil.collection.getIndexInfo());

		/*
		 * DBObject query = new BasicDBObject(key, value); ReadPreference preference =
		 * ReadPreference.secondary(); DBCursor cursor =
		 * MongoUtil.collection.find(query,null,preference); ReadPreference rp =
		 * cursor.getReadPreference(); while (cursor.hasNext()) { DBObject object =
		 * cursor.next(); System.out.println(object.toString()); }
		 */
	}
}
