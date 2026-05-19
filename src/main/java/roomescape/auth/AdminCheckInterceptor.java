package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.member.Member;
import roomescape.repository.MemberQueryingDao;

public class AdminCheckInterceptor implements HandlerInterceptor {

    private static final String LOGIN_MEMBER_ID = "loginMemberId";

    private final MemberQueryingDao memberQueryingDao;

    public AdminCheckInterceptor(MemberQueryingDao memberQueryingDao) {
        this.memberQueryingDao = memberQueryingDao;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(LOGIN_MEMBER_ID) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long memberId = (Long) session.getAttribute(LOGIN_MEMBER_ID);
        Member member = memberQueryingDao.findById(memberId).orElse(null);

        if (member == null || !member.isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
