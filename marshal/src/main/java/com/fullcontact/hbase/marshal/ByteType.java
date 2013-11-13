package com.fullcontact.hbase.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Type for Byte.
 *
 * @author Brandon Vargo
 */
final class ByteType extends AbstractType<Byte> {
    public static final ByteType INSTANCE = new ByteType();

    // singleton
    private ByteType() {}

    @Override
    public ByteArray marshal(Byte b) {
        byte[] array = { b };
        return new ByteArray(array);
    }

    @Override
    public Byte demarshal(ByteArray data) {
        return data.getAt(0);
    }

    @Override
    public void write(Byte b, DataOutput dataOutput) throws IOException {
        dataOutput.write(b);
    }

    @Override
    public Byte read(DataInput dataInput) throws IOException {
        return dataInput.readByte();
    }

    @Override
    public String toString() {
        return "ByteType";
    }
}
