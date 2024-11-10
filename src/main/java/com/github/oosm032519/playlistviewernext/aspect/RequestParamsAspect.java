package com.github.oosm032519.playlistviewernext.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
public class RequestParamsAspect {

    private static final Logger logger = LoggerFactory.getLogger(RequestParamsAspect.class);

    @Before("execution(* com.github.oosm032519.playlistviewernext.controller..*(..))")
    public void logRequestParams(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Map<String, String[]> parameterMap = request.getParameterMap();
                if (!parameterMap.isEmpty()) {
                    StringBuilder params = new StringBuilder();
                    parameterMap.forEach((key, values) -> params.append(key).append("=").append(String.join(",", values)).append("&"));
                    if (!params.isEmpty()) {
                        params.deleteCharAt(params.length() - 1);
                    }
                    logger.debug("{} のリクエストパラメータ: {}", joinPoint.getSignature(), params);
                }
            }
        } catch (Exception e) {
            logger.error("リクエストパラメータの取得中にエラーが発生しました。", e);
        }
    }
}
