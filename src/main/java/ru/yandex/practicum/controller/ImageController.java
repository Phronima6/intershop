package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.yandex.practicum.service.ImageService;

@Controller
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ImageController {

    ImageService imageService;

    @ResponseBody
    @GetMapping("/{itemId}/image")
    public byte[] getImage(@PathVariable(name = "itemId") final int itemId) {
        return imageService.getImage(itemId);
    }

}