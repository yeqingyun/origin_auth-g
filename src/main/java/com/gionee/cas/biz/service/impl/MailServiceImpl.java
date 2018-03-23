package com.gionee.cas.biz.service.impl;

import java.util.Map;

import com.gionee.cas.biz.service.MailService;
import com.gionee.gnif.mail.biz.model.MailSender;
import com.gionee.gnif.util.PropertiesConfig;

public class MailServiceImpl implements MailService {
	
	private MailSender mailSender;
	
	private String prefix = "ACCOUNT_";
	
	private Map cacheStore;
	
	private String basePath = PropertiesConfig.getString("base.path");
	
	private String code = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public void setCacheStore(Map cacheStore) {
		this.cacheStore = cacheStore;
	}
	
	public String getCode() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<100; i++) {
			int rand = (int)(Math.random()*100) % 62;
			sb.append(code.charAt(rand));
		}
		return sb.toString();
	}

	@Override
	public void sendMailForGetPassword(String account, String mailAddress, String url) {
		String code = getCode();
		cacheStore.put(prefix+account, code);
		if (url == null || url.equals("")) {
			url = basePath;
		}
		mailSender.sendMail(mailAddress, "请修改密码", "您好：\r\n　　您已经申请修改密码，请访问以下链接来完成修改密码功能：\r\n\r\n" + basePath + "setpswd?a=" + account + "&c=" + code + "&s=" + url);
	}

	@Override
	public boolean validateMail(String account, String code) {
		String cacheCode = (String) cacheStore.get(prefix+account);
		if (cacheCode == null || !cacheCode.equals(code)) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public void invalidate(String account) {
		cacheStore.remove(prefix+account);
	}

}
