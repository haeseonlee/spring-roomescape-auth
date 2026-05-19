package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AdminCheckInterceptor;
import roomescape.auth.LoginCheckInterceptor;
import roomescape.auth.LoginMemberArgumentResolver;
import roomescape.repository.MemberQueryingDao;

import java.util.List;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final MemberQueryingDao memberQueryingDao;

    public AuthConfig(MemberQueryingDao memberQueryingDao) {
        this.memberQueryingDao = memberQueryingDao;
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
        resolvers.add(new LoginMemberArgumentResolver(memberQueryingDao));
    }
}