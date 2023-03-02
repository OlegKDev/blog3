package com.app.blog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.blog.dto.auth.ErrorMessageDto;
import com.app.blog.exception.BlogApiException;
import com.app.blog.exception.ErrorDetailsDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = getJwtFromRequestHeader(request);

		try {
			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
				String username = jwtTokenProvider.getUsernameFromJwt(token);
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}

		} catch (BlogApiException e) {
			response.setStatus(e.getStatus().value());
			ErrorMessageDto errorMessageDto = new ErrorMessageDto();
			errorMessageDto.setErrorMessage(e.getMessage());
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(errorMessageDto);
			response.getWriter().write(message);
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequestHeader(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			String token = bearerToken.substring(7, bearerToken.length());
			return token;
		}

		return null;
	}
}
