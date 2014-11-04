package com.fullcontact.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Type for String.
 *
 * @author Brandon Vargo
 */
final class StringType extends AbstractType<String> {
    public static final StringType INSTANCE = new StringType();

    private static final Charset CHARSET = Charset.forName("UTF-8");

    // singleton
    private StringType() {}

    @Override
    public ByteArray marshal(String s) {
        if(s.isEmpty()) {
            // special case to encode the empty string
            byte[] bytes = { 0x00 };
            return new ByteArray(bytes);
        }
        else {
            byte[] bytes = s.getBytes(CHARSET);
            return new ByteArray(bytes);
        }
    }

    @Override
    public String demarshal(ByteArray data) {
        byte[] b = data.toArray();
        // special case to decode the empty string
        if(b.length == 1 && b[0] == 0x00)
            return "";
        else
            return new String(b, CHARSET);
    }

    @Override
    public void write(String s, DataOutput dataOutput) throws IOException {
        IOUtil.writeUtf(s, dataOutput);
    }

    @Override
    public String read(DataInput dataInput) throws IOException {
        return IOUtil.readUtf(dataInput);
    }

    @Override
    public String toString() {
        return "StringType";
    }
}
