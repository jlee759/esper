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
package com.espertech.esper.common.internal.serde.serdeset.builtin;

import com.espertech.esper.common.client.serde.DataInputOutputSerde;
import com.espertech.esper.common.client.serde.EventBeanCollatedWriter;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class DIOCollectionFloatNullableSerde implements DataInputOutputSerde<Collection<Float>> {
    public final static EPTypeClass EPTYPE = new EPTypeClass(DIOCollectionFloatNullableSerde.class);

    public final static DIOCollectionFloatNullableSerde INSTANCE = new DIOCollectionFloatNullableSerde();

    private DIOCollectionFloatNullableSerde() {
    }

    public void write(Collection<Float> object, DataOutput output) throws IOException {
        writeInternal(object, output);
    }

    public Collection<Float> read(DataInput input) throws IOException {
        return readInternal(input);
    }

    public void write(Collection<Float> object, DataOutput output, byte[] unitKey, EventBeanCollatedWriter writer) throws IOException {
        writeInternal(object, output);
    }

    public Collection<Float> read(DataInput input, byte[] unitKey) throws IOException {
        return readInternal(input);
    }

    private void writeInternal(Collection<Float> object, DataOutput output) throws IOException {
        if (object == null) {
            output.writeInt(-1);
            return;
        }
        output.writeInt(object.size());
        for (Float i : object) {
            DIONullableFloatSerde.INSTANCE.write(i, output);
        }
    }

    private Collection<Float> readInternal(DataInput input) throws IOException {
        int len = input.readInt();
        if (len == -1) {
            return null;
        }
        Collection<Float> array = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            array.add(DIONullableFloatSerde.INSTANCE.read(input));
        }
        return array;
    }
}
