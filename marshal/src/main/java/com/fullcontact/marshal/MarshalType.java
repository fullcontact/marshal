package com.fullcontact.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Type for Marshal.
 *
 * @author Brandon Vargo
 */
final class MarshalType extends AbstractType<Marshal> {
    public static final MarshalType INSTANCE = new MarshalType();

    // singleton
    private MarshalType() {}

    @Override
    public ByteArray marshal(Marshal m) {
        return m.toByteArray();
    }

    @Override
    public Marshal demarshal(ByteArray data) {
        return new Marshal(data);
    }

    @Override
    public void write(Marshal m, DataOutput dataOutput) throws IOException {
        m.write(dataOutput);
    }

    @Override
    public Marshal read(DataInput dataInput) throws IOException {
        return Marshal.read(dataInput);
    }

    @Override
    public String toString() {
        return "MarshalType";
    }
}
