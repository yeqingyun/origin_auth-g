package com.gionee.cas.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.gionee.cas.biz.service.MailService;
import com.gionee.gnif.util.GioneePasswordEncoder;
import com.gionee.hr.HrUserService;
import com.gionee.hr.model.HrUser;

public class ModifyPasswordController extends AbstractController {
	
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
		
		String code = request.getParameter("c");
		String account = request.getParameter("a");
		String service = request.getParameter("s");
		String password = request.getParameter("password");
		if (code == null || code.equals("") || account == null || account.equals("") || !mailService.validateMail(account, code)) {
			return new ModelAndView("gnifCodeInvalid");
		}
		
		Map<String, Object> model = new HashMap<String, Object>();
		if (request.getMethod().equalsIgnoreCase(METHOD_GET)) {
			model.put("account", account);
			model.put("code", code);
			model.put("service", service);
			return new ModelAndView("gnifModifyPassword", model);
		}
		else {
			if (password == null || password.equals("")) {
				return new ModelAndView("gnifCodeInvalid");
			}
			HrUser user = userService.getByAccount(account);
			user.setPassword(password);
			userService.update(user);
			mailService.invalidate(account);
			model.put("service", service);
			return new ModelAndView("gnifModifyPasswordEnd", model);
		}
	}

}
