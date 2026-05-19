package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;

import java.util.Optional;

@Repository
public class MemberQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public MemberQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            resultSet.getString("role")
    );

    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT id, name, email, password, role FROM member WHERE email = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, email);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<Member> findById(Long id) {
        String sql = "SELECT id, name, email, password, role FROM member WHERE id = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, id);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
