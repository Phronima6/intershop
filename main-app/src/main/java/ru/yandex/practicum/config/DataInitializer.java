package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.ItemRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        log.info("Инициализация данных завершена");
    }

}