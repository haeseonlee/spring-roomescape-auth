package roomescape.dto.reservation;

import roomescape.exception.InvalidInputException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record ReservationRequest(LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        List<String> emptyFields = new ArrayList<>();

        if (date == null) {
            emptyFields.add("date");
        }

        if (timeId == null) {
            emptyFields.add("timeId");
        }

        if (themeId == null) {
            emptyFields.add("themeId");
        }

        if (!emptyFields.isEmpty()) {
            throw new InvalidInputException("%s 필드가 비어있습니다.".formatted(emptyFields));
        }
    }
}
