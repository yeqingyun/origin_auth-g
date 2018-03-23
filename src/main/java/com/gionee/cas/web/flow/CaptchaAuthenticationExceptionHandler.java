package com.gionee.cas.web.flow;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.web.flow.AuthenticationExceptionHandler;
import org.springframework.beans.factory.InitializingBean;

public class CaptchaAuthenticationExceptionHandler extends AuthenticationExceptionHandler implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		List<Class<? extends Exception>> origErrors = new ArrayList<Class<? extends Exception>>(getErrors());
		origErrors.add(CaptchaNotValidException.class);
		origErrors.add(ServiceNotValideException.class);
		setErrors(origErrors);
	}

}
