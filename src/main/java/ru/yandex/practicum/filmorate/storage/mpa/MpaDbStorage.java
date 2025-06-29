package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mpa> getMpaById(Integer id) {
        String query = "SELECT * FROM mpa WHERE id = ?";
        try {
            Mpa result = jdbcTemplate.queryForObject(query, new MpaRowMapper(), id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        String query = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(query, new MpaRowMapper());
    }
}
