package roomescape.exception;

public class AuthorizationException extends RuntimeException {

    public AuthorizationException() {
        super("해당 예약에 대한 권한이 없습니다.");
    }
}
