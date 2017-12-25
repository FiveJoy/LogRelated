package com.ifeng.yaokai.usercenter;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;


/**
 * 只读权限请求文章交互信息基础数据
 * 查询内容以simid为key
 * @author zhangyang6
 *
 */
public class UsercenterCatchClient {
	private static final Log log = LogFactory.getLog(UsercenterCatchClient.class);
	protected static ReentrantLock lockPool = new ReentrantLock();  
    protected static ReentrantLock lockJedis = new ReentrantLock();  
  //业务前缀
  	private static String rowKeyHead = "uscatch_";
    
    public static ShardedJedisPool jedisPool_slave = null;
    
	/**
	 * @Title: redisPoolInit
	 * @Description: 初始化jedisPool
	 * @author liu_yi
	 * @throws
	 */
	public static void redisPoolInit() {
		// 当前锁是否已经锁住?if锁住了，do nothing; else continue  
	    assert !lockPool.isHeldByCurrentThread(); 
		  
		try {
			String[] redis_slave_hosts = "10.90.9.72#10.90.9.73#10.90.9.74#10.90.9.75".split("#");
			String[] redis_slave_ports = "6379".split("#");
			
			List<JedisShardInfo> slave_shards = new ArrayList<JedisShardInfo>();
			slave_shards = buildJedisShard(redis_slave_hosts, redis_slave_ports);		
			
			JedisPoolConfig config = new JedisPoolConfig();
			// 是否启用后进先出, 默认true
			config.setLifo(true);
			 
			// 最大空闲连接数
			config.setMaxIdle(10);
			
			// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted), 如果超时就抛异常,小于零:阻塞不确定的时间
			config.setMaxWaitMillis(2000);
			 
			// 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
			config.setMinEvictableIdleTimeMillis(1800000);
			 
			// 最小空闲连接数, 默认0
			config.setMinIdle(0);
			 
			// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
			config.setNumTestsPerEvictionRun(3);
			 
			// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)   
			config.setSoftMinEvictableIdleTimeMillis(1800000);
			 
			// 在获取连接的时候检查有效性, 默认false
			config.setTestOnBorrow(false);
			config.setMaxTotal(50000);
			
			// 在空闲时检查有效性, 默认false
			config.setTestWhileIdle(false);
			
			log.info("usercenter build jedisPool_slave...");
			jedisPool_slave = new ShardedJedisPool(config, slave_shards, Hashing.MURMUR_HASH);
		} catch (Exception ex) {
			log.error("redisPoolInit failed#", ex);
		}
	}
	
	/**
	 * @Title: buildJedisShard
	 * @Description: 根据参数列表，构建shardInfo List
	 * @author liu_yi
	 * @param hosts hosts列表
	 * @param ports port列表
	 * @param shardName 分片名
	 * @return
	 * @throws
	 */
	public static List<JedisShardInfo> buildJedisShard(String[] hosts, String[] ports) {
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		
		// 这是个固定名字，否则主从不一致
		String shardName = "FIXEDNAME";
		
		for (int i = 0; i != hosts.length; i++) {
			String tempHost = hosts[i];
			for (int j =  0; j != ports.length; j++) {
				String tempPort = ports[j];
				int tempPortsNum = Integer.valueOf(tempPort);
				
				String tempJedisShardNodeName = shardName + "_" + tempHost + "_addFixedPort" + j;
				JedisShardInfo tempJedisShardNode = new JedisShardInfo(tempHost, tempPortsNum, tempJedisShardNodeName);
				shards.add(tempJedisShardNode);
			}
		}
		
		return shards;
	}
	
	/**
     * @Title: 获取jedis连接池
     * @Description: 获得jedis连接池
     * @return
     * @throws
     */
    public synchronized static ShardedJedisPool getJedisClientPool() {
    	ShardedJedisPool sjp = null;
    	
    	sjp = getJedisPoolSlave();
    	
        if (sjp == null) {
            redisPoolInit();
        }

        // 再检测一次看是否为空(可能初始化失败)
        if (sjp == null) {
            log.error("can not get jedis instance, init again..");
            redisPoolInit();
        }
        
        // 还是失败了
        if (sjp == null) {
        	log.error("can not get jedis instance");
        	return null;
        }
		return sjp;
    }
    
    /**
     * 根据连接池获取可用的jedis连接客户端
     * @param args
     */
    public synchronized static ShardedJedis getJedisClientFromPool(ShardedJedisPool sjp) {
    	if(sjp == null){
        	
    		sjp = getJedisPoolSlave();
        	
            if (sjp == null) {
                redisPoolInit();
            }
    	}
            
    	ShardedJedis shardJedis = null;

    	try {
    		shardJedis = sjp.getResource();
    	} catch (Exception e) {
    		log.error("got an exception:", e);
    	}

    	return shardJedis;
    }
    
    public static ShardedJedisPool getJedisPoolSlave() {
		 if (jedisPool_slave == null) {
	            redisPoolInit();
	     }
		 
		 return jedisPool_slave;
	}
	
	 /** 
     * 释放jedis资源 
     * @param jedis 
     */  
    public static void returnResource(final ShardedJedis jedis, ShardedJedisPool sjp) {  
        if (jedis != null && sjp !=null) {  
        	sjp.returnResourceObject(jedis);
        }  
    }
    
    public static void main(String[] args) {
    	ShardedJedisPool sjp = null;
    	ShardedJedis client = null;
		//示例
    	try {
    		sjp = getJedisClientPool();
    		client = getJedisClientFromPool(sjp);
    		
//    		String re = client.set("123", "Yes");
    		String re = client.get(rowKeyHead+"cmpp_010230042935014");
    		List<String> ex_feature = JsonUtils.fromJson(re, ArrayList.class);
    		String val=JsonUtils.toJson(ex_feature,ArrayList.class);
    		System.out.println(val);
    		
    		
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			returnResource(client, sjp);
			sjp.close();
		}
	}
}
