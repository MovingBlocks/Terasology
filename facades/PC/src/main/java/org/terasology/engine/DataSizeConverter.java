// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import picocli.CommandLine;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataSizeConverter implements CommandLine.ITypeConverter<Long> {
    protected final static Pattern pattern = Pattern.compile(
            "(?<n>\\d+([.,]\\d*)?)" +
            "\\s*" +
            "(?<suffix>\\p{Alpha})?b?",
            Pattern.CASE_INSENSITIVE
    );

    @SuppressWarnings("unused")  // used by Unit.valueOf
    enum Unit {
        B(1),
        K(1 << 10),
        M(1 << 20),
        G(1 << 30),
        T(1L << 40);

        private final BigDecimal scale;

        Unit(long scale) {
            this.scale = BigDecimal.valueOf(scale);
        }

        BigDecimal multiply(BigDecimal n) {
            return n.multiply(scale);
        }
    }

    @Override
    public Long convert(String value) {
        if (value == null) {
            throw new CommandLine.TypeConversionException("null input");
        }
        Matcher match = pattern.matcher(value);
        if (!match.matches()) {
            throw new CommandLine.TypeConversionException("JANK");
        }
        BigDecimal n = new BigDecimal(match.group("n"));
        if (match.group("suffix") != null) {
            Unit unit = Unit.valueOf(match.group("suffix").toUpperCase(Locale.ROOT));
            n = unit.multiply(n);
        }
        return n.toBigInteger().longValueExact();
    }
}
