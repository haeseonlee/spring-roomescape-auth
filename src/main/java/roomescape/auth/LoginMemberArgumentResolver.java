package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.member.Member;
import roomescape.exception.AuthenticationException;
import roomescape.repository.MemberQueryingDao;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String LOGIN_MEMBER_ID = "loginMemberId";

    private final MemberQueryingDao memberQueryingDao;

    public LoginMemberArgumentResolver(MemberQueryingDao memberQueryingDao) {
        this.memberQueryingDao = memberQueryingDao;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new AuthenticationException();
        }

        Long memberId = (Long) session.getAttribute(LOGIN_MEMBER_ID);
        if (memberId == null) {
            throw new AuthenticationException();
        }

        return memberQueryingDao.findById(memberId)
                .orElseThrow(AuthenticationException::new);
    }
}
