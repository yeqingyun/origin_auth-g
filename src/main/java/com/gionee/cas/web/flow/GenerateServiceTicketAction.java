package com.gionee.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class GenerateServiceTicketAction extends AbstractAction {
	
    private static final String AUTHENTICATION_FAILURE = "authenticationFailure";
    
	@NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @Override
    protected Event doExecute(final RequestContext context) {
        final Service service = WebUtils.getService(context);
        final String ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);

        try {
            final String serviceTicketId = this.centralAuthenticationService
                .grantServiceTicket(ticketGrantingTicket,
                    service);
            WebUtils.putServiceTicketInRequestScope(context,
                serviceTicketId);
            return success();
        } catch (final TicketException e) {
        	if (e instanceof UnsatisfiedAuthenticationPolicyException) {
        		Map<String, Class<? extends Exception>> handlerErrors = new HashMap<String, Class<? extends Exception>>();
        		handlerErrors.put("captchaNotValidExceptioon", ServiceNotValideException.class);
        		return new Event(this, AUTHENTICATION_FAILURE, new LocalAttributeMap("error", new AuthenticationException(handlerErrors)));
        	}
            if (isGatewayPresent(context)) {
                return result("gateway");
            }
        }

        return error();
    }

    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    protected boolean isGatewayPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get("gateway"));
    }

}
