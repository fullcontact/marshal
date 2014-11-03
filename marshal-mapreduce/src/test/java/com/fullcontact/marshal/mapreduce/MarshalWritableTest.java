package com.fullcontact.marshal.mapreduce;

import com.fullcontact.marshal.Marshal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Tests for MarshalWritable.
 *
 * @author Brandon Vargo
 */
@RunWith(JUnit4.class)
public class MarshalWritableTest {
    @Test
    public void testSerializationDeserialization() throws IOException {
        String string1 = "string1";
        String string2 = "string2";
        int i = 3;
        double d = 3.14;

        Marshal marshal = Marshal.builder()
            .addString(string1)
            .addString(string2)
            .addInteger(i)
            .addDouble(d)
            .build();
        MarshalWritable input = new MarshalWritable(marshal);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        input.write(new DataOutputStream(baos));

        MarshalWritable output = new MarshalWritable();
        output.readFields(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        assertEquals(input, output);
    }

    @Test
    public void testCompare__comparator() throws IOException {
        String string1 = "string1";
        String string2 = "string22";

        Marshal marshal1 = Marshal.builder()
            .addString(string1)
            .addString(string2)
            .addInteger(-23)
            .addInteger(55)
            .build();
        MarshalWritable input1 = new MarshalWritable(marshal1);
        ByteArrayOutputStream input1Stream = new ByteArrayOutputStream(8192);
        input1.write(new DataOutputStream(input1Stream));
        byte[] input1Bytes = input1Stream.toByteArray();

        Marshal marshal2 = Marshal.builder()
            .addString(string2)
            .addString(string1)
            .addInteger(-23)
            .build();
        MarshalWritable input2 = new MarshalWritable(marshal2);
        ByteArrayOutputStream input2Stream = new ByteArrayOutputStream(8192);
        input2.write(new DataOutputStream(input2Stream));
        byte[] input2Bytes = input2Stream.toByteArray();

        MarshalWritable.Comparator comparator = new MarshalWritable.Comparator();

        // comparison on the marshal writables is undefined other than on equality, but is guaranteed to be stable
        int oneToTwo = comparator.compare(input1Bytes, 0, 32, input2Bytes, 0, 27);
        int twoToOne = comparator.compare(input2Bytes, 0, 27, input1Bytes, 0, 32);
        // one should be positive, and the other should be negative
        assertTrue(oneToTwo * twoToOne < 0);

        assertTrue(comparator.compare(input1Bytes, 0, 32, input1Bytes, 0, 32) == 0);
        assertTrue(comparator.compare(input2Bytes, 0, 27, input2Bytes, 0, 27) == 0);
    }
}
