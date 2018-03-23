package com.gionee.cas.web.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Locale;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.gionee.gnif.util.PropertiesConfig;
import com.octo.captcha.Captcha;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.captchastore.CaptchaStore;

public class SessionCaptchaStore implements CaptchaStore {
	
	private static final String CAPTCHA = "CAPTCHA_";

	private static Logger logger = Logger.getLogger(SessionCaptchaStore.class);
	
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

	private Integer expireSecond = 60;
	
	public void setExpireSecond(Integer expireSecond) {
		this.expireSecond  = expireSecond;
	}

	@Override
	public void cleanAndShutdown() {
		
	}

	@Override
	public void empty() {
		
	}

	@Override
	public Captcha getCaptcha(String id) throws CaptchaServiceException {
		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);
		
		Captcha captcha = null;
		ObjectInputStream ois = null;
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(jedis.get((CAPTCHA+id).getBytes()));
			ois = new ObjectInputStream(bais);
			captcha = (Captcha)ois.readObject();
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
		return captcha;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection getKeys() {
		return null;
	}

	@Override
	public Locale getLocale(String arg0) throws CaptchaServiceException {
		return null;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public boolean hasCaptcha(String id) {
		if (id == null) {
			return false;
		}

		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		boolean ret = jedis.exists((CAPTCHA+id).getBytes());

		cachePool.returnResource(jedis);

		return ret;
	}

	@Override
	public void initAndStart() {
		
	}

	@Override
	public boolean removeCaptcha(String id) {
		if (id == null) {
			return false;
		}

		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		jedis.del((CAPTCHA+id).getBytes());

		cachePool.returnResource(jedis);

		return true;
	}

	@Override
	public void storeCaptcha(String id, Captcha captcha)
			throws CaptchaServiceException {
		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try{
			oos = new ObjectOutputStream(bos);
			oos.writeObject(captcha);
		}catch(Exception e){
			logger.error("adding ticket to redis error.");
		}finally{
			try{
				if(null!=oos) oos.close();
			}catch(Exception e){
				logger.error("oos closing error when adding ticket to redis.");
			}
		}
		
		jedis.set((CAPTCHA+id).getBytes(), bos.toByteArray());
		jedis.expire((CAPTCHA+id).getBytes(), expireSecond);

		cachePool.returnResource(jedis);
	}

	@Override
	public void storeCaptcha(String id, Captcha captcha, Locale locale)
			throws CaptchaServiceException {
		storeCaptcha(id, captcha);
	}

}
