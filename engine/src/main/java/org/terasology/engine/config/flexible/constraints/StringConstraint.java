// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.constraints;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringConstraint implements SettingConstraint<String> {
    private static final Logger logger = LoggerFactory.getLogger(StringConstraint.class);
    private final List<Predicate<String>> predicates;

    public StringConstraint(Predicate<String>... predicates) {
        this.predicates = Arrays.asList(predicates);
    }

    public static Predicate<String> notEmptyOrNull() {
        return new PredicateWithDescription<>("not null or not empty", Predicates.not(Strings::isNullOrEmpty));
    }

    public static Predicate<String> maxLength(int length) {
        return new PredicateWithDescription<>("length should be less than " + length, s -> s.length() < length);
    }

    public static Predicate<String> regex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return new PredicateWithDescription<>("matches regex: \"" + regex + "\"", s -> pattern.matcher(s).matches());
    }

    private static String getDescription(Predicate<String> p) {
        if (p instanceof PredicateWithDescription) {
            return ((PredicateWithDescription<String>) p).getDescription();
        } else {
            return "Predicate without description";
        }
    }

    @Override
    public boolean isSatisfiedBy(String value) {
        return predicates.stream().allMatch(p -> p.test(value));
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void warnUnsatisfiedBy(String value) {
        logger.warn("String [{}] does not match the conditions: {}", value,
                predicates.stream()
                        .filter(p -> !p.test(value))
                        .map(StringConstraint::getDescription)
                        .collect(Collectors.joining(",", "[", "]")));
    }
}
