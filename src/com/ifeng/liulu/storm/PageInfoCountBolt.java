package com.ifeng.liulu.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import com.ifeng.yaokai.usercenter.JsonUtils;
import com.ifeng.yaokai.usercenter.UsercenterCatchClient;

import parquet.org.slf4j.Logger;
import parquet.org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class PageInfoCountBolt implements IBasicBolt {

	protected static final Logger LOG = LoggerFactory.getLogger(PageInfoCountBolt.class);

	HashMap<String, String> exposed_keywords = null;

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
		LOG.info("shenkehuaiyi pageinfo没有执行");
		final Jedis jedis = new Jedis("10.90.9.71", 6379, 10000);
		// 点击特征�?
		String action_time = tuple.getStringByField("action_time");
		String _date = action_time.substring(0, 10);
		String content_id = tuple.getStringByField("content_id");
		LOG.info("piwrong---" + content_id);
		String redis_key = _date + "_pageinfo";
		String oldcount = "";
		// 获取关键词部�?
		String rowKeyHead = "uscatch_";
		ShardedJedisPool sjp = null;
		ShardedJedis client = null;
		// 示例
		try {

			jedis.auth("6i6FxegQb8FyPqypS7pM");
			jedis.select(0);// 默认数据库就�?0
			UsercenterCatchClient.redisPoolInit();
			sjp = UsercenterCatchClient.getJedisClientPool();
			client = UsercenterCatchClient.getJedisClientFromPool(sjp);
			String re = client.get(rowKeyHead + content_id);
			List<String> ex_feature = JsonUtils.fromJson(re, ArrayList.class);
			if (ex_feature == null)
				System.out.println("ex_feature in pageinfo 怎么就成了null???" + "content_id=" + content_id);
			else {
				for (int i = 0; i < ex_feature.size(); i = i + 3) {
					String keyword = ex_feature.get(i);
					String thiscount = StringManager.getAbs(ex_feature.get(i + 2));
					exposed_keywords = (HashMap<String, String>) jedis.hgetAll(redis_key);
					if (exposed_keywords == null)
						exposed_keywords = new HashMap<>();
					oldcount = exposed_keywords.get(keyword);
					if (oldcount == null) {
						oldcount = "0";
					}
					Float curcount = Float.valueOf(oldcount) + Float.valueOf(thiscount);
					exposed_keywords.put(keyword, curcount.toString());
					jedis.hmset(redis_key, exposed_keywords);
					////////////
					LOG.info("pageinfocount999!!!!!" + keyword + "(" + curcount + ")");
					///
				}

			}

		} catch (Exception e) {
			LOG.info("Log exposed_keywords page to Redis ERROR");
			e.printStackTrace();

		} finally {
			UsercenterCatchClient.returnResource(client, sjp);
			sjp.close();
			jedis.disconnect();
		}

	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1) {

	}

}
