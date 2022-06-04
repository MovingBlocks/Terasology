// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.xml.sax.Attributes;

import java.util.function.BiFunction;

/**
 * Regex-based logback filter with concise syntax.
 * <p>
 * <b>Example:</b>
 * <p><code>
 *     &lt;denyRegex prefix="org.reflections" message="given scan \w+ are empty" /&gt;
 * </code>
 * <dl>
 *     <dt>{@code denyRegex}</dt>
 *     <dd>The tag starts with {@code "deny"}, meaning events matching this filter will be dropped.</dd>
 *
 *     <dt>{@code prefix} <i>(optional)</i></dt>
 *     <dd>This filter will only test events from loggers with names starting {@code "org.reflections"}.<br>
 *     It is {@linkplain FilterReply#NEUTRAL neutral} on other events.</dd>
 *
 *     <dt>{@code message}</dt>
 *     <dd>Match the {@linkplain ILoggingEvent#getFormattedMessage() formatted message} against
 *     the regular expression {@linkplain java.util.regex.Pattern pattern}
 *     {@code /given scan \w+ are empty/}.</dd>
 * </dl>
 * <p>
 * <b>Example:</b>
 * <p><code>
 *     &lt;requireRegex message="module:.*-SNAPSHOT" /&gt;
 * </code>
 * <dl>
 *     <dt>{@code requireRegex}</dt>
 *     <dd>The tag starts with {@code "require"}, meaning events <em>must</em> match the pattern.</dd>
 *
 *     <dt>{@code message}</dt>
 *     <dd>Match the {@linkplain ILoggingEvent#getFormattedMessage() formatted message} against
 *     the regular expression {@linkplain java.util.regex.Pattern pattern}
 *     {@code /module:.*-SNAPSHOT/}.</dd>
 * </dl>
 * No Groovy or Janino required.
 * <p>
 * This Action adds an {@link EvaluatorFilter} with a {@link RegexEvaluator} to
 * the current element, such as an {@link ch.qos.logback.core.Appender Appender}.
 * <p>
 * The element must be on the top of the stack and must implement {@link FilterAttachable}.
 * <p>
 * <b>Implementation Note:</b> In logback 1.2, we can <em>almost</em> define an
 * EvaluatorFilter using only logback-core, but core defines no LoggingEvent interface.
 * We have to link against logback-classic for that.
 *
 * @param <E> the type of logging event the filter operates on
 */
public class RegexFilterAction<E extends ILoggingEvent> extends Action {
    public static final String PREFIX_ATTRIBUTE = "prefix";
    public static final String MESSAGE_ATTRIBUTE = "message";
    public static final String DENY_ELEMENT = "deny";
    public static final String REQUIRE_ELEMENT = "require";

    protected final BiFunction<String, String, EventEvaluator<E>> evaluatorConstructor = RegexEvaluator::new;

    @Override
    public void begin(InterpretationContext context, String elementName, Attributes attributes) throws ActionException {
        if (peekFilterContainer(context) == null) {
            return;
        }

        var evaluator = evaluatorConstructor.apply(
                findAndSubst(context, attributes, PREFIX_ATTRIBUTE),
                findAndSubst(context, attributes, MESSAGE_ATTRIBUTE)
        );

        EvaluatorFilter<E> filter = new RegexFilter<>(
                getContext(),
                evaluator,
                makeFilterName(context, attributes));

        if (elementName.startsWith(REQUIRE_ELEMENT)) {
            filter.setOnMatch(FilterReply.NEUTRAL);
            filter.setOnMismatch(FilterReply.DENY);
        } else if (elementName.startsWith(DENY_ELEMENT)) {
            filter.setOnMatch(FilterReply.DENY);
            filter.setOnMismatch(FilterReply.NEUTRAL);
        }

        // Push the new filter to the stack, in case anyone wants to configure it further.
        context.pushObject(filter);
    }

    @Override
    public void end(InterpretationContext context, String name) throws ActionException {
        Object top = context.peekObject();
        EvaluatorFilter<E> filter;

        if (top instanceof EvaluatorFilter) {
            // ignore Java warning about cast from Object to EvaluatorFilter<E>
            //noinspection unchecked
            filter = (EvaluatorFilter<E>) top;
            // Remove the filter from the stack now that the parser is at the end of it.
            context.popObject();
        } else {
            addError("Top of stack does not have Filter, but this thing: " + top);
            return;
        }

        // We expect the stack to be back at the parent container now.
        FilterAttachable<E> filterContainer = peekFilterContainer(context);
        if (filterContainer == null) {
            return;
        }

        if (filter.getOnMatch() == FilterReply.NEUTRAL && filter.getOnMismatch() == FilterReply.NEUTRAL) {
            addError("onMatch and onMismatch are both neutral; this filter is a no-op: " + filter);
        }

        filterContainer.addFilter(filter);
        filter.getEvaluator().start();
        filter.start();
        addInfo(String.format("Added %s to %s", filter, filterContainer));
    }

    protected FilterAttachable<E> peekFilterContainer(InterpretationContext context) {
        Object parentObject = context.peekObject();
        if (parentObject instanceof FilterAttachable) {
            @SuppressWarnings("unchecked") FilterAttachable<E> container =
                    (FilterAttachable<E>) parentObject;
            return container;
        } else {
            addError("Cannot attach filter to top of stack item " + parentObject);
            return null;
        }
    }

    protected String makeFilterName(InterpretationContext context, Attributes attributes) {
        String filterName = findAndSubst(context, attributes, NAME_ATTRIBUTE);

        if (Strings.isNullOrEmpty(filterName)) {
            // If a name wasn't provided, make one from the attributes
            // as they appear in the source (before variable substitution).
            filterName = Strings.nullToEmpty(attributes.getValue(PREFIX_ATTRIBUTE)) + ":"
                    + attributes.getValue(MESSAGE_ATTRIBUTE);
        }

        return filterName;
    }

    private String findAndSubst(InterpretationContext context, Attributes attributes, String name) {
        return context.subst(attributes.getValue(name));
    }

    public static class RegexFilter<EE> extends EvaluatorFilter<EE> {
        RegexFilter(Context context, EventEvaluator<EE> evaluator, String name) {
            super();
            setName(name);
            setContext(context);
            setEvaluator(evaluator);
        }

        @Override
        public String toString() {
            // This is the whole reason for the subclass: a useful toString()
            return MoreObjects.toStringHelper(this)
                    .addValue(getName())
                    .toString();
        }
    }
}
