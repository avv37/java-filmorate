package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Mpa> findAll() {
        Collection<Mpa> mpas = mpaStorage.getAllMpa();
        log.info("Отдали коллекцию mpa");
        return mpas;
    }

    public Mpa getMpaById(Integer mpaId) {
        Optional<Mpa> mpaOpt = mpaStorage.getMpaById(mpaId);
        if (mpaOpt.isEmpty()) {
            throw new NotFoundException("Mpa с id = " + mpaId + " не найден");
        }
        return mpaOpt.get();
    }
}
