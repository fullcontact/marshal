package com.fullcontact.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Type for Integer.
 *
 * @author Brandon Vargo
 */
final class IntegerType extends AbstractType<Integer> {
    public static final IntegerType INSTANCE = new IntegerType();

    // singleton
    private IntegerType() {}

    @Override
    public ByteArray marshal(Integer i) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putInt(i);
        return new ByteArray(bytes);
    }

    @Override
    public Integer demarshal(ByteArray data) {
        return ByteBuffer.wrap(data.toArray()).getInt();
    }

    @Override
    public void write(Integer i, DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(i);
    }

    @Override
    public Integer read(DataInput dataInput) throws IOException {
        return dataInput.readInt();
    }

    @Override
    public String toString() {
        return "IntegerType";
    }
}
