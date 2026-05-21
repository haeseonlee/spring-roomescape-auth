package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.store.Store;

import java.util.Optional;

@Repository
public class StoreQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public StoreQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Store> storeRowMapper = (resultSet, rowNum) -> new Store(
            resultSet.getLong("id"),
            resultSet.getString("name")
    );

    public Optional<Store> findById(Long id) {
        String sql = "SELECT id, name FROM store WHERE id = ?";
        try {
            Store store = jdbcTemplate.queryForObject(sql, storeRowMapper, id);
            return Optional.of(store);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
