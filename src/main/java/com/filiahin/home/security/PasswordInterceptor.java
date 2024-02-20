package com.filiahin.home.security;

import com.filiahin.home.exceptions.UnauthorizedAccessException;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class PasswordInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            PasswordAnnotation passwordAnnotation = method.getMethodAnnotation(PasswordAnnotation.class);
            if (passwordAnnotation == null) {
                return true;
            }

            String hash = request.getParameter("");
            boolean isHashValid = EncryptionHelper.md5().equals(hash);

            if (isHashValid) {
                return true;
            }

            throw new UnauthorizedAccessException();
        }
        return true;
    }
}
