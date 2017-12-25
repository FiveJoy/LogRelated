package com.ifeng.liulu.storm;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.ifeng.yaokai.usercenter.Transform;

import parquet.org.slf4j.Logger;
import parquet.org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class KeyWordsExtractBolt implements IBasicBolt {
	protected static final Logger LOG = LoggerFactory.getLogger(KeyWordsExtractBolt.class);
	private OutputCollector collector;
	private Transform transform;
	private Jedis jedis;
	private int nullUserLogCount = 0;
	int rightdata = 0;
	int userlog_null = 0;

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declareStream("page", new Fields("action_time", "content_id"));
		outputFieldsDeclarer.declareStream("pageinfo", new Fields("action_time", "content_id"));
		// declarer.declare(new Fields("action_time","content_id"));
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
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		LOG.info("keyWrodefajkm");
		int page_pageinfo_number = 0;
		String value = tuple.getStringByField("value");
		String[] userlogs = null;
		String[] details = null;
		String action_type = null;
		String content_id = null;
		String action_time_stamp = null;
		String action_time = null;
		try {
			userlogs = transform.listToStr(value).split("\n");
			for (int i = 0; i < userlogs.length; i++) {
				details = userlogs[i].split("\t");
				action_type = details[11];
				if (action_type.equals("page") || action_type.equals("pageinfo")) {
					String a = "sfdfafer";
					char s = (char) a.codePointAt(0);
					LOG.info("I want to see  " + content_id);
					LOG.info("I want to see type  " + action_type);
					LOG.info("I want to see detail[12]  " + details[12]);
					action_time = details[10].substring(0, 10);
					if (TimeUtil.isToday(action_time)) { // 表示该用户日志是当天�?
						if (action_type.equals("pageinfo")) {
							content_id = StringManager.getContentId(details[12], "pageinfo");
							LOG.info("five5joy9-pageinfo" + action_time + " 文章ID  " + details[12]);
							LOG.info("five5joy9-pageinfo" + action_time + " 文章ID  contentid=?" + content_id);
							LOG.info("pageinfodaodi-------------" + value);
							collector.emit("pageinfo", new Values(action_time, content_id));
						}

						else if (action_type.equals("page")) {
							content_id = StringManager.getContentId(details[12], "page");
							LOG.info("five5joy9-page" + action_time + "     文章ID" + details[12]);
							LOG.info("five5joy9-page" + action_time + " 文章ID  " + content_id);
							LOG.info("fivejoywrong----" + value);
							collector.emit("page", new Values(action_time, content_id));
						}
					} else
						continue;
				} else {
					page_pageinfo_number++;
					LOG.info("not page&pageinfo" + action_type);
				}
			}

			// System.out.println("--------------$$$$$$$$______----------************************************************************");
		} catch (Exception e) {
			LOG.error("tansform error " + value, e);
			System.out.println("-----------------flavor Exception--------------------" + value);
			// collector.ack(input);
			return;
		} catch (Throwable e) {
			LOG.error("tansform error " + value, e);
			System.out.println("-----------------flavor Throwable--------------------" + value);
			// collector.ack(input);
			return;
		}

	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1) {
		this.transform = new Transform();

	}

}
