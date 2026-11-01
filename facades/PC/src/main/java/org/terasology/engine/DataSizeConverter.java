// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import picocli.CommandLine;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert a string describing a size in bytes.
 * <p>
 * Intended to be compatible with the command line options for java:
 * <blockquote><p>
 * If you are expected to specify the size in bytes, you can use no suffix,
 * or use the suffix <b>k</b> or <b>K</b> for kilobytes (KB),
 * <b>m</b> or <b>M</b> for megabytes (MB), <b>g</b> or <b>G</b> for gigabytes (GB).
 * For example, to set the size to 8 GB, you can specify either <code>8g</code>,
 * <code>8192m</code>, <code>8388608k</code>, or <code>8589934592</code> as the argument.
 * </p></blockquote><p>
 * This implementation also supports decimal values such as <code>3.5G</code>.
 */
public class DataSizeConverter implements CommandLine.ITypeConverter<Long> {
    @SuppressWarnings("unused")  // used by Unit.valueOf
    enum Unit {
        B(1),
        K(1 << 10),  // kilo
        M(1 << 20),  // mega
        G(1 << 30),  // giga
        T(1L << 40); // tera

        private final BigDecimal scale;

        Unit(long scale) {
            this.scale = BigDecimal.valueOf(scale);
        }

        BigDecimal multiply(BigDecimal n) {
            return n.multiply(scale);
        }
    }

    protected static final Pattern PATTERN = Pattern.compile(
            "(?<n>\\d+([.,]\\d*)?)" + // digits, maybe also a decimal part
                    "\\s*" +
                    "(?<suffix>\\p{Alpha})?b?",  // a suffix character, optionally followed by B
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Long convert(String value) {
        if (value == null) {
            throw new CommandLine.TypeConversionException("null input");
        }
        Matcher match = PATTERN.matcher(value);
        if (!match.matches()) {
            throw new CommandLine.TypeConversionException(
                    "Expected a number followed by K, M, G, or T. " +
                    "Failed to parse '" + value + "'");
        }
        BigDecimal n = new BigDecimal(match.group("n"));
        if (match.group("suffix") != null) {
            Unit unit = Unit.valueOf(match.group("suffix").toUpperCase(Locale.ROOT));
            n = unit.multiply(n);
        }
        return n.toBigInteger().longValueExact();
    }
}
