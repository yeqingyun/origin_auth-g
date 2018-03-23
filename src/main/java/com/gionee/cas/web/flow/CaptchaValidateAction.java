package com.gionee.cas.web.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.RequestContext;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

public final class CaptchaValidateAction extends AbstractAction {
	
	private static final String AUTHENTICATION_FAILURE = "authenticationFailure";
	private ImageCaptchaService jcaptchaService;
	private String captchaValidationParameter = "_captcha_parameter";
	private List<IpFilter> ipcodes = new ArrayList<CaptchaValidateAction.IpFilter>();

	protected Event doExecute(final RequestContext context) {
		String captcha_response = context.getRequestParameters().get(captchaValidationParameter);
		FlowExecutionContext flowExecutionContext = context.getFlowExecutionContext();
		
		boolean valid = false;

		if (captcha_response != null) {
			// 大小写不敏感
			String inputCaptcha = captcha_response.toUpperCase();
			String id = WebUtils.getHttpServletRequest(context).getSession().getId();
			if (id != null) {
				try {
					valid = jcaptchaService.validateResponseForID(id, inputCaptcha).booleanValue();
				}
				catch (CaptchaServiceException cse) {
				}
			}
			if (!valid) {
				String remoteIp = getRemortIP(context);
				for (IpFilter ip:ipcodes) {
					// 大小写敏感
					if (captcha_response.equals(ip.getCode(remoteIp))) {
						valid = true;
						break;
					}
				}
			}
		}
		
		
		if (valid) {
			return success();
		}
		Map<String, Class<? extends Exception>> handlerErrors = new HashMap<String, Class<? extends Exception>>();
		handlerErrors.put("captchaNotValidExceptioon", CaptchaNotValidException.class);
		return new Event(this, AUTHENTICATION_FAILURE, new LocalAttributeMap("error", new AuthenticationException(handlerErrors)));
	}
	
	public String getRemortIP(RequestContext context) {
		HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr();
		}
		else {
			return request.getHeader("x-forwarded-for");
		}
	}

	public void setCaptchaService(ImageCaptchaService captchaService) {
		this.jcaptchaService = captchaService;
	}

	public void setCaptchaValidationParameter(String captchaValidationParameter) {
		this.captchaValidationParameter = captchaValidationParameter;
	}
	
	public void setStaticCaptchaCode(String code) {
		if (StringUtils.hasText(code)) {
			String[] codes = code.split(",");
			for (String c:codes) {
				String [] mv = c.split("[/:]");
				ipcodes.add(new IpFilter(mv[0].trim(), mv[1].trim(), mv[2].trim()));
			}
		}
	}
	
	private static class IpFilter {
		private String code;
		private Integer segment;
		private Integer offset;
		private IpFilter(String ip, String mask, String code) {
			offset = 32-Integer.valueOf(mask);
			segment = getIntIp(ip);
			segment >>>= offset;
			this.code = code;
		}
		
		private Integer getIntIp(String ip) {
			if (ip.indexOf(".") == -1) {
				return 0;
			}
			String[] ips = ip.split("\\.");
			Integer intIp = Integer.valueOf(ips[0]);
			intIp <<= 8;
			intIp += Integer.valueOf(ips[1]);
			intIp <<= 8;
			intIp += Integer.valueOf(ips[2]);
			intIp <<= 8;
			intIp += Integer.valueOf(ips[3]);
			return intIp;
		}
		
		private String getCode(String ip) {
			Integer intIp = getIntIp(ip);
			intIp >>>= offset;
			if ((intIp ^ segment) == 0) {
				return code;
			}
			else {
				return null;
			}
		}
	}
	
}

