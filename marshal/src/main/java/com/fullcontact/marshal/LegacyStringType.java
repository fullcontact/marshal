package com.fullcontact.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A legacy string type.
 *
 * The Marshal encoding is idential to the current string. The DataInput/DataOutput encoding uses a
 * length short instead of a varint, as in the current version of String.
 *
 * This type was original type 5for Marshals. However, since the Marshal serailization is the exact
 * same between this and the current format, and there are few uses of the serialized
 * DataInput/DataOutput, it would cause less breakage to demote this to a fake type ID and then use
 * a compatibility mode to deserialize existing data where required.
 *
 * @author Brandon Vargo
 */
final class LegacyStringType extends AbstractType<String> {
    public static final LegacyStringType INSTANCE = new LegacyStringType();

    private static final Charset CHARSET = Charset.forName("UTF-8");

    // singleton
    private LegacyStringType() {}

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
        dataOutput.writeUTF(s);
    }

    @Override
    public String read(DataInput dataInput) throws IOException {
        return dataInput.readUTF();
    }

    @Override
    public String toString() {
        return "LegacyStringType";
    }
}
