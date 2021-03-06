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
package com.espertech.esper.common.internal.event.core;

import com.espertech.esper.common.client.type.EPType;

import java.lang.reflect.Method;

/**
 * Descriptor for writable properties.
 */
public class WriteablePropertyDescriptor {
    private final String propertyName;
    private final EPType type;
    private final Method writeMethod;
    private final boolean fragment;

    /**
     * Ctor.
     * @param propertyName name of property
     * @param type         type
     * @param writeMethod  optional write methods
     * @param fragment whether the property is itself an event or array of events
     */
    public WriteablePropertyDescriptor(String propertyName, EPType type, Method writeMethod, boolean fragment) {
        this.propertyName = propertyName;
        this.type = type;
        this.writeMethod = writeMethod;
        this.fragment = fragment;
    }

    /**
     * Returns property name.
     *
     * @return property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns property type.
     *
     * @return property type
     */
    public EPType getType() {
        return type;
    }

    /**
     * Returns write methods.
     *
     * @return write methods
     */
    public Method getWriteMethod() {
        return writeMethod;
    }

    public boolean isFragment() {
        return fragment;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WriteablePropertyDescriptor that = (WriteablePropertyDescriptor) o;

        if (!propertyName.equals(that.propertyName)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return propertyName.hashCode();
    }
}
