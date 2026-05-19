package roomescape.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.dto.auth.LoginRequest;
import roomescape.exception.AuthenticationException;
import roomescape.repository.MemberQueryingDao;

@Service
public class AuthService {

    private static final String LOGIN_MEMBER_ID = "loginMemberId";

    private final MemberQueryingDao memberQueryingDao;

    public AuthService(MemberQueryingDao memberQueryingDao) {
        this.memberQueryingDao = memberQueryingDao;
    }

    public void login(LoginRequest request, HttpSession session) {
        Member member = memberQueryingDao.findByEmail(request.email())
                .orElseThrow(AuthenticationException::new);

        if (!member.getPassword().equals(request.password())) {
            throw new AuthenticationException();
        }

        session.setAttribute(LOGIN_MEMBER_ID, member.getId());
    }
}