package com.gionee.cas.web.flow;

import java.security.GeneralSecurityException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gionee.cas.model.GnifCredential;


public class GnifQueryDatabaseAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
{
	private String sql;
	// 用于用户Id查询
	private String idQuerySql;
	
	private String uaam_url;
	
	private String uaam_user;
	
	private String uaam_password;
	
	private String uaam_db;
	
	 @NotNull
	 private JdbcTemplate jdbcTemplate;

    @NotNull
    private DataSource dataSource;
	    
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

	 public final void setDataSource(final DataSource dataSource) {
	        this.jdbcTemplate = new JdbcTemplate(dataSource);
	        this.dataSource = dataSource;
	    }

	    /**
	     * Method to return the jdbcTemplate.
	     *
	     * @return a fully created JdbcTemplate.
	     */
	    protected final JdbcTemplate getJdbcTemplate() {
	        return this.jdbcTemplate;
	    }

	    protected final DataSource getDataSource() {
	        return this.dataSource;
	    }
	/**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    @NotNull
    private PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    @NotNull
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /** {@inheritDoc} */
    @Override
    protected final HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, PreventedException {
        final GnifCredential userPass = (GnifCredential) credential;
        if (userPass.getUsername() == null) {
            throw new AccountNotFoundException("Username is null.");
        }
        final String transformedUsername = this.principalNameTransformer.transform(userPass.getUsername());
        if (transformedUsername == null) {
            throw new AccountNotFoundException("Transformed username is null.");
        }
        final Principal principal = authenticateUsernamePasswordInternal(
                transformedUsername,
                userPass.getPassword(),userPass.getService());
        return new HandlerResult(this, new BasicCredentialMetaData(credential), principal);
    }


    /**
     * Method to return the PasswordEncoder to be used to encode passwords.
     *
     * @return the PasswordEncoder associated with this class.
     */
    protected final PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    protected final PrincipalNameTransformer getPrincipalNameTransformer() {
        return this.principalNameTransformer;
    }

    /**
     * Sets the PasswordEncoder to be used with this class.
     *
     * @param passwordEncoder the PasswordEncoder to use when encoding
     * passwords.
     */
    public final void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public final void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    /**
     * @return True if credential is a {@link UsernamePasswordCredential}, false otherwise.
     */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof GnifCredential;
    }
    
	protected final Principal authenticateUsernamePasswordInternal(final String username, final String password,final String service)
            throws GeneralSecurityException, PreventedException 
    {
		// 首先验证密码的正确性
		Principal principal = authenticateUsernamePasswordInternal(username,password);
		
		// 密码正确，则进一步验证是否有系统权限
		if(principal != null)
		{
			try
			{
				// 首先通过用户账号得到用户的Id
				Integer userId = getJdbcTemplate().queryForObject(idQuerySql,  Integer.class,username);;
				
				// 通过service得到登陆系统的地址
				String url = service.substring(0,service.indexOf("/j_spring_cas_security_check"));
				
				// 通过service路径在uaam中得到用户的登陆的系统Id
				String getAppIdFromUaamSql = "select id_ from openrowset( 'SQLOLEDB ', '"+ uaam_url + "';'"+ uaam_user +"'; '"+ uaam_password +"',"+ uaam_db +".dbo.uaam_app_) where url_ =?";
				Integer appId = getJdbcTemplate().queryForObject(getAppIdFromUaamSql,  Integer.class, url);
				// 查询指定用户id和系统ID的映射是否在uaam中存在
				String uaamSql = "select count(*) from openrowset( 'SQLOLEDB ', '"+ uaam_url + "';'"+ uaam_user +"'; '"+ uaam_password +"',"+ uaam_db +".dbo.uc_user_app_) where user_id_ = ? and app_id_ = ? and status_ != -1";
				Integer count = getJdbcTemplate().queryForObject(uaamSql,  Integer.class, userId,appId);
				if(count > 0)
					return new SimplePrincipal(username);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new FailedLoginException("用户没有权限");
			}
		}
		
		throw new FailedLoginException("登陆失败");
		
    }

	protected final Principal authenticateUsernamePasswordInternal(final String username, final String password)
	            throws GeneralSecurityException, PreventedException {

	        final String encryptedPassword = this.getPasswordEncoder().encode(password);
	        try {
	            final String dbPassword = getJdbcTemplate().queryForObject(this.sql, String.class, username);
	            if (!dbPassword.equals(encryptedPassword)) {
	                throw new FailedLoginException("Password does not match value on record.");
	            }
	        } catch (final IncorrectResultSizeDataAccessException e) {
	        	e.printStackTrace();
	            if (e.getActualSize() == 0) {
	                throw new AccountNotFoundException(username + " not found with SQL query");
	            } else {
	                throw new FailedLoginException("Multiple records found for " + username);
	            }
	        } catch (final DataAccessException e) {
	        	e.printStackTrace();
	            throw new PreventedException("SQL exception while executing query for " + username, e);
	        }
	        return new SimplePrincipal(username);
	    }

	 public void setIdQuerySql(final String sql) {
	        this.idQuerySql = sql;
	    }
	 public void setSql(final String sql) {
	        this.sql = sql;
	    }
}
