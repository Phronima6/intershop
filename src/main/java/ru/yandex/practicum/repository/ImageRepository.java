package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Image;

public interface ImageRepository extends JpaRepository<Image, Integer> {
}