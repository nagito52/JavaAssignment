package com.example.attendance.filter;

import java.io.IOException;
import java.net.http.HttpRequest;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthenticationFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hthpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpSession session = HttpRequest.getSession(false);
		
		boolean loggedIn = session != null && session.getAttribute("user") != null;
		
		if (loggedIn) {
			chain.doFilter(request, response);
		} else {
			httpResponse.sendRedirect(HttpRequest.getContextPath() + "/login.jsp");
		}
	}
}
