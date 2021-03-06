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
package com.espertech.esper.common.internal.epl.datetime.dtlocal;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.datetime.calop.CalendarOp;
import com.espertech.esper.common.internal.epl.datetime.reformatop.ReformatOp;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.expression.time.abacus.TimeAbacus;
import com.espertech.esper.common.internal.settings.RuntimeSettingsTimeZoneField;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class DTLocalLongOpsReformatEval extends DTLocalEvaluatorCalopReformatBase {

    private final TimeZone timeZone;
    private final TimeAbacus timeAbacus;

    public DTLocalLongOpsReformatEval(List<CalendarOp> calendarOps, ReformatOp reformatOp, TimeZone timeZone, TimeAbacus timeAbacus) {
        super(calendarOps, reformatOp);
        this.timeZone = timeZone;
        this.timeAbacus = timeAbacus;
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        Calendar cal = Calendar.getInstance(timeZone);
        timeAbacus.calendarSet((Long) target, cal);
        DTLocalUtil.evaluateCalOpsCalendar(calendarOps, cal, eventsPerStream, isNewData, exprEvaluatorContext);
        return reformatOp.evaluate(cal, eventsPerStream, isNewData, exprEvaluatorContext);
    }

    public static CodegenExpression codegen(DTLocalLongOpsReformatForge forge, CodegenExpression inner, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenExpression timeZoneField = codegenClassScope.addOrGetFieldSharable(RuntimeSettingsTimeZoneField.INSTANCE);
        CodegenMethod methodNode = codegenMethodScope.makeChild(forge.reformatForge.getReturnType(), DTLocalLongOpsReformatEval.class, codegenClassScope).addParam(EPTypePremade.LONGPRIMITIVE.getEPType(), "target");

        CodegenBlock block = methodNode.getBlock()
                .declareVar(EPTypePremade.CALENDAR.getEPType(), "cal", staticMethod(Calendar.class, "getInstance", timeZoneField))
                .expression(forge.timeAbacus.calendarSetCodegen(ref("target"), ref("cal"), methodNode, codegenClassScope));
        DTLocalUtil.evaluateCalOpsCalendarCodegen(block, forge.calendarForges, ref("cal"), methodNode, exprSymbol, codegenClassScope);
        block.methodReturn(forge.reformatForge.codegenCal(ref("cal"), methodNode, exprSymbol, codegenClassScope));
        return localMethod(methodNode, inner);
    }
}
