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
package com.espertech.esper.common.internal.epl.pattern.core;

import com.espertech.esper.common.client.type.EPTypeClass;

public interface EvalFactoryNode {
    EPTypeClass EPTYPE = new EPTypeClass(EvalFactoryNode.class);
    EPTypeClass EPTYPEARRAY = new EPTypeClass(EvalFactoryNode[].class);

    short getFactoryNodeId();

    String getTextForAudit();

    boolean isFilterChildNonQuitting();

    boolean isStateful();

    EvalNode makeEvalNode(PatternAgentInstanceContext agentInstanceContext, EvalNode parentNode);

    void accept(EvalFactoryNodeVisitor visitor);
}
