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
package com.espertech.esper.common.internal.supportunit.util;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.util.ClassHelperGenericType;

import java.io.StringWriter;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constantNull;

public class SupportExprNode extends ExprNodeBase implements ExprForge, ExprEvaluator {
    private static int validateCount;

    private EPTypeClass type;
    private Object value;
    private int validateCountSnapshot;

    public static void setValidateCount(int validateCount) {
        SupportExprNode.validateCount = validateCount;
    }

    public SupportExprNode(Class type) {
        this.type = type == null ? null : ClassHelperGenericType.getClassEPType(type);
        this.value = null;
    }

    public SupportExprNode(Object value) {
        this.type = value == null ? null : ClassHelperGenericType.getClassEPType(value.getClass());
        this.value = value;
    }

    public SupportExprNode(Object value, Class type) {
        this.value = value;
        this.type = ClassHelperGenericType.getClassEPType(type);
    }

    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return constantNull();
    }

    public EPTypeClass getEvaluationType() {
        return type;
    }

    public ExprForge getForge() {
        return this;
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        // Keep a count for if and when this was validated
        validateCount++;
        validateCountSnapshot = validateCount;
        return null;
    }

    public boolean isConstantResult() {
        return false;
    }

    public int getValidateCountSnapshot() {
        return validateCountSnapshot;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void toPrecedenceFreeEPL(StringWriter writer, ExprNodeRenderableFlags flags) {
        if (value instanceof String) {
            writer.append("\"" + value + "\"");
        } else {
            if (value == null) {
                writer.append("null");
            } else {
                writer.append(value.toString());
            }
        }
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.UNARY;
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        if (!(node instanceof SupportExprNode)) {
            return false;
        }
        SupportExprNode other = (SupportExprNode) node;
        return value.equals(other.value);
    }

    public ExprForgeConstantType getForgeConstantType() {
        return ExprForgeConstantType.NONCONST;
    }
}
