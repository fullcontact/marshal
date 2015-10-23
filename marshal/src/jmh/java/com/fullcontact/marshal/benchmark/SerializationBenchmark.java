package com.fullcontact.marshal.benchmark;

import com.fullcontact.marshal.ByteArray;
import com.fullcontact.marshal.Marshal;
import com.fullcontact.marshal.MarshalException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

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

    // ensure encoded format is correct
    byte[] serialized = {
        // byte array
        1,
        0, 1, 2, 3, 4, 5,
        // separator
        (byte)0xFE,
        // double
        2,
        (byte)-64, 9, 30, (byte)-72, 81, (byte)-21, (byte)-123, 31,
        // separator
        (byte)0xFE,
        // integer
        3,
        0, 0, 0, 22,
        // separator
        (byte)0xFE,
        // long
        4,
        1, (byte)-74, (byte)-101, 75, (byte)-90, 48, (byte)-13, 78,
        // separator
        (byte)0xFE,
        // string
        5,
        32, 84, 104, (byte)-61, (byte)-85, 32, 113, 117, (byte)-61, (byte)-83, 99, 107, 32, 98, 114, (byte)-61,
        (byte)-74, 119, 110, 32, 102, 111, 120, 32, 106, (byte)-61, (byte)-71, 109, 112, 115, 32, 111, 118, 101,
        114, 32, 116, 104, 101, 32, 108, 97, 122, 121, 32, 100, 111, 103, 33, 32,
        // separator
        (byte)0xFE,
        // empty Marshal
        6,
        (byte)0xFE, (byte)0xFE,
        // separator
        (byte)0xFE,
        // single byte
        0,
        (byte)-128,
        // separator
        (byte)0xFE,
        // empty Marshal
        6,
        (byte)0xFE, (byte)0xFE,
        // terminating separator
        (byte)0xFE
    };

    @Benchmark
    @Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(1)
    public ByteArray testSerializationSpeed_basic() throws MarshalException {
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

        return input.toByteArray();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(1)
    public Marshal testDeserializationSpeed_basic() throws MarshalException {
        return Marshal.fromBytes(serialized);
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(1)
    public byte[] testReserializeSpeed() throws MarshalException {
        Marshal marshal = Marshal.fromBytes(serialized);
        return marshal.toBytes();
    }
}
