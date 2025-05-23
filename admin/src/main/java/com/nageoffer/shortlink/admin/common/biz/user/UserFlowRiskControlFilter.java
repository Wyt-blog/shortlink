package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserFlowRiskControlFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private final String timeWindow;
    private final Long maxAccessCount;

    private static final RedisScript<Long> USER_FLOW_RISK_CONTROL;

    static {
        USER_FLOW_RISK_CONTROL = RedisScript.of(new ClassPathResource("lua/user_flow_risk_control.lua"), Long.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String username = Optional.ofNullable(UserContext.getUsername()).orElse("other");
        Long result = null;
        try{
            result = stringRedisTemplate.execute(USER_FLOW_RISK_CONTROL, List.of(username), timeWindow);
        }catch (Throwable e){
            log.error("用户限流拦截器异常",e);
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(UserErrorCodeEnum.FLOW_LIMIT_ERROR))));
        }
        if (result == null || result > maxAccessCount){
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(UserErrorCodeEnum.FLOW_LIMIT_ERROR))));
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void returnJson(HttpServletResponse response, String json) throws IOException {
        PrintWriter out = null;
        response.setContentType("text/html;charset=utf-8");
        try {
            out = response.getWriter();
            out.write(json);
        }catch (Exception e){}
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
