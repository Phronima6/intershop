package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.service.ImageService;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ImageControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ImageService imageService;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public ImageService imageService() {
            return Mockito.mock(ImageService.class);
        }
    }

    @Test
    void getImage_shouldReturnImageBytesWhenFound() throws Exception {
        int itemId = 1;
        byte[] imageBytes = "test image data".getBytes();
        given(imageService.getImage(itemId)).willReturn(imageBytes);
        mockMvc.perform(get("/{itemId}/image", itemId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageBytes));
        verify(imageService).getImage(itemId);
    }

    @Test
    void getImage_shouldReturnOkWithEmptyBodyWhenServiceReturnsEmpty() throws Exception {
        int itemId = 2;
        given(imageService.getImage(itemId)).willReturn(new byte[0]);
        mockMvc.perform(get("/{itemId}/image", itemId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0]));
        verify(imageService).getImage(itemId);
    }

}