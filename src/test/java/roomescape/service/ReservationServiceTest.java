package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.Member;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.theme.ThemeRequest;
import roomescape.exception.InvalidReservationException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationTimeUpdatingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ThemeQueryingDao;
import roomescape.repository.ThemeUpdatingDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({ReservationService.class,
        ReservationQueryingDao.class, ReservationUpdatingDao.class,
        ReservationTimeQueryingDao.class, ReservationTimeUpdatingDao.class,
        ThemeQueryingDao.class, ThemeUpdatingDao.class})
class ReservationServiceTest {

    private Member testMember;
    private Member testMember2;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeUpdatingDao reservationTimeUpdatingDao;

    @Autowired
    private ThemeUpdatingDao themeUpdatingDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES (?, ?, ?)", "브라운", "brown@example.com", "password1");
        Long id1 = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, "brown@example.com");
        testMember = new Member(id1, "브라운", "brown@example.com", "password1");

        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES (?, ?, ?)", "네오", "neo@example.com", "password2");
        Long id2 = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, "neo@example.com");
        testMember2 = new Member(id2, "네오", "neo@example.com", "password2");
    }

    @Test
    void 예약_생성_성공() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId);

        ReservationResponse saved = reservationService.create(testMember, request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("브라운");
    }

    @Test
    void 과거_날짜로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest(LocalDate.now().minusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.create(testMember, request))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 과거_시간으로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐점의 부재", "탐험", "http://example.com"));
        ReservationRequest reservationReq = new ReservationRequest(LocalDate.now(), timeId, themeId);

        assertThatThrownBy(() -> reservationService.create(testMember, reservationReq))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 중복된_테마_날짜_시간으로_예약하면_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId);

        reservationService.create(testMember, request);

        assertThatThrownBy(() -> reservationService.create(testMember, request))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외가_발생한다() {
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 999L, themeId);

        assertThatThrownBy(() -> reservationService.create(testMember, request))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), timeId, 999L);

        assertThatThrownBy(() -> reservationService.create(testMember, request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 전체_예약_조회() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        reservationService.create(testMember, new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId));
        reservationService.create(testMember2, new ReservationRequest(LocalDate.now().plusDays(2), timeId, themeId));

        List<ReservationResponse> result = reservationService.readAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 예약_날짜_및_시간_변경() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("테마", "설명", "http://example.com"));
        ReservationResponse created = reservationService.create(testMember, new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId));

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationRequest newReservationReq = new ReservationRequest(LocalDate.now().plusDays(2), newTimeId, themeId);
        ReservationResponse updated = reservationService.update(created.getId(), newReservationReq);

        assertThat(updated.getDate()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 과거_날짜로_변경시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationResponse created = reservationService.create(testMember, new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId));

        Long newTimeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        ReservationRequest newReservationReq = new ReservationRequest(LocalDate.now().minusDays(1), newTimeId, themeId);

        assertThatThrownBy(() -> reservationService.update(created.getId(), newReservationReq))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 이미_예약된_시간으로_변경시_예외가_발생한다() {
        Long timeId1 = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long timeId2 = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(11, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));

        ReservationResponse created = reservationService.create(testMember, new ReservationRequest(LocalDate.now().plusDays(1), timeId1, themeId));
        reservationService.create(testMember2, new ReservationRequest(LocalDate.now().plusDays(1), timeId2, themeId));

        ReservationRequest updated = new ReservationRequest(LocalDate.now().plusDays(1), timeId2, themeId);
        assertThatThrownBy(() -> reservationService.update(created.getId(), updated))
                .isInstanceOf(ReservationAlreadyExistException.class);
    }

    @Test
    void 존재하지_않는_예약_변경시_예외가_발생한다() {
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        Long themeId = themeUpdatingDao.insert(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), timeId, themeId);

        assertThatThrownBy(() -> reservationService.update(999L, request))
                .isInstanceOf(ReservationNotFoundException.class);
    }
}
