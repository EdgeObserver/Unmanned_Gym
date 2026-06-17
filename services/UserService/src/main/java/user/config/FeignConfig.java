package user.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    // 传递用户ID
                    String userId = request.getHeader("X-User-Id");
                    if (userId != null) {
                        template.header("X-User-Id", userId);
                    }
                    
                    // 传递用户角色
                    String userRole = request.getHeader("X-User-Role");
                    if (userRole != null) {
                        template.header("X-User-Role", userRole);
                    }
                    
                    // 传递Authorization Token
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null) {
                        template.header("Authorization", authorization);
                    }
                }
            }
        };
    }
}
