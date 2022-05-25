// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import com.google.common.base.MoreObjects;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegexEvaluator<E extends ILoggingEvent> extends EventEvaluatorBase<E> {
    final Pattern prefix;
    final Pattern matchMessage;

    private final Predicate<String> prefixTest;
    private final Predicate<String> messageTest;

    public RegexEvaluator(Pattern prefix, Pattern matchMessage) {
        this.prefix = prefix;
        this.matchMessage = matchMessage;

        prefixTest = (prefix == null) ? s -> true : prefix.asPredicate();
        messageTest = checkNotNull(matchMessage).asPredicate();
    }

    public RegexEvaluator(String prefixString, String messagePattern) {
        this(prefixString == null ? null : Pattern.compile("^" + Pattern.quote(prefixString)),
                Pattern.compile(checkNotNull(messagePattern)));
    }

    @Override
    public boolean evaluate(E event) {
        return prefixTest.test(event.getLoggerName()) && messageTest.test(event.getFormattedMessage());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("prefix", prefix)
                .add("matchMessage", matchMessage)
                .toString();
    }
}
