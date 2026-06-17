package book.interceptor;

import book.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        // 方案二：从 HTTP Header 中直接获取用户 ID（由 Gateway 透传）
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                int userId = Integer.parseInt(userIdHeader);

                // 将用户 ID 存入 ThreadLocal（模拟原来的格式）
                Map<String, Object> claims = new HashMap<>();
                claims.put("id", userId);
                ThreadLocalUtil.set(claims);

                // 放行请求
                return true;

            } catch (NumberFormatException e) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户ID格式错误\"}");
                return false;
            }
        }

        // 缺少 X-User-Id Header
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
        // 清理 ThreadLocal，防止内存泄漏
        ThreadLocalUtil.remove();
    }
}
