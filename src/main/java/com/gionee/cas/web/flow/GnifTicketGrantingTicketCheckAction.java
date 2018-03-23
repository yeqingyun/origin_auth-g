package com.gionee.cas.web.flow;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class GnifTicketGrantingTicketCheckAction
{
	 /** TGT does not exist event ID={@value}. */
    public static final String NOT_EXISTS = "notExists";
    
    /** TGT invalid event ID={@value}. */
    public static final String INVALID = "invalid";

    /** TGT valid event ID={@value}. */
    public static final String VALID = "valid";

    /** Ticket registry searched for TGT by ID. */
    @NotNull
    private final TicketRegistry ticketRegistry;

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
     * Creates a new instance with the given ticket registry.
     *
     * @param registry Ticket registry to query for valid tickets.
     */
    public GnifTicketGrantingTicketCheckAction(final TicketRegistry registry) {
        this.ticketRegistry = registry;
    }

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     *
     * @return {@link #NOT_EXISTS}, {@link #INVALID}, or {@link #VALID}.
     */
    public Event checkValidity(final RequestContext requestContext) {
        final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (!StringUtils.hasText(tgtId)) {
            return new Event(this, NOT_EXISTS);
        }
        
        String code = requestContext.getRequestParameters().get("code");
        if(code == null)
        {
        	return new Event(this, INVALID);
        }
        else
        {
        	final Ticket ticket = this.ticketRegistry.getTicket(tgtId);
        	if(ticket == null || ticket.isExpired())
        	{
        		return new Event(this,INVALID);
        	}
        	else
        	{
        		try
    			{
        			// 得到用户账号
        			SimplePrincipal pri = (SimplePrincipal)ticket.getGrantingTicket().getAuthentication().getPrincipal();
        			System.out.println("00000000000000000000000000000000"+pri.getId());
    				// 首先通过用户账号得到用户的Id
    				Integer userId = getJdbcTemplate().queryForObject("select pwd as password from usr where login = ?",  Integer.class,pri.getId());;
    				
    				// 通过service路径在uaam中得到用户的登陆的系统Id
    				String getAppIdFromUaamSql = "select id_ from openrowset( 'SQLOLEDB ', '"+ uaam_url + "';'"+ uaam_user +"'; '"+ uaam_password +"',"+ uaam_db +".dbo.uaam_app_) where code_ =?";
    				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>"+userId);
    				Integer appId = getJdbcTemplate().queryForObject(getAppIdFromUaamSql,  Integer.class, code);
    				System.out.println("......................"+appId);
    				// 查询指定用户id和系统ID的映射是否在uaam中存在
    				String uaamSql = "select count(*) from openrowset( 'SQLOLEDB ', '"+ uaam_url + "';'"+ uaam_user +"'; '"+ uaam_password +"',"+ uaam_db +".dbo.uc_user_app_) where user_id_ = ? and app_id_ = ? and status_ != -1";
    				Integer count = getJdbcTemplate().queryForObject(uaamSql,  Integer.class, userId,appId);
    				if(count > 0)
    					return new Event(this,VALID);
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    				return new Event(this,INVALID);
    			}
        		return new Event(this,INVALID);
        	}
        }
    }
}
