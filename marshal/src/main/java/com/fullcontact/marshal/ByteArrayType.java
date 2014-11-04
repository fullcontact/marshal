package com.fullcontact.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Type for ByteArray.
 *
 * @author Brandon Vargo
 */
final class ByteArrayType extends AbstractType<ByteArray> {
    public static final ByteArrayType INSTANCE = new ByteArrayType();

    // singleton
    private ByteArrayType() {}

    @Override
    public ByteArray marshal(ByteArray byteArray) {
        return byteArray;
    }

    @Override
    public ByteArray demarshal(ByteArray data) {
        return data;
    }

    @Override
    public void write(ByteArray byteArray, DataOutput dataOutput) throws IOException {
        byte[] bytes = byteArray.toArray();
        IOUtil.writeVarInt(bytes.length, dataOutput);
        dataOutput.write(bytes);
    }

    @Override
    public ByteArray read(DataInput dataInput) throws IOException {
        int length = IOUtil.readVarInt(dataInput);
        byte[] bytes = new byte[length];
        dataInput.readFully(bytes);
        return new ByteArray(bytes);
    }

    @Override
    public String toString() {
        return "ByteArrayType";
    }
}
