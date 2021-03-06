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
package com.espertech.esper.common.internal.epl.updatehelper;

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.*;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.*;
import com.espertech.esper.common.internal.epl.expression.codegen.CodegenLegoMethodExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.event.core.EventBeanCopyMethod;
import com.espertech.esper.common.internal.event.core.EventBeanCopyMethodForge;
import com.espertech.esper.common.internal.util.JavaClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenNames.*;
import static com.espertech.esper.common.internal.metrics.instrumentation.InstrumentationCode.instblock;
import static com.espertech.esper.common.internal.util.JavaClassHelper.isTypePrimitive;

public class EventBeanUpdateHelperForge {
    private final EventType eventType;
    private final EventBeanCopyMethodForge copyMethod;
    private final EventBeanUpdateItemForge[] updateItems;

    public EventBeanUpdateHelperForge(EventType eventType, EventBeanCopyMethodForge copyMethod, EventBeanUpdateItemForge[] updateItems) {
        this.eventType = eventType;
        this.copyMethod = copyMethod;
        this.updateItems = updateItems;
    }

    public CodegenExpression makeWCopy(CodegenMethodScope scope, CodegenClassScope classScope) {
        CodegenExpressionField copyMethodField = classScope.addFieldUnshared(true, EventBeanCopyMethod.EPTYPE, copyMethod.makeCopyMethodClassScoped(classScope));

        CodegenMethod method = scope.makeChild(EventBeanUpdateHelperWCopy.EPTYPE, this.getClass(), classScope);
        CodegenMethod updateInternal = makeUpdateInternal(method, classScope);

        CodegenExpressionNewAnonymousClass clazz = newAnonymousClass(method.getBlock(), EventBeanUpdateHelperWCopy.EPTYPE);
        CodegenMethod updateWCopy = CodegenMethod.makeParentNode(EventBean.EPTYPE, this.getClass(), classScope)
            .addParam(EventBean.EPTYPE, "matchingEvent")
            .addParam(EventBean.EPTYPEARRAY, NAME_EPS)
            .addParam(ExprEvaluatorContext.EPTYPE, NAME_EXPREVALCONTEXT);
        clazz.addMethod("updateWCopy", updateWCopy);

        updateWCopy.getBlock()
            .apply(instblock(classScope, "qInfraUpdate", ref("matchingEvent"), REF_EPS, constant(updateItems.length), constantTrue()))
            .declareVar(EventBean.EPTYPE, "copy", exprDotMethod(copyMethodField, "copy", ref("matchingEvent")))
            .assignArrayElement(REF_EPS, constant(0), ref("copy"))
            .assignArrayElement(REF_EPS, constant(2), ref("matchingEvent"))
            .localMethod(updateInternal, REF_EPS, REF_EXPREVALCONTEXT, ref("copy"))
            .apply(instblock(classScope, "aInfraUpdate", ref("copy")))
            .methodReturn(ref("copy"));

        method.getBlock().methodReturn(clazz);

        return localMethod(method);
    }

    public CodegenExpression makeNoCopy(CodegenMethodScope scope, CodegenClassScope classScope) {
        CodegenMethod method = scope.makeChild(EventBeanUpdateHelperNoCopy.EPTYPE, this.getClass(), classScope);
        CodegenMethod updateInternal = makeUpdateInternal(method, classScope);

        CodegenExpressionNewAnonymousClass clazz = newAnonymousClass(method.getBlock(), EventBeanUpdateHelperNoCopy.EPTYPE);

        CodegenMethod updateNoCopy = CodegenMethod.makeParentNode(EPTypePremade.VOID.getEPType(), this.getClass(), classScope)
            .addParam(EventBean.EPTYPE, "matchingEvent")
            .addParam(EventBean.EPTYPEARRAY, NAME_EPS)
            .addParam(ExprEvaluatorContext.EPTYPE, NAME_EXPREVALCONTEXT);
        clazz.addMethod("updateNoCopy", updateNoCopy);
        updateNoCopy.getBlock()
            .apply(instblock(classScope, "qInfraUpdate", ref("matchingEvent"), REF_EPS, constant(updateItems.length), constantFalse()))
            .localMethod(updateInternal, REF_EPS, REF_EXPREVALCONTEXT, ref("matchingEvent"))
            .apply(instblock(classScope, "aInfraUpdate", ref("matchingEvent")));

        CodegenMethod getUpdatedProperties = CodegenMethod.makeParentNode(EPTypePremade.STRINGARRAY.getEPType(), this.getClass(), classScope);
        clazz.addMethod("getUpdatedProperties", getUpdatedProperties);
        getUpdatedProperties.getBlock().methodReturn(constant(getUpdateItemsPropertyNames()));

        CodegenMethod isRequiresStream2InitialValueEvent = CodegenMethod.makeParentNode(EPTypePremade.BOOLEANPRIMITIVE.getEPType(), this.getClass(), classScope);
        clazz.addMethod("isRequiresStream2InitialValueEvent", isRequiresStream2InitialValueEvent);
        isRequiresStream2InitialValueEvent.getBlock().methodReturn(constant(isRequiresStream2InitialValueEvent()));

        method.getBlock().methodReturn(clazz);

        return localMethod(method);
    }

    public EventBeanUpdateItemForge[] getUpdateItems() {
        return updateItems;
    }

    private CodegenMethod makeUpdateInternal(CodegenMethodScope scope, CodegenClassScope classScope) {
        CodegenMethod method = scope.makeChildWithScope(EPTypePremade.VOID.getEPType(), this.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope)
            .addParam(EventBean.EPTYPEARRAY, NAME_EPS)
            .addParam(ExprEvaluatorContext.EPTYPE, NAME_EXPREVALCONTEXT)
            .addParam(EventBean.EPTYPE, "target");

        ExprForgeCodegenSymbol exprSymbol = new ExprForgeCodegenSymbol(true, true);
        CodegenMethod exprMethod = method.makeChildWithScope(EPTypePremade.VOID.getEPType(), CodegenLegoMethodExpression.class, exprSymbol, classScope).addParam(PARAMS);

        EPType[] types = new EPType[updateItems.length];
        for (int i = 0; i < updateItems.length; i++) {
            types[i] = updateItems[i].getExpression().getEvaluationType();
        }

        EventBeanUpdateItemForgeWExpressions[] forgeExpressions = new EventBeanUpdateItemForgeWExpressions[updateItems.length];
        for (int i = 0; i < updateItems.length; i++) {
            EPTypeClass nullableType = types[i] == null || types[i] == EPTypeNull.INSTANCE ? null : (EPTypeClass) types[i];
            EPTypeClass targetType = updateItems[i].isUseUntypedAssignment() ? EPTypePremade.OBJECT.getEPType() : nullableType;
            forgeExpressions[i] = updateItems[i].toExpression(targetType, exprMethod, exprSymbol, classScope);
        }

        exprSymbol.derivedSymbolsCodegen(method, method.getBlock(), classScope);

        method.getBlock().declareVar(eventType.getUnderlyingEPType(), "und", cast(eventType.getUnderlyingEPType(), exprDotUnderlying(ref("target"))));

        for (int i = 0; i < updateItems.length; i++) {
            EventBeanUpdateItemForge updateItem = updateItems[i];
            CodegenExpression rhs = forgeExpressions[i].getRhsExpression();
            if (updateItems[i].isUseTriggeringEvent()) {
                rhs = arrayAtIndex(ref(NAME_EPS), constant(1));
            }
            method.getBlock().apply(instblock(classScope, "qInfraUpdateRHSExpr", constant(i)));

            EPType type = types[i];
            if ((type == null || type == EPTypeNull.INSTANCE) && updateItem.getOptionalWriter() != null) {
                method.getBlock().expression(updateItem.getOptionalWriter().writeCodegen(constantNull(), ref("und"), ref("target"), method, classScope));
                continue;
            }

            if (type != null && (JavaClassHelper.isTypeVoid(type) || (updateItem.getOptionalWriter() == null && updateItem.getOptionalArray() == null))) {
                method.getBlock()
                    .expression(rhs)
                    .apply(instblock(classScope, "aInfraUpdateRHSExpr", constantNull()));
                continue;
            }

            EPTypeClass targetType = EPTypePremade.OBJECT.getEPType();
            if (!updateItems[i].isUseUntypedAssignment() && type instanceof EPTypeClass) {
                targetType = (EPTypeClass) type;
            }

            CodegenExpressionRef ref = ref("r" + i);
            method.getBlock().declareVar(targetType, ref.getRef(), rhs);

            CodegenExpression assigned = ref;
            if (updateItem.getOptionalWidener() != null) {
                assigned = updateItem.getOptionalWidener().widenCodegen(ref, method, classScope);
            }

            if (updateItem.getOptionalArray() != null) {
                // handle array value with array index expression
                CodegenExpressionRef index = ref("i" + i);
                CodegenExpressionRef array = ref("a" + i);
                EventBeanUpdateItemArray arraySet = updateItem.getOptionalArray();
                CodegenBlock arrayBlock;
                boolean arrayOfPrimitiveNullRHS = arraySet.getArrayType().getType().getComponentType().isPrimitive() && (type == null || type == EPTypeNull.INSTANCE || !isTypePrimitive(type));
                if (arrayOfPrimitiveNullRHS) {
                    arrayBlock = method.getBlock()
                        .ifNull(ref)
                        .staticMethod(EventBeanUpdateHelperForge.class, "logWarnWhenNullAndNotNullable", constant(updateItem.getOptionalPropertyName()))
                        .ifElse();
                } else {
                    arrayBlock = method.getBlock();
                }
                arrayBlock.declareVar(EPTypePremade.INTEGERBOXED.getEPType(), index.getRef(), forgeExpressions[i].getOptionalArrayExpressions().getIndex())
                    .ifRefNotNull(index.getRef())
                    .declareVar(arraySet.getArrayType(), array.getRef(), forgeExpressions[i].getOptionalArrayExpressions().getArrayGet())
                    .ifRefNotNull(array.getRef())
                    .ifCondition(relational(index, CodegenExpressionRelational.CodegenRelational.LT, arrayLength(array)))
                    .assignArrayElement(array, cast(EPTypePremade.INTEGERPRIMITIVE.getEPType(), index), assigned)
                    .ifElse()
                    .blockThrow(newInstance(EPException.EPTYPE, concat(constant("Array length "), arrayLength(array), constant(" less than index "), index, constant(" for property '" + updateItems[i].getOptionalArray().getPropertyName() + "'"))))
                    .blockEnd()
                    .blockEnd();
                if (arrayOfPrimitiveNullRHS) {
                    arrayBlock.blockEnd();
                }
            } else {
                // handle regular values
                if (!isTypePrimitive(type) && updateItem.isNotNullableField()) {
                    method.getBlock()
                        .ifNull(ref)
                        .staticMethod(EventBeanUpdateHelperForge.class, "logWarnWhenNullAndNotNullable", constant(updateItem.getOptionalPropertyName()))
                        .ifElse()
                        .expression(updateItem.getOptionalWriter().writeCodegen(assigned, ref("und"), ref("target"), method, classScope))
                        .blockEnd();
                } else {
                    method.getBlock().expression(updateItem.getOptionalWriter().writeCodegen(assigned, ref("und"), ref("target"), method, classScope));
                }
            }

            method.getBlock().apply(instblock(classScope, "aInfraUpdateRHSExpr", assigned));
        }

        return method;
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param propertyName name
     */
    public static void logWarnWhenNullAndNotNullable(String propertyName) {
        log.warn("Null value returned by expression for assignment to property '" + propertyName + "' is ignored as the property type is not nullable for expression");
    }

    private static final Logger log = LoggerFactory.getLogger(EventBeanUpdateHelperForge.class);

    public boolean isRequiresStream2InitialValueEvent() {
        return copyMethod != null;
    }

    public String[] getUpdateItemsPropertyNames() {
        List<String> properties = new ArrayList<>();
        for (EventBeanUpdateItemForge item : updateItems) {
            if (item.getOptionalPropertyName() != null) {
                properties.add(item.getOptionalPropertyName());
            }
        }
        return properties.toArray(new String[properties.size()]);
    }
}
