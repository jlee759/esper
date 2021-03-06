/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.runtime.internal.filtersvcimpl;

import com.espertech.esper.common.internal.epl.expression.core.ExprFilterSpecLookupable;
import com.espertech.esper.common.internal.filterspec.DoubleRange;
import com.espertech.esper.common.internal.filterspec.FilterOperator;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Index for filter parameter constants for the range operators (range open/closed/half).
 * The implementation is based on the SortedMap implementation of TreeMap and stores only expression
 * parameter values of type DoubleRange.
 */
public abstract class FilterParamIndexDoubleRangeBase extends FilterParamIndexLookupableBase {
    protected final TreeMap<DoubleRange, EventEvaluator> ranges;
    protected EventEvaluator rangesNullEndpoints;
    private final ReadWriteLock rangesRWLock;

    protected double largestRangeValueDouble = Double.MIN_VALUE;

    protected FilterParamIndexDoubleRangeBase(ExprFilterSpecLookupable lookupable, ReadWriteLock readWriteLock, FilterOperator filterOperator) {
        super(filterOperator, lookupable);

        ranges = new TreeMap<>(DoubleRangeComparator.INSTANCE);
        rangesRWLock = readWriteLock;
    }

    public final EventEvaluator get(Object expressionValue) {
        if (!(expressionValue instanceof DoubleRange)) {
            throw new IllegalArgumentException("Supplied expressionValue must be of type DoubleRange");
        }

        DoubleRange range = (DoubleRange) expressionValue;
        if ((range.getMax() == null) || (range.getMin() == null)) {
            return rangesNullEndpoints;
        }

        return ranges.get(range);
    }

    public final void put(Object expressionValue, EventEvaluator matcher) {
        if (!(expressionValue instanceof DoubleRange)) {
            throw new IllegalArgumentException("Supplied expressionValue must be of type DoubleRange");
        }

        DoubleRange range = (DoubleRange) expressionValue;
        if ((range.getMax() == null) || (range.getMin() == null)) {
            rangesNullEndpoints = matcher;
            return;
        }

        if (Math.abs(range.getMax() - range.getMin()) > largestRangeValueDouble) {
            largestRangeValueDouble = Math.abs(range.getMax() - range.getMin());
        }

        ranges.put(range, matcher);
    }

    public final void remove(Object filterConstant) {
        DoubleRange range = (DoubleRange) filterConstant;

        if ((range.getMax() == null) || (range.getMin() == null)) {
            rangesNullEndpoints = null;
            return;
        }
        ranges.remove(range);
    }

    public final int sizeExpensive() {
        return ranges.size();
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public final ReadWriteLock getReadWriteLock() {
        return rangesRWLock;
    }

    public void getTraverseStatement(EventTypeIndexTraverse traverse, Set<Integer> statementIds, ArrayDeque<FilterItem> evaluatorStack) {
        for (Map.Entry<DoubleRange, EventEvaluator> entry : ranges.entrySet()) {
            evaluatorStack.add(new FilterItem(lookupable.getExpression(), getFilterOperator(), entry.getKey(), this));
            entry.getValue().getTraverseStatement(traverse, statementIds, evaluatorStack);
            evaluatorStack.removeLast();
        }
    }
}
