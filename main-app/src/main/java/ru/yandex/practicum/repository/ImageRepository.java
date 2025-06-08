package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.model.Image;

public interface ImageRepository extends R2dbcRepository<Image, Integer> {
}