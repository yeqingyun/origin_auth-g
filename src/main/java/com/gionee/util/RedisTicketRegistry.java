package com.gionee.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.gionee.gnif.util.PropertiesConfig;


/*
 *  TicketRegistry using Redis, to solve CAS Cluster.
 */

public class RedisTicketRegistry extends AbstractDistributedTicketRegistry {
	
	private static Logger logger = Logger.getLogger(RedisTicketRegistry.class);

	private static int redisDatabaseNum;
	private static String hosts;
	private static int port;
	private static int st_time;  //ST最大空闲时间
	private static int tgt_time; //TGT最大空闲时间

	private static JedisPool cachePool;

	static {
		redisDatabaseNum = PropertiesConfig.getInteger("redis.database_num");
		hosts = PropertiesConfig.getString("redis.hosts");
		port = PropertiesConfig.getInteger("redis.port");
		st_time = PropertiesConfig.getInteger("cas.st_time");
		tgt_time = PropertiesConfig.getInteger("cas.tgt_time");
		cachePool = new JedisPool(new JedisPoolConfig(), hosts, port);
	}

	public void addTicket(Ticket ticket) {

		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		int seconds = 0;

		String key = ticket.getId() ;

		if(ticket instanceof TicketGrantingTicket){
//			key = ((TicketGrantingTicket)ticket).getAuthentication().getPrincipal().getId();
			seconds = tgt_time/1000;
		}else{
			seconds = st_time/1000;
		}


		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try{
			oos = new ObjectOutputStream(bos);
			oos.writeObject(ticket);
		}catch(Exception e){
			logger.error("adding ticket to redis error.");
		}finally{
			try{
				if(null!=oos) oos.close();
			}catch(Exception e){
				logger.error("oos closing error when adding ticket to redis.");
			}
		}
		jedis.set(key.getBytes(), bos.toByteArray());
		jedis.expire(key.getBytes(), seconds);

		cachePool.returnResource(jedis);

	}

	public Ticket getTicket(final String ticketId) {
		return getProxiedTicketInstance(getRawTicket(ticketId));
	}


	private Ticket getRawTicket(final String ticketId) {

		if(null == ticketId) return null;

		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		Ticket ticket = null;

		ObjectInputStream ois = null;
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(jedis.get(ticketId.getBytes()));
			ois = new ObjectInputStream(bais);
			ticket = (Ticket)ois.readObject();
		}catch(Exception e){
			logger.error("getting ticket from redis error.");
		}finally{
			try{
				if(null!=ois)  ois.close();
			}catch(Exception e){
				logger.error("ois closing error when getting ticket from redis.");
			}
		}

		cachePool.returnResource(jedis);

		return ticket;
	}

	public boolean deleteTicket(final String ticketId) {

		if (ticketId == null) {
			return false;
		}


		Jedis jedis = cachePool.getResource();
		jedis.select(redisDatabaseNum);

		jedis.del(ticketId.getBytes());

		cachePool.returnResource(jedis);

		return true;
	}

	public Collection<Ticket> getTickets() {
		throw new UnsupportedOperationException("GetTickets not supported.");
	}

	protected boolean needsCallback() {
		return false;
	}

	protected void updateTicket(final Ticket ticket) {
		addTicket(ticket);
	}

}
