package com.ifeng.liulu.hotboostLog;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.client.model.DBCollectionFindOptions;

public class QueryHotBoots {
	protected static final Log LOG = LogFactory.getLog(QueryHotBoots.class);

	public static void query(String value) {
		MongoUtil.getConnect();
		MongoUtil.getConnect();
		String key = "docid";

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
		String key = "docid";
		String value = "39226994";

		Gson gson = new Gson();
		DBObject query = new BasicDBObject(key, value);
		ReadPreference preference = ReadPreference.secondary();
		DBCollectionFindOptions options = new DBCollectionFindOptions();
		options.readPreference(preference);
		DBCursor cursor = MongoUtil.collection.find(query, options);
		// DBCursor cursor = new DBCursor(MongoUtil.collection, null, null, null);
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				HotboostLogInfo info = transfer(dbObject);
				String json = gson.toJson(info);
				sb.append(json + ",");
			}

			sb.setCharAt(sb.lastIndexOf(","), ']');

		}
		System.out.println("************************************");
		// JSONArray myJsonArray = JSONArray.fromObject(sb.toString());
		System.out.println(sb.toString());

		// System.out.println(MongoUtil.collection.getIndexInfo());

		/*
		 * DBObject query = new BasicDBObject(key, value); ReadPreference preference =
		 * ReadPreference.secondary(); DBCursor cursor =
		 * MongoUtil.collection.find(query,null,preference); ReadPreference rp =
		 * cursor.getReadPreference(); while (cursor.hasNext()) { DBObject object =
		 * cursor.next(); System.out.println(object.toString()); }
		 */
	}

	public static String queryDocid(String value) {
		String key = "docid";
		Gson gson = new Gson();
		DBObject query = new BasicDBObject(key, value);
		ReadPreference preference = ReadPreference.secondary();
		DBCollectionFindOptions options = new DBCollectionFindOptions();
		options.readPreference(preference);
		DBCursor cursor = MongoUtil.collection.find(query, options);
		// DBCursor cursor = new DBCursor(MongoUtil.collection, null, null, null);
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		if (cursor != null) {
			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				HotboostLogInfo info = transfer(dbObject);
				String json = gson.toJson(info);
				sb.append(json + ",");
			}

			sb.setCharAt(sb.lastIndexOf(","), ']');

		}
		return sb.toString();
	}

	public static HotboostLogInfo transfer(DBObject dbObject) {
		HotboostLogInfo info = new HotboostLogInfo();
		info.setSimid((String) dbObject.get("simid"));
		info.setGlobalScore((Double) dbObject.get("globalScore"));
		info.setHotParameter((String) dbObject.get("hotParameter"));
		info.setIsBills((Boolean) dbObject.get("isBills"));
		info.setPriorScore((Double) dbObject.get("priorScore"));
		info.setQualityEvalLevel((Double) dbObject.get("qualityEvalLevel"));
		info.setSourceEvalLevel((String) dbObject.get("sourceEvalLevel"));
		info.setTime((Date) dbObject.get("time"));
		info.setUnUpdateTurns((Integer) dbObject.get("unUpdateTurns"));
		info.setUpdateTurns((Integer) dbObject.get("updateTurns"));
		info.setVerticalScore((String) dbObject.get("verticalScore"));

		return info;
		// String simid=(String) dbObject.get("simid");
		// Double globalScore=(Double) dbObject.get("globalScore");
		// Boolean isBills=(Boolean) dbObject.get("isBills");
		// String sourceEvalLevel=(String) dbObject.get("sourceEvalLevel");
		// Date time=(Date) dbObject.get("time");
		// Integer updateTurns=(Integer) dbObject.get("updateTurns");
		// Integer unUpdateTurns=(Integer) dbObject.get("unUpdateTurns");
		// Double priorScore=(Double) dbObject.get("priorScore");
		// String hotParameter=(String) dbObject.get("hotParameter");
		// String verticalScore=(String) dbObject.get("verticalScore");
		// Double qualityEvalLevel=(Double) dbObject.get("qualityEvalLevel");

	}
}
