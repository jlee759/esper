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
package com.espertech.esper.common.internal.epl.historical.common;

import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.util.Arrays;

/**
 * Descriptor for an index requirement on a historical stream.
 * <p>
 * Saves and compares the properties indexed and their types, as well as the types of key properties to
 * account for coercion.
 */
public class HistoricalStreamIndexDesc {
    private final String[] indexProperties;
    private final EPTypeClass[] indexPropTypes;
    private final EPType[] keyPropTypes;

    /**
     * Ctor.
     *
     * @param indexProperties index properties
     * @param indexPropTypes  index property types
     * @param keyPropTypes    key property types
     */
    public HistoricalStreamIndexDesc(String[] indexProperties, EPTypeClass[] indexPropTypes, EPType[] keyPropTypes) {
        this.indexProperties = indexProperties;
        this.indexPropTypes = indexPropTypes;
        this.keyPropTypes = keyPropTypes;
    }

    /**
     * Returns index properties.
     *
     * @return index properties
     */
    public String[] getIndexProperties() {
        return indexProperties;
    }

    /**
     * Returns index property types.
     *
     * @return index property types
     */
    public EPTypeClass[] getIndexPropTypes() {
        return indexPropTypes;
    }

    /**
     * Returns key property types.
     *
     * @return key property types
     */
    public EPType[] getKeyPropTypes() {
        return keyPropTypes;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoricalStreamIndexDesc that = (HistoricalStreamIndexDesc) o;

        if (!Arrays.equals(indexPropTypes, that.indexPropTypes)) return false;
        if (!Arrays.equals(indexProperties, that.indexProperties)) return false;
        if (!Arrays.equals(keyPropTypes, that.keyPropTypes)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = Arrays.hashCode(indexProperties);
        result = 31 * result + Arrays.hashCode(indexPropTypes);
        result = 31 * result + Arrays.hashCode(keyPropTypes);
        return result;
    }
}
