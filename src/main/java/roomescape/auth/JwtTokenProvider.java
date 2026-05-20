package roomescape.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = "49b503eed458ec83811c78261d325ac8c61202d77f77890eb709895cd8917557773f0679675f3174c3002c986b6e73b3f15a48ea3de1ab71578186413b00b140";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // 로그인 성공시 memberId 담은 토큰 생성
    public String createToken(Long memberId) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    // 요청이 들어올 때 토큰에서 memberId 추출
    public Long getMemberId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }
}
