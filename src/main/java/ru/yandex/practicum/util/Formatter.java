package ru.yandex.practicum.util;

import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@FieldDefaults(makeFinal = true)
@NoArgsConstructor
public class Formatter {

    public static DecimalFormat DECIMAL_FORMAT;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(',');
        DECIMAL_FORMAT = new DecimalFormat("###,###,##0.00", symbols);
    }

}