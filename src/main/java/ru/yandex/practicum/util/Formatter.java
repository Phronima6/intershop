package ru.yandex.practicum.util;

import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.text.DecimalFormat;

@FieldDefaults(makeFinal = true)
@NoArgsConstructor
public class Formatter {

    public static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###,##0.00");

}