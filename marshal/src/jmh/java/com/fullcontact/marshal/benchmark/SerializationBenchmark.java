package com.fullcontact.marshal.benchmark;

import com.fullcontact.marshal.ByteArray;
import com.fullcontact.marshal.Marshal;
import com.fullcontact.marshal.MarshalException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Serialization benchmarks
 *
 * @author Michael Rose (xorlev)
 */
@State(Scope.Benchmark)
public class SerializationBenchmark {
    private byte[] bytes = {0, 1, 2, 3, 4, 5 };
    private ByteArray byteArray = new ByteArray(bytes);
    private double d = -3.14d;
    private int i = 22;
    private long l = 123456789012345678L;
    private String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";
    private byte b = (byte)0x80;

    @Benchmark
    public Marshal testSerializationSpeed() throws MarshalException {
        Marshal input = Marshal.builder()
                               .addByteArray(byteArray)
                               .addDouble(d)
                               .addInteger(i)
                               .addLong(l)
                               .addString(s)
                               .addMarshal(Marshal.EMPTY)
                               .addByte(b)
                               .addMarshal(Marshal.EMPTY)
                               .build();

        ByteArray mBytes = input.toByteArray();

        return Marshal.fromBytes(mBytes);
    }
}
