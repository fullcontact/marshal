package com.fullcontact.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Type for Long.
 *
 * @author Brandon Vargo
 */
final class LongType extends AbstractType<Long> {
    public static final LongType INSTANCE = new LongType();

    // singleton
    private LongType() {}

    @Override
    public ByteArray marshal(Long l) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(l);
        return new ByteArray(bytes);
    }

    @Override
    public Long demarshal(ByteArray data) {
        return ByteBuffer.wrap(data.toArray()).getLong();
    }

    @Override
    public void write(Long l, DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(l);
    }

    @Override
    public Long read(DataInput dataInput) throws IOException {
        return dataInput.readLong();
    }

    @Override
    public String toString() {
        return "LongType";
    }
}
