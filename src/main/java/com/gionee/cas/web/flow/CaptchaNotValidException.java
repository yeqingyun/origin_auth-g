package com.gionee.cas.web.flow;

import java.io.Serializable;

import javax.security.auth.login.AccountException;

public class CaptchaNotValidException extends AccountException implements Serializable {

    private static final long serialVersionUID = 5745711263227480194L;

    public CaptchaNotValidException() {
        super();
    }

    public CaptchaNotValidException(final String message) {
        super(message);
    }
    
}