package com.fullcontact.marshal.mapreduce;

import com.fullcontact.marshal.Marshal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Tests for LegacyMarshalWritable.
 *
 * @author Brandon Vargo
 */
@RunWith(JUnit4.class)
public class LegacyMarshalWritableTest {
    @Test
    public void testDeserialization() throws IOException {
        byte[] bytes = { 0, 0, 0, 4, 5, 0, 7, 115, 116, 114, 105, 110, 103, 49, 5, 0, 7, 115, 116,
            114, 105, 110, 103, 50, 3, 0, 0, 0, 3, 2, 64, 9, 30, -72, 81, -21, -123, 31 };

        LegacyMarshalWritable output = new LegacyMarshalWritable();
        output.readFields(new DataInputStream(new ByteArrayInputStream(bytes)));

        Marshal m = output.get();
        assertEquals("string1", m.getStringAt(0));
        assertEquals("string2", m.getStringAt(1));
        assertEquals(3, m.getIntegerAt(2));
        assertEquals(3.14, m.getDoubleAt(3), 0.0);
    }
}
