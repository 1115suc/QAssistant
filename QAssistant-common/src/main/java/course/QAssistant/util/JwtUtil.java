package course.QAssistant.util;

import course.QAssistant.constant.TimeConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类
 */
public class JwtUtil {

    //有效期为
    public static final Long JWT_TTL = TimeConstant.ONE_WEEK * 1000L;
    //设置秘钥明文
    public static final String JWT_KEY = "1115suc-QAssistant";

    private static final Long ONE_DAY = 60 * 60 * 24 * 1000L;

    public static String getUUID(){
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }
    
    /**
     * 生成jtw
     * @param subject token中要存放的数据（json格式）
     * @return
     */
    public static String createJWT(String subject) {
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID());// 设置过期时间
        return builder.compact();
    }

    /**
     * 生成jtw
     * @param subject token中要存放的数据（json格式）
     * @param ttlMillis token超时时间
     * @return
     */
    public static String createJWT(String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID());// 设置过期时间
        return builder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(ttlMillis==null){
            ttlMillis=JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .id(uuid)                   //唯一的ID
                .subject(subject)           // 主题,可以是JSON数据
                .issuer("1115suc")       // 签发者
                .issuedAt(now)              // 签发时间
                .signWith(secretKey, Jwts.SIG.HS256) //使用HS256对称加密算法签名, 第二个参数为秘钥
                .expiration(expDate);
    }

    /**
     * 创建token
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     */
    public static String createJWT(String id, String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id);// 设置过期时间
        return builder.compact();
    }

    /**
     * 生成加密后的秘钥 secretKey
     * @return
     */
    public static SecretKey generalKey() {
        try {
            // 使用SHA-256哈希算法将密钥转换为32字节，以满足HS256算法的要求
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(JWT_KEY.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
    
    /**
     * 解析
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    /**
     * 判断token是否即将过期
     * @param token token字符串
     * @return true:即将过期 false:未过期且剩余时间充足
     */
    public static boolean isNearExpiration(String token) {
        try {
            return isNearExpiration(parseJWT(token));
        } catch (Exception e) {
            return true; // 解析失败（如已过期）视为即将过期
        }
    }

    /**
     * 判断token是否即将过期
     * @param claims claims对象
     * @return true:即将过期 false:未过期且剩余时间充足
     */
    public static boolean isNearExpiration(Claims claims) {
        long time = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        return (time - currentTime) < ONE_DAY;
    }
}
