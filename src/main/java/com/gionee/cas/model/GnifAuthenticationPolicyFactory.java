package com.gionee.cas.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.cxf.common.util.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ContextualAuthenticationPolicy;
import org.jasig.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.jasig.cas.services.ServiceContext;
import org.jasig.cas.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class GnifAuthenticationPolicyFactory implements ContextualAuthenticationPolicyFactory<ServiceContext> {

	private static final String OLD_CAS_LOGIN_URL = "j_spring_cas_security_check";

	private static final String NEW_CAS_LOGIN_URL = "login/cas";
	
	private static final String PROXY_CAS_AUTHORITY = "/cas/authority";
	private static final String PROXY_CAS_SUFFIX = "/j_spring_cas_security_proxyreceptor";
	
	private HttpClient httpClient;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String uaam_url;

	private String uaam_user;

	private String uaam_password;

	private String uaam_db;

	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private String eoa_url;

	private String exclude_url;
	
	
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	private final List<String> legencyUrls = new ArrayList<String>();

	public void setEoa_url(String eoa_url) {
		this.eoa_url = eoa_url;
	}

	public void setUaam_url(String uaam_url) {
		this.uaam_url = uaam_url;
	}

	public void setUaam_user(String uaam_user) {
		this.uaam_user = uaam_user;
	}

	public void setUaam_password(String uaam_password) {
		this.uaam_password = uaam_password;
	}

	public void setUaam_db(String uaam_db) {
		this.uaam_db = uaam_db;
	}
	
	public void setLegencyUrls(String legencyUrls) {
		String[] urls = legencyUrls.split("[;,]+");
		for (String url : urls) {
			this.legencyUrls.add(url.trim());
		}
	}

	public final void setDataSource(final DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}

	public void setExclude_url(String exclude_url) {
		this.exclude_url = exclude_url;
	}

	@Override
	public ContextualAuthenticationPolicy<ServiceContext> createPolicy(final ServiceContext context) {
		return new ContextualAuthenticationPolicy<ServiceContext>() {

			@Override
			public ServiceContext getContext() {
				return context;
			}

			@Override
			public boolean isSatisfiedBy(final Authentication authentication) {
				String account = authentication.getPrincipal().getId();
				String url = context.getService().getId();
				try {
					URL proxyreceptorUrl = new URL(account);
					if(httpClient.isValidEndPoint(proxyreceptorUrl)){
						return true;
					}
				} catch (Exception e) {
					logger.info("account=  "+account+" message:"+e.getMessage());
				}
				
				// CAS Server版本升级后，登录登出的URL变化了，需要进行兼容老系统中的代码
				int gnifUrlIndex = -1;
				if (url.contains(OLD_CAS_LOGIN_URL)) {
					gnifUrlIndex = url.indexOf(OLD_CAS_LOGIN_URL);
				} else if (url.contains(NEW_CAS_LOGIN_URL)) {
					gnifUrlIndex = url.indexOf(NEW_CAS_LOGIN_URL);
				}

				if (gnifUrlIndex != -1) {
					url = url.substring(0, gnifUrlIndex);
				} else {
					for (String u : legencyUrls) {
						if (url.startsWith(u)) {
							url = u;
							break;
						}
					}
				}
				try {
					// 通过账号得到用户
					User user = jdbcTemplate.query("select Id as id,Login as account,IsInner as isInner from Usr where Login = ? and Status = 1",
							new ResultSetExtractor<User>() {

								@Override
								public User extractData(ResultSet rs) throws SQLException, DataAccessException {
									User user = new User();
									while (rs.next()) {
										user.setId(rs.getInt(1));
										user.setAccount(rs.getString(2));
										user.setIsInner(rs.getInt(3));
										break;
									}
									return user;
								}
							}, account);

					// 如果找不到用户，则验证不通过
					if (user == null) {
						return false;
					}

					// 分析url,对于eoa系统，只能让内部用户进入，如果是，则不需进行下一步验证
					if (eoa_url.equals(url)) {
						// isinner属性是否为1标示能否用户是否为内部用户
						if (1 == user.getIsInner().intValue())
							return true;
					}

					// 对于例外的系统，只要是内部用户，直接通过，不需要进行其他的验证
					if (!StringUtils.isEmpty(exclude_url) && exclude_url.contains(url) && 1 == user.getIsInner().intValue())
						return true;
					// 得到系统id
					String getAppIdFromUaamSql = "select id_ from " + uaam_db + ".dbo.uaam_app_ where url_ = ? and status_ != -1";
					if (logger.isInfoEnabled()) {
						logger.info("获取系统[" + url + "]的ID");
					}
					Integer appId = jdbcTemplate.queryForObject(getAppIdFromUaamSql, Integer.class, url);

					// 查询用户是否有对应的系统
					String uaamSql = "select count(*) from " + uaam_db + ".dbo.uc_user_app_ where user_id_ = ? and app_id_ = ? and status_ != -1";

					Integer count = jdbcTemplate.queryForObject(uaamSql, Integer.class, user.getId(), appId);
					if (count > 0) {

						if (logger.isInfoEnabled()) {
							logger.info("账号为：" + account + "的用户通过验证");
						}
						return true;
					} else {
						if (logger.isInfoEnabled()) {
							logger.info("账号为：" + account + "的用户无法通过验证");
						}
						return false;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (logger.isInfoEnabled()) {
						logger.info("账号为：" + account + "的用户无法通过验证");
					}
					return false;
				}
			}
		};
	}

}
