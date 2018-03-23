package com.gionee.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.gionee.gnif.util.PropertiesConfig;

@SuppressWarnings("rawtypes")
public class RedisMap implements Map {
	
	private static Logger logger = Logger.getLogger(RedisMap.class);
	
	private static int redisDatabaseNum;
	private static String hosts;
	private static int port;
	
	private static JedisPool cachePool;
	
	static {
		redisDatabaseNum = PropertiesConfig.getInteger("redis.database_num");
		hosts = PropertiesConfig.getString("redis.hosts");
		port = PropertiesConfig.getInteger("redis.port");
		cachePool = new JedisPool(new JedisPoolConfig(), hosts, port);
	}

	private Integer expireSecond = 3600;
	
	public void setExpireSecond(Integer expireSecond) {
		this.expireSecond  = expireSecond;
	}

	@Override
	public Object get(Object key) {
		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);
		
		Object object = null;
		ObjectInputStream ois = null;
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(jedis.get(key.toString().getBytes()));
			ois = new ObjectInputStream(bais);
			object = ois.readObject();
		}catch(Exception e){
			logger.error("getting captcha to redis error.");
		}finally{
			try{
				if(null!=ois)  ois.close();
			}catch(Exception e){
				logger.error("ois closing error when getting ticket to redis.");
			}
		}
		
		cachePool.returnResource(jedis);
		return object;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}

		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		boolean ret = jedis.exists(key.toString().getBytes());

		cachePool.returnResource(jedis);

		return ret;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public Object put(Object key, Object value) {
		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try{
			oos = new ObjectOutputStream(bos);
			oos.writeObject(value);
		}catch(Exception e){
			logger.error("adding ticket to redis error.");
		}finally{
			try{
				if(null!=oos) oos.close();
			}catch(Exception e){
				logger.error("oos closing error when adding ticket to redis.");
			}
		}
		
		jedis.set(key.toString().getBytes(), bos.toByteArray());
		jedis.expire(key.toString().getBytes(), expireSecond);

		cachePool.returnResource(jedis);
		return value;
	}

	@Override
	public Object remove(Object key) {
		if (key == null) {
			return null;
		}

		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		jedis.del(key.toString().getBytes());

		cachePool.returnResource(jedis);

		return null;
	}

	@Override
	public void putAll(Map m) {
		
	}

	@Override
	public void clear() {
		
	}

	@Override
	public Set keySet() {
		return null;
	}

	@Override
	public Collection values() {
		return null;
	}

	@Override
	public Set entrySet() {
		return null;
	}

}
