package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AdminCheckInterceptor;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.LoginCheckInterceptor;
import roomescape.auth.LoginMemberArgumentResolver;
import roomescape.repository.MemberQueryingDao;

import java.util.List;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final MemberQueryingDao memberQueryingDao;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthConfig(MemberQueryingDao memberQueryingDao, JwtTokenProvider jwtTokenProvider) {
        this.memberQueryingDao = memberQueryingDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/reservations/mine");

        registry.addInterceptor(new AdminCheckInterceptor(memberQueryingDao))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/times", "/admin/times/**", "/admin/themes", "/admin/themes/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(memberQueryingDao, jwtTokenProvider));
    }
}