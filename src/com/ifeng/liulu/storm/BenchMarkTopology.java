package com.ifeng.liulu.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

import com.ifeng.kedm.storm.spout.GetKafkaSpout;
import com.ifeng.kedm.storm.spout.MyKafkaSpout;

import parquet.org.slf4j.Logger;
import parquet.org.slf4j.LoggerFactory;

/*目前有个�?求，�?要统计用户近五个小时基准�?
 * 基准�? �?有用户的点击特征词词/�?有用户的曝光特征词�??
 * 比如�?有用户点击明�?3次，曝光明星10次，基准0.3�?

可以在storm起个任务将用户的近五个小时的点击曝光的特征词提取出来存入redis�?
 并统计特征词对应的次数，同时算出基准
*/
public class BenchMarkTopology {
	protected static final Logger LOG = LoggerFactory.getLogger(BenchMarkTopology.class);

	public static void main(String[] args) throws Exception {

		String groupid = "benchmarkk";
		TopologyBuilder builder = new TopologyBuilder();
		MyKafkaSpout kafkaSpout = new MyKafkaSpout<>(
				GetKafkaSpout.getKafkaSpoutConfig(GetKafkaSpout.getKafkaSpoutStreams(), groupid));

		Config conf = new Config();
		conf.setDebug(true);// 设置Config.TOPOLOGY_DEBUG为true，每次从spout或�?�bolt发�?�元组，Storm都会写进日志，对调试比较有用，具体有啥用我也不知�?
		conf.setNumAckers(0);
		conf.setNumWorkers(20);
		conf.setMessageTimeoutSecs(60);

		builder.setSpout("userlog-reader", kafkaSpout);
		builder.setBolt("keywordsextract-bolt", new KeyWordsExtractBolt(), 1).shuffleGrouping("userlog-reader");
		builder.setBolt("pagecount-bolt", new PageCountBolt(), 1).shuffleGrouping("keywordsextract-bolt", "page");
		builder.setBolt("pageinfocount-bolt", new PageInfoCountBolt(), 1).shuffleGrouping("keywordsextract-bolt",
				"pageinfo");
		builder.setBolt("benchmark_bolt", new BenchMarkComputeBolt(), 1).shuffleGrouping("pagecount-bolt");
		// StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

		// StormSubmitter.submitTopologyWithProgressBar(args[0], conf,
		// builder.createTopology());
		LocalCluster cluster = new LocalCluster();
		StormTopology topology = builder.createTopology();
		cluster.submitTopology("jizhuncijisuan", conf, topology);
		Thread.sleep(60000);
		cluster.shutdown();
		System.out.println("win!");
	}

}
