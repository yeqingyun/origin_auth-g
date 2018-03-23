package com.gionee.cas.model;

import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;

public class GnifCredential extends RememberMeUsernamePasswordCredential
{
	private String service;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
	
}
