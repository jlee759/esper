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
package com.espertech.esper.common.internal.epl.join.assemble;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.context.aifactory.core.SAIFFInitializeSymbol;
import com.espertech.esper.common.internal.util.IndentWriter;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constant;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.newInstance;

/**
 * Assembly factory node for an event stream that is a root with a two or more child nodes below it.
 */
public class RootCartProdAssemblyNodeFactory extends BaseAssemblyNodeFactory {
    public final static EPTypeClass EPTYPE = new EPTypeClass(RootCartProdAssemblyNodeFactory.class);

    private final int[] childStreamIndex; // maintain mapping of stream number to index in array
    private boolean allSubStreamsOptional;

    /**
     * Ctor.
     *
     * @param streamNum             - is the stream number
     * @param numStreams            - is the number of streams
     * @param allSubStreamsOptional - true if all substreams are optional and none are required
     */
    public RootCartProdAssemblyNodeFactory(int streamNum, int numStreams, boolean allSubStreamsOptional) {
        super(streamNum, numStreams);
        this.allSubStreamsOptional = allSubStreamsOptional;
        childStreamIndex = new int[numStreams];
    }

    @Override
    public void addChild(BaseAssemblyNodeFactory childNode) {
        childStreamIndex[childNode.getStreamNum()] = childNodes.size();
        super.addChild(childNode);
    }

    public void print(IndentWriter indentWriter) {
        indentWriter.println("RootCartProdAssemblyNode streamNum=" + streamNum);
    }

    public BaseAssemblyNode makeAssemblerUnassociated() {
        return new RootCartProdAssemblyNode(streamNum, numStreams, allSubStreamsOptional, childStreamIndex);
    }

    public CodegenExpression make(CodegenMethodScope parent, SAIFFInitializeSymbol symbols, CodegenClassScope classScope) {
        return newInstance(RootCartProdAssemblyNodeFactory.EPTYPE, constant(streamNum), constant(numStreams), constant(allSubStreamsOptional));
    }
}
