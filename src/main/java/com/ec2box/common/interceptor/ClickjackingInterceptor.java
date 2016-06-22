package com.ec2box.common.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class ClickjackingInterceptor extends AbstractInterceptor {

	/**
	 * Clickjacking, also known as a "UI redress attack", is when an attacker
	 * uses multiple transparent or opaque layers to trick a user into clicking
	 * on a button or link on another page when they were intending to click on
	 * the the top level page. Thus, the attacker is "hijacking" clicks meant
	 * for their page and routing them to another page, most likely owned by
	 * another application, domain, or both.
	 * https://www.owasp.org/index.php/Clickjacking
	 */

	private static final long serialVersionUID = 2438421386123540997L;
	private static final String HEADER = "X-Frame-Options";
	private static final String VALUE = "DENY";
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext context = invocation.getInvocationContext();
		HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);
		String headerValue = VALUE;
		response.addHeader(HEADER, headerValue);
		return invocation.invoke();
	}
}
