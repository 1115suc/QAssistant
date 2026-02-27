package course.QAssistant.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 请求工具类，提取 IP、DeviceID、Token
 */
public class RequestUtil {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };

    private RequestUtil() {
    }

    /**
     * 获取客户端真实 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个 IP，取第一个
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取 DeviceID（由前端通过请求头或参数传递）
     */
    public static String getDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-ID");
        if (!StringUtils.hasText(deviceId)) {
            deviceId = request.getParameter("deviceId");
        }
        return StringUtils.hasText(deviceId) ? deviceId : "unknown-device";
    }

    /**
     * 获取 Token（支持 Authorization 头和 token 参数）
     */
    public static String getToken(HttpServletRequest request) {
        String token = request.getHeader("Token");
        if (StringUtils.hasText(token)) {
            // 去掉 Bearer 前缀
            return token.startsWith("Bearer ") ? token.substring(7) : token;
        }
        token = request.getParameter("token");
        return StringUtils.hasText(token) ? token : "anonymous";
    }
}