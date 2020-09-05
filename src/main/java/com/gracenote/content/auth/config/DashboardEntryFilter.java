package com.gracenote.content.auth.config;

import com.gracenote.content.auth.domain.application.ApplicationService;
import com.gracenote.content.auth.domain.history.UserApplicationHistoryService;
import com.gracenote.content.auth.domain.user.UserService;
import com.gracenote.content.auth.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author deepak on 24/10/17.
 */

@Component
public class DashboardEntryFilter extends GenericFilterBean {

	@Autowired
	UserApplicationHistoryService historyService;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;

		filterChain.doFilter(request, response);

		/**
		 * Make entry in history table at the time of successful login to content-auth
		 * dashboard only. And for login entry in history table of other applications
		 * are made from CustomAuthentication class
		 */
		if (request.getRequestURI() != null && request.getRequestURI().equalsIgnoreCase(Constants.OAUTH_URL)) {
			if (request.getUserPrincipal().getName() != null && request.getParameter("username") != null) {
				if (request.getUserPrincipal().getName().equalsIgnoreCase(Constants.DASHBOARD_CLIENT)
						&& response.getStatus() == 200) {

					String[] str_array = request.getParameter("username").split("_sso_");
					String username = null;
					if (str_array.length == 2) {
						username = str_array[0];
						str_array[1] = "";

						historyService.save(username, request.getUserPrincipal().getName());
					}

				}
			}
		}
	}

}
