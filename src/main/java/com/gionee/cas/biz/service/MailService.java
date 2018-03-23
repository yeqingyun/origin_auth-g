package com.gionee.cas.biz.service;

public interface MailService {
	
	/**
	 * 发送修改密码的邮件.
	 * @param account
	 * @param mailAddress
	 * @param url TODO
	 */
	void sendMailForGetPassword(String account, String mailAddress, String url);
	
	/**
	 * 校验用户账号是否申请过要修改密码功能.
	 * @param account
	 * @param code
	 * @return
	 */
	boolean validateMail(String account, String code);
	
	/**
	 * 使得要修改密码的邮箱哦不能再修改密码，直到再一次通过忘记密码功能发起修改申请.
	 * @param account
	 */
	void invalidate(String account);

}
