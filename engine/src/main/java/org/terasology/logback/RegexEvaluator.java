// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import com.google.common.base.MoreObjects;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Evaluate messages with a regular expression.
 *
 * @see RegexFilterAction
 */
public class RegexEvaluator<E extends ILoggingEvent> extends EventEvaluatorBase<E> {
    final Pattern prefix;
    final Pattern matchMessage;

    private final Predicate<String> prefixTest;
    private final Predicate<String> messageTest;

    /**
     * @param prefix Test the event's logger name against this pattern.
     *     May be null to apply to all loggers.
     *
     * @param matchMessage Test the event's formatted message against this pattern.
     */
    public RegexEvaluator(Pattern prefix, Pattern matchMessage) {
        this.prefix = prefix;
        this.matchMessage = matchMessage;

        prefixTest = (prefix == null) ? s -> true : prefix.asPredicate();
        messageTest = checkNotNull(matchMessage).asPredicate();
    }

    /**
     * @param prefixString   Test the event's logger name against this prefix. This input is a
     *     simple string, <em>not</em> a regex pattern. May be null to apply to all loggers.
     *
     * @param messagePattern A regular expression {@linkplain Pattern pattern} to match against the
     *     {@linkplain ILoggingEvent#getFormattedMessage() formatted message} of a logging event.
     */
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
