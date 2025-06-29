package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
public class MpaDbStorageTest {
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void testGetMpaById() {
        Optional<Mpa> mpaOptional = mpaDbStorage.getMpaById(5);
        assertTrue(mpaOptional.isPresent());
        Mpa mpa = mpaOptional.get();
        assertThat(mpa).isNotNull()
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "NC-17");

        assertTrue(mpaDbStorage.getMpaById(6).isEmpty());
    }

    @Test
    public void testGetAllMpa() {
        List<Mpa> mpas = mpaDbStorage.getAllMpa();
        assertEquals(5, mpas.size());
        Mpa mpa = mpas.get(0);
        assertThat(mpa).isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
        mpa = mpas.get(1);
        assertThat(mpa).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "PG");
        mpa = mpas.get(2);
        assertThat(mpa).isNotNull()
                .hasFieldOrPropertyWithValue("id", 3)
                .hasFieldOrPropertyWithValue("name", "PG-13");
        mpa = mpas.get(3);
        assertThat(mpa).isNotNull()
                .hasFieldOrPropertyWithValue("id", 4)
                .hasFieldOrPropertyWithValue("name", "R");
        mpa = mpas.get(4);
        assertThat(mpa).isNotNull()
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "NC-17");
    }
}
