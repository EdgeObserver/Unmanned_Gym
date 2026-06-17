package order.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import order.util.ThreadLocalUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String userIdHeader = request.getHeader("X-User-Id");
        String userRoleHeader = request.getHeader("X-User-Role");

        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                int userId = Integer.parseInt(userIdHeader);
                int userRole = 0;

                if (userRoleHeader != null && !userRoleHeader.isEmpty()) {
                    userRole = Integer.parseInt(userRoleHeader);
                }

                Map<String, Object> claims = new HashMap<>();
                claims.put("id", userId);
                claims.put("role", userRole);
                ThreadLocalUtil.set(claims);

                return true;

            } catch (NumberFormatException e) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户信息格式错误\"}");
                return false;
            }
        }

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"缺少用户信息，请通过网关访问\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
