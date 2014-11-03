package com.fullcontact.marshal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests for Marshal.
 *
 * @author Brandon Vargo <brandon@fullcontact.com>
 */
@RunWith(JUnit4.class)
public class MarshalTest {
    @Test
    public void testEscape() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] escaped = Marshal.escape(new ByteArray(original), (byte)2).toArray();
        byte[] expected = { 0, 1, 2, 2, 3 };
        assertArrayEquals(expected, escaped);
    }

    @Test
    public void testEscape__beginning() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] escaped = Marshal.escape(new ByteArray(original), (byte)0).toArray();
        byte[] expected = { 0, 0, 1, 2, 3 };
        assertArrayEquals(expected, escaped);
    }

    @Test
    public void testEscape__end() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] escaped = Marshal.escape(new ByteArray(original), (byte)3).toArray();
        byte[] expected = { 0, 1, 2, 3, 3 };
        assertArrayEquals(expected, escaped);
    }

    @Test
    public void testEscape__none() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] escaped = Marshal.escape(new ByteArray(original), (byte)5).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, escaped);
    }

    @Test
    public void testEscape__double() {
        byte[] original = { 0, 1, 1, 2, 3 };
        byte[] escaped = Marshal.escape(new ByteArray(original), (byte)1).toArray();
        byte[] expected = { 0, 1, 1, 1, 1, 2, 3 };
        assertArrayEquals(expected, escaped);
    }

    @Test
    public void testUnescape() {
        byte[] original = { 0, 1, 2, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)2).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__beginning() {
        byte[] original = { 0, 0, 1, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)0).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__end() {
        byte[] original = { 0, 1, 2, 3, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)3).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__none() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)5).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__double() {
        byte[] original = { 0, 1, 1, 1, 1, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)1).toArray();
        byte[] expected = { 0, 1, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__invalid_start() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)0).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__invalid_middle() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)1).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testUnescape__invalid_end() {
        byte[] original = { 0, 1, 2, 3 };
        byte[] unescaped = Marshal.unescape(new ByteArray(original), (byte)3).toArray();
        byte[] expected = { 0, 1, 2, 3 };
        assertArrayEquals(expected, unescaped);
    }

    @Test
    public void testFindSeparator__beginning() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(0, Marshal.findSeparator(a, (byte)0));
    }

    @Test
    public void testFindSeparator__middle() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(2, Marshal.findSeparator(a, (byte)2));
    }

    @Test
    public void testFindSeparator__end() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(3, Marshal.findSeparator(a, (byte)3));
    }

    @Test
    public void testFindSeparator__skippedEscape_beginning() {
        byte[] bytes = { 0, 0, 1, 2, 0, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(4, Marshal.findSeparator(a, (byte)0));
    }

    @Test
    public void testFindSeparator__skippedEscape_middle() {
        byte[] bytes = { 0, 1, 1, 2, 1, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(4, Marshal.findSeparator(a, (byte)1));
    }

    @Test
    public void testFindSeparator__skippedEscape_end() {
        byte[] bytes = { 0, 1, 2, 3, 3, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(5, Marshal.findSeparator(a, (byte)3));
    }

    @Test
    public void testFindSeparator__skippedEscape_endNoSeparator() {
        byte[] bytes = { 0, 1, 2, 3, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(5, Marshal.findSeparator(a, (byte)3));
    }

    @Test
    public void testFindSeparator__empty() {
        byte[] bytes = { };
        ByteArray a = new ByteArray(bytes);
        assertEquals(0, Marshal.findSeparator(a, (byte)0));
    }


    @Test
    public void testFindSeparator__noSeparator() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(4, Marshal.findSeparator(a, (byte)9));
    }

    @Test
    public void testGetAt() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";
        byte b = (byte)0x80;

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .addByte(b)
            .addMarshal(Marshal.EMPTY)
            .build();

        assertEquals(7, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));
        assertEquals(b, m.getByteAt(5));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(6));
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__byte() {
        int i = 22;

        Marshal m = Marshal.builder()
            .addInteger(i)
            .build();

        m.getByteAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidSize__byte() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .build();

        m.getByteAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__byteArray() {
        int i = 22;

        Marshal m = Marshal.builder()
            .addInteger(i)
            .build();

        m.getByteArrayAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__double() {
        int i = 22;

        Marshal m = Marshal.builder()
            .addInteger(i)
            .build();

        m.getDoubleAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__integer() {
        double d = 3.14;

        Marshal m = Marshal.builder()
            .addDouble(d)
            .build();

        m.getIntegerAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__long() {
        int i = 22;

        Marshal m = Marshal.builder()
            .addInteger(i)
            .build();

        m.getLongAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__string() {
        int i = 22;

        Marshal m = Marshal.builder()
            .addInteger(i)
            .build();

        m.getStringAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__marshall() {
        int i = 22;

        Marshal m = Marshal.builder()
            .addInteger(i)
            .build();

        m.getMarshalAt(0);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__invalidPosition() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .build();

        assertEquals(5, m.size());

        m.getStringAt(6);
    }

    @Test(expected=NullPointerException.class)
    public void testAddString__null() {
        Marshal.builder().addString(null).build();
    }

    @Test(expected=NullPointerException.class)
    public void testAddMarshal__null() {
        Marshal.builder().addMarshal(null).build();
    }

    @Test
    public void testSerializeDeserialize__basic() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";
        byte b = (byte)0x80;

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
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(8, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(5));
        assertEquals(b, m.getByteAt(6));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(7));

        // ensure encoded format is correct
        byte[] expected = {
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

        assertArrayEquals(expected, m.toBytes());
    }

    @Test
    public void testSerializeDeserialize__doubleByte() {
        byte b1 = (byte)0x80;
        byte b2 = (byte)0x81;

        Marshal input = Marshal.builder()
            .addByte(b1)
            .addByte(b2)
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(2, m.size());

        assertEquals(b1, m.getByteAt(0));
        assertEquals(b2, m.getByteAt(1));
    }

    @Test
    public void testSerializeDeserialize__separator() {
        byte[] bytes = { Marshal.SEPARATOR, Marshal.SEPARATOR };
        ByteArray byteArray = new ByteArray(bytes);
        double d = Marshal.SEPARATOR;
        int i = Marshal.SEPARATOR;
        long l = Marshal.SEPARATOR;
        byte b = Marshal.SEPARATOR;

        Marshal input = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addByte(b)
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(5, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(b, m.getByteAt(4));
    }

    @Test
    public void testSerializeDeserialize__embedded() {
        byte[] bytes = { 0, 0, 2, 2, 4, 4 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal embedded = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .build();

        Marshal input = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .addMarshal(embedded)
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(6, m.size());

        // check the outer marshal
        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));

        // check the inner marshal
        Marshal embeddedActual = m.getMarshalAt(5);
        assertArrayEquals(byteArray.toArray(), embeddedActual.getByteArrayAt(0).toArray());
        assertEquals(d, embeddedActual.getDoubleAt(1), 0);
        assertEquals(i, embeddedActual.getIntegerAt(2));
        assertEquals(l, embeddedActual.getLongAt(3));
        assertEquals(s, embeddedActual.getStringAt(4));
    }

    @Test
    public void testSerializeDeserialize__append() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal input1 = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .build();
        Marshal input2 = Marshal.builder()
            .addLong(l)
            .addString(s)
            .build();

        Marshal input = Marshal.builder(input1)
            .appendMarshal(input2)
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(5, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));
    }

    @Test
    public void testSerializeDeserialize__appendToEmpty() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal input1 = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .build();
        Marshal input2 = Marshal.builder()
            .addLong(l)
            .addString(s)
            .build();

        Marshal input = Marshal.builder()
            .appendMarshal(input1)
            .appendMarshal(input2)
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(5, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));
    }

    @Test
    public void testSerializeDeserialize__empty() {
        Marshal input = Marshal.builder().build();

        ByteArray mBytes = input.toByteArray();
        assertNotNull(mBytes);

        Marshal m = Marshal.fromBytes(mBytes);
        assertEquals(0, m.size());
    }

    @Test
    public void testSerializeDeserialize__emptyEmbedded() {
        Marshal input = Marshal.builder()
            .addMarshal(Marshal.builder().build())
            .addMarshal(Marshal.builder().build())
            .addMarshal(Marshal.builder().build())
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(3, m.size());

        assertEquals(Marshal.EMPTY, m.getMarshalAt(0));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(1));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(2));
    }

    @Test
    public void testSerializeDeserialize__emptyString() {
        Marshal input = Marshal.builder()
            .addString("")
            .addString("")
            .addString("")
            .build();

        ByteArray mBytes = input.toByteArray();
        Marshal m = Marshal.fromBytes(mBytes);

        assertEquals(3, m.size());

        assertEquals("", m.getStringAt(0));
        assertEquals("", m.getStringAt(1));
        assertEquals("", m.getStringAt(2));
    }

    @Test
    public void testDeserialize__nullByteArray() {
        Marshal m = Marshal.fromBytes((ByteArray)null);
        assertEquals(0, m.size());
    }

    @Test
    public void testDeserialize__nullBytes() {
        Marshal m = Marshal.fromBytes((byte[])null);
        assertEquals(0, m.size());
    }

    @Test(expected=MarshalException.class)
    public void testDeserialize__invalid() {
        byte[] bytes = { (byte)0xFD, 1, 2, 3, 4 };
        ByteArray byteArray = new ByteArray(bytes);

        Marshal m = Marshal.fromBytes(byteArray);
        assertEquals(0, m.size());
    }

    @Test
    public void testGetAt__subrange() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .build();

        assertEquals(5, m.size());

        Marshal m2 = m.subrange(1, 4);

        assertEquals(3, m2.size());

        // skip the first byte array
        // allow zero tolerance in the double comparison, since the representation should be exact
        assertEquals(d, m2.getDoubleAt(0), 0);
        assertEquals(i, m2.getIntegerAt(1));
        assertEquals(l, m2.getLongAt(2));
        // skip the string
    }

    @Test
    public void testGetAt__from() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .build();

        assertEquals(5, m.size());

        Marshal m2 = m.from(2);

        assertEquals(3, m2.size());

        // skip the first byte array
        // skip double
        assertEquals(i, m2.getIntegerAt(0));
        assertEquals(l, m2.getLongAt(1));
        assertEquals(s, m2.getStringAt(2));
    }

    @Test
    public void testGetAt__to() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .build();

        assertEquals(5, m.size());

        Marshal m2 = m.to(2);

        assertEquals(2, m2.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m2.getByteArrayAt(0).toArray());
        assertEquals(d, m2.getDoubleAt(1), 0);
    }

    @Test
    public void testEmptyConstruction() {
        assertSame(Marshal.EMPTY, Marshal.builder().build());
    }

    @Test
    public void testPrefixUnterminated() {
        Marshal m1 = Marshal.builder()
            .addString("a")
            .addString("cat")
            .addString("named")
            .addString("kitty")
            .build();
        Marshal m2 = Marshal.builder()
            .addString("a")
            .addString("cat")
            .addString("named")
            .addString("kitty_kat")
            .build();

        byte[] prefix1 = m1.prefixUnterminatedBytes(4);
        byte[] prefix2 = m2.prefixUnterminatedBytes(4);

        // prefix1 is prefix of prefix2
        for(int i = 0; i < prefix1.length; i++)
            assertTrue(prefix1[i] == prefix2[i]);

        byte[] expected = {
            // string
            (byte)5,
            // "a"
            (byte)97,
            // separator
            (byte)0xFE,

            // string
            (byte)5,
            // "cat"
            (byte)99, (byte)97, (byte)116,
            // separator
            (byte)0xFE,

            // string
            (byte)5,
            // "named"
            (byte)110, (byte)97, (byte)109, (byte)101, (byte)100,
            // seperator
            (byte)0xFE,

            // string
            (byte)5,
            // kitty
            (byte)107, (byte)105, (byte)116, (byte)116, (byte)121
        };

        assertArrayEquals(expected, prefix1);
    }

    @Test
    public void testPrefixTerminated() {
        Marshal m1 = Marshal.builder()
            .addString("a")
            .addString("cat")
            .addString("named")
            .addString("kitty")
            .addString("something here")
            .build();
        Marshal m2 = Marshal.builder()
            .addString("a")
            .addString("cat")
            .addString("named")
            .addString("kitty_kat")
            .addString("something here")
            .build();

        byte[] prefix1 = m1.prefixTerminatedBytes(4);
        byte[] prefix2 = m2.prefixTerminatedBytes(4);

        // all bytes up to the last byte are the same (unterminated case)
        for(int i = 0; i < prefix1.length - 1; i++)
            assertTrue(prefix1[i] == prefix2[i]);
        // last byte is not the same (separator vs underscore)
        assertTrue(prefix1[prefix1.length - 1] != prefix2[prefix1.length - 1]);

        byte[] expected = {
            // string
            (byte)5,
            // "a"
            (byte)97,
            // separator
            (byte)0xFE,

            // string
            (byte)5,
            // "cat"
            (byte)99, (byte)97, (byte)116,
            // separator
            (byte)0xFE,

            // string
            (byte)5,
            // "named"
            (byte)110, (byte)97, (byte)109, (byte)101, (byte)100,
            // seperator
            (byte)0xFE,

            // string
            (byte)5,
            // kitty
            (byte)107, (byte)105, (byte)116, (byte)116, (byte)121,
            // seperator
            (byte)0xFE
        };

        assertArrayEquals(expected, prefix1);

        // terminated prefix of everything should be the same as the serialized form for anything that is not empty
        assertEquals(m1.toByteArray(), m1.prefixTerminated(5));
        assertEquals(m2.toByteArray(), m2.prefixTerminated(5));
    }

    @Test
    public void testPrefixUnterminated__emptyMarshal() {
        Marshal m = Marshal.builder()
            .addMarshal(Marshal.EMPTY)
            .build();

        byte[] prefix = m.prefixUnterminatedBytes(1);

        byte[] expected = {
            // empty Marshal
            6,
            (byte)0xFE, (byte)0xFE
        };

        assertArrayEquals(expected, prefix);
    }

    @Test
    public void testPrefixTerminated__emptyMarshal() {
        Marshal m = Marshal.builder()
            .addMarshal(Marshal.EMPTY)
            .build();

        byte[] prefix = m.prefixTerminatedBytes(1);

        byte[] expected = {
            // empty Marshal
            6,
            (byte)0xFE, (byte)0xFE,
            // terminating separator
            (byte)0xFE
        };

        assertArrayEquals(expected, prefix);
    }

    @Test
    public void testPrefixUnterminated__empty() {
        Marshal m = Marshal.builder()
            .build();

        byte[] prefix = m.prefixUnterminatedBytes(0);
        assertEquals(0, prefix.length);
    }

    @Test
    public void testPrefixUnterminated__nothingSelected() {
        Marshal m = Marshal.builder()
            .addString("foo")
            .build();

        byte[] prefix = m.prefixUnterminatedBytes(0);
        assertEquals(0, prefix.length);
    }

    @Test
    public void testPrefixTerminated__empty() {
        Marshal m = Marshal.builder()
            .build();

        byte[] prefix = m.prefixTerminatedBytes(0);
        assertEquals(0, prefix.length);
    }

    @Test
    public void testPrefixTerminated__nothingSelected() {
        Marshal m = Marshal.builder()
            .addString("foo")
            .build();

        byte[] prefix = m.prefixTerminatedBytes(0);
        assertEquals(0, prefix.length);
    }

    @Test
    public void testDeserialize_legacyNoTerminator() {
        byte[] input = {
            // string
            (byte)5,
            // "a"
            (byte)97,
            // separator
            (byte)0xFE,

            // string
            (byte)5,
            // "cat"
            (byte)99, (byte)97, (byte)116,
            // separator
            (byte)0xFE,

            // string
            (byte)5,
            // "named"
            (byte)110, (byte)97, (byte)109, (byte)101, (byte)100,
            // seperator
            (byte)0xFE,

            // string
            (byte)5,
            // kitty
            (byte)107, (byte)105, (byte)116, (byte)116, (byte)121
        };

        Marshal output = Marshal.fromBytes(input);

        Marshal expected = Marshal.builder()
            .addString("a")
            .addString("cat")
            .addString("named")
            .addString("kitty")
            .build();

        assertEquals(expected, output);
    }

    /**
     * Although we do not require (or specify) a stable hash code for serialization, we require a stable hash code
     * across JVM instances. This is used in MapReduce, among other uses. If the components of the hash code change,
     * then this test can change, so long as successive runs in different JVMs of the test produce the same hashcode.
     */
    @Test
    public void testHashCode__stable() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);
        double d = -3.14d;
        int i = 22;
        long l = 123456789012345678l;
        String s = " Thë quíck bröwn fox jùmps over the lazy dog! ";
        byte b = (byte)0x80;

        Marshal m = Marshal.builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .addByte(b)
            .build();

        assertEquals(-937664105, m.hashCode());
    }
}
