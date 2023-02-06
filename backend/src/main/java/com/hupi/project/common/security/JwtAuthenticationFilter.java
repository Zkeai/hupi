package com.hupi.project.common.security;


import com.hupi.project.constant.JwtConstant;

import com.hupi.project.model.entity.CheckResult;

import com.hupi.project.model.entity.User;
import com.hupi.project.service.UserService;
import io.jsonwebtoken.Claims;
import com.hupi.project.util.JwtUtil;
import io.jsonwebtoken.JwtException;


import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.support.WebApplicationContextUtils;


import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JwtAuthenticationFilter extends BasicAuthenticationFilter {


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //拦截器调用服务层
        ServletContext context = request.getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
        UserService userService  = ctx.getBean(UserService.class);
        MyUserDetailServiceImpl myUserDetailService = ctx.getBean(MyUserDetailServiceImpl.class);

        System.out.println("请求url："+request.getRequestURI());
        /** 获取token */
        String token = request.getHeader("authorization").replace("Bearer ", "");

        if(token.equals("null")|| token.equals("")){
            chain.doFilter(request,response);
            return;
        }

        /** 验证token */
        CheckResult checkResult  = JwtUtil.validateJWT(token);
        if(!checkResult.isSuccess()){
            switch (checkResult.getErrCode()){
                case JwtConstant.JWT_ERRCODE_NULL: throw new JwtException("Token不存在");
                case JwtConstant.JWT_ERRCODE_FAIL: throw new JwtException("Token验证不通过");
                case JwtConstant.JWT_ERRCODE_EXPIRE: throw new JwtException("Token过期");

            }
        }

        Claims claims = JwtUtil.getAllClaimsFromToken(token);
        String account = claims.getSubject();
        User user =userService.getByAccount(account);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=new UsernamePasswordAuthenticationToken(account,null,myUserDetailService.getUserAuthority(user.getId()));

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        chain.doFilter(request,response);
    }
}
