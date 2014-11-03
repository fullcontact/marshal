package com.fullcontact.marshal;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Tests for ByteArray.
 *
 * @author Brandon Vargo <brandon@fullcontact.com>
 */
@RunWith(JUnit4.class)
public class ByteArrayTest {
    //
    // normal operation
    //

    @Test
    public void testGetAt() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(a.getAt(0), 0);
        assertEquals(a.getAt(1), 1);
        assertEquals(a.getAt(2), 2);
        assertEquals(a.getAt(3), 3);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__tooHigh() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        a.getAt(4);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__tooLow() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        a.getAt(-1);
    }

    @Test
    public void testSize() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertEquals(a.size(), 4);
    }

    @Test
    public void testToArray() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertArrayEquals(bytes, a.toArray());
    }

    @Test
    public void from__zero() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(0);
        assertArrayEquals(bytes, a.toArray());
    }

    @Test
    public void testEquals() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        ByteArray b = new ByteArray(bytes);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());
        assertFalse(a == b);
    }

    @Test
    public void testEquals__different() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        ByteArray b = a.from(1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a == b);
    }

    @Test
    public void testEquals__differentTypes() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray a = new ByteArray(bytes);
        assertFalse(a.equals(null));
    }

    @Test
    public void testAppend() {
        byte[] b1 = { 0, 1, 2, 3 };
        byte[] b2 = { 0, 1, 2, 4 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2);

        ByteArray combined = a1.append(a2);
        byte[] expectedBytes = { 0, 1, 2, 3, 0, 1, 2, 4 };
        ByteArray expected = new ByteArray(expectedBytes);

        assertEquals(expected, combined);
    }

    @Test
    public void testAppend__multi() {
        byte[] b1 = { 0, 1, 2, 3 };
        byte[] b2 = { 0, 1, 2, 4 };
        byte[] b3 = { 0, 1, 2, 5 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2);
        ByteArray a3 = new ByteArray(b3);

        ByteArray combined = a1.append(Arrays.asList(a2, a3));
        byte[] expectedBytes = { 0, 1, 2, 3, 0, 1, 2, 4, 0, 1, 2, 5 };
        ByteArray expected = new ByteArray(expectedBytes);

        assertEquals(expected, combined);
    }

    @Test
    public void testCombine() {
        byte[] b1 = { 0, 1, 2, 3 };
        byte[] b2 = { 0, 1, 2, 4 };
        byte[] b3 = { 0, 1, 2, 5 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2);
        ByteArray a3 = new ByteArray(b3);

        ByteArray combined = ByteArray.combine(a1, a2, a3);
        byte[] expectedBytes = { 0, 1, 2, 3, 0, 1, 2, 4, 0, 1, 2, 5 };
        ByteArray expected = new ByteArray(expectedBytes);

        assertEquals(expected, combined);
    }

    @Test
    public void testCompareTo__equals() {
        byte[] b1 = { 0, 1, 2, 3 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2);
        assertEquals(0, a1.compareTo(a2));
        assertEquals(0, a2.compareTo(a1));
    }

    @Test
    public void testCompareTo__compare() {
        byte[] b1 = { 0, 1, 1, 3 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compareLexographical() {
        byte[] b1 = { 0, 1, 2 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    //
    // view that starts at a positive startIndex
    //

    @Test
    public void testGetAt__offset() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1);
        assertEquals(a.getAt(0), 1);
        assertEquals(a.getAt(1), 2);
        assertEquals(a.getAt(2), 3);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__offset__tooHigh() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1);
        a.getAt(3);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__offset__tooLow() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1);
        a.getAt(-1);
    }

    @Test
    public void testSize__offset() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1);
        assertEquals(a.size(), 3);
    }

    @Test
    public void toArray__offset() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1);
        byte[] expected = { 1, 2, 3 };
        assertArrayEquals(expected, a.toArray());
    }

    @Test
    public void testCompareTo__equals__offset() {
        byte[] b1 = { 22, 0, 1, 2, 3 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).from(1);
        ByteArray a2 = new ByteArray(b2);
        assertEquals(0, a1.compareTo(a2));
        assertEquals(0, a2.compareTo(a1));
    }

    @Test
    public void testCompareTo__compare__offsetLeft() {
        byte[] b1 = { 22, 0, 1, 1, 3 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).from(1);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compare__offsetRight() {
        byte[] b1 = { 0, 1, 1, 3 };
        byte[] b2 = { 22, 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2).from(1);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compareLexographical__offsetLeft() {
        byte[] b1 = { 22, 0, 1, 2 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).from(1);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compareLexographical__offsetRight() {
        byte[] b1 = { 0, 1, 2 };
        byte[] b2 = { 22, 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2).from(1);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    //
    // view that ends at a endIndex smaller than the size
    //

    @Test
    public void testGetAt__short() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.to(3);
        assertEquals(a.getAt(0), 0);
        assertEquals(a.getAt(1), 1);
        assertEquals(a.getAt(2), 2);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__short__tooHigh() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.to(3);
        a.getAt(3);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__short__tooLow() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.to(3);
        a.getAt(-1);
    }

    @Test
    public void testSize__short() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.to(3);
        assertEquals(a.size(), 3);
    }

    @Test
    public void toArray__short() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.to(3);
        byte[] expected = { 0, 1, 2 };
        assertArrayEquals(expected, a.toArray());
    }

    @Test
    public void testCompareTo__equals__short() {
        byte[] b1 = { 0, 1, 2, 3, 22 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).to(4);
        ByteArray a2 = new ByteArray(b2);
        assertEquals(0, a1.compareTo(a2));
        assertEquals(0, a2.compareTo(a1));
    }

    @Test
    public void testCompareTo__compare__shortLeft() {
        byte[] b1 = { 0, 1, 1, 3, 22 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).to(4);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compare__shortRight() {
        byte[] b1 = { 0, 1, 1, 3 };
        byte[] b2 = { 0, 1, 2, 3, 22 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2).to(4);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compareLexographical__shortLeft() {
        byte[] b1 = { 0, 1, 2, 22 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).to(3);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compareLexographical__shortRight() {
        byte[] b1 = { 0, 1, 2 };
        byte[] b2 = { 0, 1, 2, 3, 22 };
        ByteArray a1 = new ByteArray(b1);
        ByteArray a2 = new ByteArray(b2).to(4);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    //
    // view that starts at a positive startIndex and ends at at an endIndex smaller than the size
    //

    // note that the to index is relative to the view returned by from
    // e.g. [0, 1, 2, 3].from(1).to(2) == [1, 2, 3].to(2) == [1, 2]

    @Test
    public void testGetAt__subsection() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1).to(2);
        assertEquals(a.getAt(0), 1);
        assertEquals(a.getAt(1), 2);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__subsection__tooHigh() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1).to(2);
        a.getAt(2);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGetAt__subsection__tooLow() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1).to(2);
        a.getAt(-1);
    }

    @Test
    public void testSize__subsection() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1).to(2);
        assertEquals(a.size(), 2);
    }

    @Test
    public void toArray__subsection() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        ByteArray a = original.from(1).to(2);
        byte[] expected = { 1, 2 };
        assertArrayEquals(expected, a.toArray());
    }

    @Test
    public void testCompareTo__equals__subsection() {
        byte[] b1 = { 22, 0, 1, 2, 3, 22 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).from(1).to(4);
        ByteArray a2 = new ByteArray(b2);
        assertEquals(0, a1.compareTo(a2));
        assertEquals(0, a2.compareTo(a1));
    }

    @Test
    public void testCompareTo__compare__subsection() {
        byte[] b1 = { 22, 0, 1, 1, 3, 22 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).from(1).to(4);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    @Test
    public void testCompareTo__compareLexographical__subsection() {
        byte[] b1 = { 22, 0, 1, 2, 22 };
        byte[] b2 = { 0, 1, 2, 3 };
        ByteArray a1 = new ByteArray(b1).from(1).to(3);
        ByteArray a2 = new ByteArray(b2);
        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
    }

    //
    // failure conditions
    //

    @Test(expected=IndexOutOfBoundsException.class)
    public void testFrom__negative() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        original.from(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testFrom__large() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        original.from(4);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testTo__negative() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        original.to(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testTo__large() {
        byte[] bytes = { 0, 1, 2, 3 };
        ByteArray original = new ByteArray(bytes);
        original.to(5);
    }

    @Test
    public void testNullArray() {
        byte[] bytes = null;
        ByteArray a = new ByteArray(bytes);
        assertEquals(0, a.size());
    }
}
