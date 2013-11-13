package com.fullcontact.hbase.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Type for Double.
 *
 * @author Brandon Vargo
 */
final class DoubleType extends AbstractType<Double> {
    public static final DoubleType INSTANCE = new DoubleType();

    // singleton
    private DoubleType() {}

    @Override
    public ByteArray marshal(Double d) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(d);
        return new ByteArray(bytes);
    }

    @Override
    public Double demarshal(ByteArray data) {
        return ByteBuffer.wrap(data.toArray()).getDouble();
    }

    @Override
    public void write(Double d, DataOutput dataOutput) throws IOException {
        dataOutput.writeDouble(d);
    }

    @Override
    public Double read(DataInput dataInput) throws IOException {
        return dataInput.readDouble();
    }

    @Override
    public String toString() {
        return "DoubleType";
    }
}
