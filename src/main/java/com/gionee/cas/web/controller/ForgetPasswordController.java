package com.gionee.cas.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.gionee.cas.biz.service.MailService;
import com.gionee.hr.HrUserService;
import com.gionee.hr.model.HrUser;

public class ForgetPasswordController extends AbstractController {
	
	private HrUserService userService;
	
	private MailService mailService;
	
	public void setUserService(HrUserService userService) {
		this.userService = userService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if (request.getMethod().equalsIgnoreCase(METHOD_GET)) {
			return new ModelAndView("gnifForgetPassword");
		}
		else {
			Map<String, Object> model = new HashMap<String, Object>();
			StringBuilder errors = new StringBuilder();
			String account = request.getParameter("account");
			String email = request.getParameter("email");
			String service = request.getParameter("service");
			if (account == null || account.equals("")) {
				errors.append("账号不能为空");
			}
			if (email == null || email.equals("")) {
				if (errors.length()>0) {
					errors.append(",");
				}
				errors.append("邮箱不能为空");
			}
			if (errors.length()>0) {
				model.put("errors", errors.toString());
				return new ModelAndView("gnifForgetPassword", model);
			}
			HrUser user = userService.getByAccountForUaam(account);
			if (user == null || user.getAddress() == null || !email.equals(user.getAddress().getEmail())) {
				model.put("errors", "请输入正确的登录账号与邮箱");
				return new ModelAndView("gnifForgetPassword", model);
			}
			mailService.sendMailForGetPassword(account, email, service);
			return new ModelAndView("gnifForgetPasswordInfo");
		}
	}

}
