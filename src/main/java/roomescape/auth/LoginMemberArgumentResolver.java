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
    private final JwtTokenProvider jwtTokenProvider;

    public LoginMemberArgumentResolver(MemberQueryingDao memberQueryingDao, JwtTokenProvider jwtTokenProvider) {
        this.memberQueryingDao = memberQueryingDao;
        this.jwtTokenProvider = jwtTokenProvider;
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

        // 세션 확인
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LOGIN_MEMBER_ID) != null) {
            Long memberId = (Long)  session.getAttribute(LOGIN_MEMBER_ID);
            return memberQueryingDao.findById(memberId)
                    .orElseThrow(AuthenticationException::new);
        }

        // Authorization 헤더 확인 (모바일)
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            Long memberId = jwtTokenProvider.getMemberId(authorization.substring(7));
            return memberQueryingDao.findById(memberId)
                    .orElseThrow(AuthenticationException::new);
        }

        throw new  AuthenticationException();
    }
}
