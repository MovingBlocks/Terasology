// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSizeConverterTest {

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "16, 16b",
            "126, 0.1234k",
            "512, 0.5 k",
            "11264, 11k",
            "11264, 11Kb",
            "44040192, 42 m",
            "268435456, 256M",
            "2684354560, 2.5g",
            "8589934592, 8G",
    })
    void testValidInputs(long expected, String input) {
        assertEquals(expected, new DataSizeConverter().convert(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"b", "mb", " mb", " ", "🆗", "-5m", "null", "NaN"})
    void testBadInputs(String input) {
        DataSizeConverter converter = new DataSizeConverter();
        assertThrows(CommandLine.TypeConversionException.class, () -> converter.convert(input));
    }
}
