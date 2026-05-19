package roomescape.dto.auth;

import roomescape.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

public record LoginRequest(String email, String password) {

    public LoginRequest {
        List<String> emptyFields = new ArrayList<>();

        if (email == null) {
            emptyFields.add("email");
        }
        if (password == null) {
            emptyFields.add("password");
        }

        if (!emptyFields.isEmpty()) {
            throw new InvalidInputException("%s 필드가 비어있습니다.".formatted(emptyFields));
        }
    }
}
