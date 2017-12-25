package com.ifeng.liulu.storm;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import parquet.org.slf4j.Logger;
import parquet.org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class BenchMarkComputeBolt extends BaseRichBolt {
	protected static final Logger LOG = LoggerFactory.getLogger(BenchMarkComputeBolt.class);
	private OutputCollector collector;
	HashMap<String, String> bench_mark_map = null;

	@Override
	public void execute(Tuple tuple) {

		final Jedis jedis = new Jedis("10.90.9.71", 6379, 10000);// host,port,timeout
		String keyword = tuple.getStringByField("keyword");
		String _date = tuple.getStringByField("time");

		String page_redis_key = _date + "_page";
		String pageinfo_redis_key = _date + "_pageinfo";
		String benchmark_redis_key = _date + "_benchmark";
		String keyword_page_count = null;
		String keyword_pageinfo_count = null;
		Float keyword_bench_mark = (float) 0;
		try {
			// LOG.info("five5+++BenchMarkCountCompute");

			jedis.auth("6i6FxegQb8FyPqypS7pM");
			bench_mark_map = (HashMap<String, String>) jedis.hgetAll(benchmark_redis_key);
			keyword_page_count = jedis.hgetAll(page_redis_key).get(keyword);
			keyword_pageinfo_count = jedis.hgetAll(pageinfo_redis_key).get(keyword);
			keyword_bench_mark = Float.valueOf(keyword_page_count) / Float.valueOf(keyword_pageinfo_count);
			if (bench_mark_map == null)
				bench_mark_map = new HashMap<>();
			bench_mark_map.put(keyword, keyword_bench_mark.toString());
			jedis.hmset(benchmark_redis_key, bench_mark_map);
			////////////
			LOG.info("bench999             " + keyword + "(" + keyword_bench_mark.toString() + ")");
			///

		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			jedis.disconnect();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.storm.task.IBolt#prepare(java.util.Map,
	 * org.apache.storm.task.TopologyContext, org.apache.storm.task.OutputCollector)
	 */
	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.storm.topology.IComponent#declareOutputFields(org.apache.storm.
	 * topology.OutputFieldsDeclarer)
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub

	}

}
