package com.ifeng.liulu.storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.ifeng.yaokai.usercenter.JsonUtils;
import com.ifeng.yaokai.usercenter.UsercenterCatchClient;

import parquet.org.slf4j.Logger;
import parquet.org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class PageCountBolt extends BaseRichBolt {
	protected static final Logger LOG = LoggerFactory.getLogger(PageCountBolt.class);
	private OutputCollector collector;
	HashMap<String, String> clicked_keywords = null;

	@Override
	public void execute(Tuple tuple) {
		final Jedis jedis = new Jedis("10.90.9.71", 6379, 10000);
		LOG.info("testif pagecount");
		// 点击特征�?
		String action_time = tuple.getStringByField("action_time");
		String _date = action_time.substring(0, 10);
		String content_id = tuple.getStringByField("content_id");
		String redis_key = _date + "_page";
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
			if (ex_feature != null) {
				for (int i = 0; i < ex_feature.size(); i = i + 3) {
					String keyword = ex_feature.get(i);
					String thiscount = StringManager.getAbs(ex_feature.get(i + 2));

					clicked_keywords = (HashMap<String, String>) jedis.hgetAll(redis_key);
					if (clicked_keywords == null) {
						clicked_keywords = new HashMap<>();
						System.out.println("没有取到的话你会看见这个");
					}

					oldcount = clicked_keywords.get(keyword);
					if (oldcount == null) {
						oldcount = "0";
					}
					Float curcount = Float.valueOf(oldcount) + Float.valueOf(thiscount);
					clicked_keywords.put(keyword, curcount.toString());
					////////////
					LOG.info("pagecount999!!!!" + keyword + "(" + curcount + ")");
					jedis.hmset(redis_key, clicked_keywords);
					collector.emit(new Values(keyword, _date));
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
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		collector = arg2;

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("keyword", "time"));
		// declarer.declareStream(new Fields("action_time","content_id"));
	}

}
