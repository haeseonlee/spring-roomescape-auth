package roomescape.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.member.Member;
import roomescape.dto.auth.LoginRequest;
import roomescape.exception.AuthenticationException;
import roomescape.repository.MemberQueryingDao;

@Service
public class AuthService {

    private static final String LOGIN_MEMBER_ID = "loginMemberId";

    private final MemberQueryingDao memberQueryingDao;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberQueryingDao memberQueryingDao, JwtTokenProvider jwtTokenProvider) {
        this.memberQueryingDao = memberQueryingDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Member authenticate(LoginRequest request) {
        Member member = memberQueryingDao.findByEmail(request.email())
                .orElseThrow(AuthenticationException::new);

        if (!member.getPassword().equals(request.password())) {
            throw new AuthenticationException();
        }

        return member;
    }

    public void login(LoginRequest request, HttpSession session) {
        Member member = authenticate(request);
        session.setAttribute(LOGIN_MEMBER_ID, member.getId());
    }

    public String loginWithToken(LoginRequest request) {
        Member member = authenticate(request);
        return jwtTokenProvider.createToken(member.getId());
    }
}