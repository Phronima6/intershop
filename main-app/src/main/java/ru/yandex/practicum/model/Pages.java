package ru.yandex.practicum.model;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pages {

    @Min(1)
    int itemsOnPage;
    @Min(1)
    int numberOfPages;

    public boolean hasMultiplePages() {
        return numberOfPages > 1;
    }

    public boolean isFirstPage(int currentPage) {
        return currentPage == 1;
    }

    public boolean isLastPage(int currentPage) {
        return currentPage == numberOfPages;
    }

}