package roomescape.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}