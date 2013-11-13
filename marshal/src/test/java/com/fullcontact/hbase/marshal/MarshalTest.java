package com.fullcontact.hbase.marshal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

        Marshal m = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .addByte(b)
            .build();

        assertEquals(6, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));
        assertEquals(b, m.getByteAt(5));
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__byte() {
        int i = 22;

        Marshal m = new Marshal.Builder()
            .addInteger(i)
            .build();

        m.getByteAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidSize__byte() {
        byte[] bytes = { 0, 1, 2, 3, 4, 5 };
        ByteArray byteArray = new ByteArray(bytes);

        Marshal m = new Marshal.Builder()
            .addByteArray(byteArray)
            .build();

        m.getByteAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__byteArray() {
        int i = 22;

        Marshal m = new Marshal.Builder()
            .addInteger(i)
            .build();

        m.getByteArrayAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__double() {
        int i = 22;

        Marshal m = new Marshal.Builder()
            .addInteger(i)
            .build();

        m.getDoubleAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__integer() {
        double d = 3.14;

        Marshal m = new Marshal.Builder()
            .addDouble(d)
            .build();

        m.getIntegerAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__long() {
        int i = 22;

        Marshal m = new Marshal.Builder()
            .addInteger(i)
            .build();

        m.getLongAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__string() {
        int i = 22;

        Marshal m = new Marshal.Builder()
            .addInteger(i)
            .build();

        m.getStringAt(0);
    }

    @Test(expected=MarshalException.class)
    public void testGetAt__invalidType__marshall() {
        int i = 22;

        Marshal m = new Marshal.Builder()
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

        Marshal m = new Marshal.Builder()
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
        new Marshal.Builder().addString(null).build();
    }

    @Test(expected=NullPointerException.class)
    public void testAddMarshal__null() {
        new Marshal.Builder().addMarshal(null).build();
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

        Marshal input = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .addByte(b)
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

        assertEquals(6, m.size());

        // allow zero tolerance in the double comparison, since the representation should be exact
        assertArrayEquals(byteArray.toArray(), m.getByteArrayAt(0).toArray());
        assertEquals(d, m.getDoubleAt(1), 0);
        assertEquals(i, m.getIntegerAt(2));
        assertEquals(l, m.getLongAt(3));
        assertEquals(s, m.getStringAt(4));
        assertEquals(b, m.getByteAt(5));
    }

    @Test
    public void testSerializeDeserialize__doubleByte() {
        byte b1 = (byte)0x80;
        byte b2 = (byte)0x81;

        Marshal input = new Marshal.Builder()
            .addByte(b1)
            .addByte(b2)
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

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

        Marshal input = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addByte(b)
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

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

        Marshal embedded = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .build();

        Marshal input = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .addLong(l)
            .addString(s)
            .addMarshal(embedded)
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

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

        Marshal input1 = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .build();
        Marshal input2 = new Marshal.Builder()
            .addLong(l)
            .addString(s)
            .build();

        Marshal input = new Marshal.Builder(input1)
            .appendMarshal(input2)
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

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

        Marshal input1 = new Marshal.Builder()
            .addByteArray(byteArray)
            .addDouble(d)
            .addInteger(i)
            .build();
        Marshal input2 = new Marshal.Builder()
            .addLong(l)
            .addString(s)
            .build();

        Marshal input = new Marshal.Builder()
            .appendMarshal(input1)
            .appendMarshal(input2)
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

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
        Marshal input = new Marshal.Builder().build();

        ByteArray mBytes = input.toBytes();
        assertNotNull(mBytes);

        Marshal m = new Marshal(mBytes);
        assertEquals(0, m.size());
    }

    @Test
    public void testSerializeDeserialize__emptyEmbedded() {
        Marshal input = new Marshal.Builder()
            .addMarshal(new Marshal.Builder().build())
            .addMarshal(new Marshal.Builder().build())
            .addMarshal(new Marshal.Builder().build())
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

        assertEquals(3, m.size());

        assertEquals(Marshal.EMPTY, m.getMarshalAt(0));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(1));
        assertEquals(Marshal.EMPTY, m.getMarshalAt(2));
    }

    @Test
    public void testSerializeDeserialize__emptyString() {
        Marshal input = new Marshal.Builder()
            .addString("")
            .addString("")
            .addString("")
            .build();

        ByteArray mBytes = input.toBytes();
        Marshal m = new Marshal(mBytes);

        assertEquals(3, m.size());

        assertEquals("", m.getStringAt(0));
        assertEquals("", m.getStringAt(1));
        assertEquals("", m.getStringAt(2));
    }

    @Test
    public void testDeserialize__null() {
        Marshal m = new Marshal(null);
        assertEquals(0, m.size());
    }

    @Test(expected=MarshalException.class)
    public void testDeserialize__invalid() {
        // separator will always be an invalid type byte
        byte[] bytes = { Marshal.SEPARATOR, 1, 2, 3, 4 };
        ByteArray byteArray = new ByteArray(bytes);

        Marshal m = new Marshal(byteArray);
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

        Marshal m = new Marshal.Builder()
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

        Marshal m = new Marshal.Builder()
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

        Marshal m = new Marshal.Builder()
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
        assertSame(Marshal.EMPTY, new Marshal.Builder().build());
    }

    /**
     * Although we do not require a stable hash code for serialization, we require a stable hash code across JVM
     * instances for reducing. If the components of the hash code change, then this test can change, so long as
     * successive runs in different JVMs of the test produce the same hashcode.
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

        Marshal m = new Marshal.Builder()
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
