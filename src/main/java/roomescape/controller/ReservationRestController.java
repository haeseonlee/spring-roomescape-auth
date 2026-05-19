package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.domain.member.Member;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.ReservationService;

import java.util.List;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> readAll() {
        return reservationService.readAll();
    }

    @GetMapping("/reservations/mine")
    public List<ReservationResponse> readMine(@LoginMember Member member) {
        return reservationService.readByMemberId(member.getId());
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> read(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationService.read(id);
        return ResponseEntity.ok(reservationResponse);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@LoginMember Member member, @RequestBody ReservationRequest reservationReq) {
        ReservationResponse response = reservationService.create(member, reservationReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> update(@PathVariable Long id, @RequestBody ReservationRequest reservationReq) {
        ReservationResponse updatedReservation = reservationService.update(id, reservationReq);
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
