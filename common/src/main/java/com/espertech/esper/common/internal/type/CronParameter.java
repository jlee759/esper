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
package com.espertech.esper.common.internal.type;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.io.Serializable;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constant;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.newInstance;

/**
 * Hold parameters for timer:at.
 */
public class CronParameter implements Serializable {
    public final static EPTypeClass EPTYPE = new EPTypeClass(CronParameter.class);
    private static final long serialVersionUID = -4006350378033980878L;

    private CronOperatorEnum operator;
    private Integer day, month;

    /**
     * Ctor.
     *
     * @param operator is the operator as text
     * @param day      is the day text
     */
    public CronParameter(CronOperatorEnum operator, Integer day) {
        this.operator = operator;
        this.day = day;
    }

    public CronParameter(CronOperatorEnum operator, Integer day, Integer month) {
        this.operator = operator;
        this.day = day;
        this.month = month;
    }

    public CodegenExpression make() {
        return newInstance(CronParameter.EPTYPE, constant(operator), constant(day), constant(month));
    }

    /**
     * Sets the month value.
     *
     * @param month to set
     */
    public void setMonth(int month) {
        this.month = month - 1;
    }

    public String formatted() {
        return operator + "(day " + day + " month " + month + ")";
    }

    public CronOperatorEnum getOperator() {
        return operator;
    }

    public Integer getDay() {
        return day;
    }

    public Integer getMonth() {
        return month;
    }
}
