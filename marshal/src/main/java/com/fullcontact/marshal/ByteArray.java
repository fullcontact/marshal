package com.fullcontact.marshal;


import com.google.common.primitives.UnsignedBytes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A ByteArray is a wrapper for an array of primitive bytes, byte[].
 *
 * @author Brandon Vargo
 */
public class ByteArray implements Comparable<ByteArray> {
    private static final Comparator<byte[]> LEXICOGRAPHICAL_COMPARATOR =
        UnsignedBytes.lexicographicalComparator();

    // backing storage
    private final byte[] bytes;

    // index into the bytes array where this ByteArray begins, inclusive
    private final int beginIndex;

    // index into the bytes array where this ByteArray ends, exclusive
    private final int endIndex;

    /**
     * Creates a ByteArray from the given byte array.
     */
    public ByteArray(byte[] bytes) {
        if(bytes == null)
            bytes = new byte[0];
        this.bytes = bytes;
        this.beginIndex = 0;
        this.endIndex = bytes.length;
    }

    /**
     * Creates a ByteArray from the given sub-range of the give byte array.
     *
     * @param bytes The backing array
     * @param beginIndex An index into the bytes array where the visible region begins, inclusive.
     * @param endIndex An index into the bytes array where the visible region ends, exclusive.
     */
    public ByteArray(byte[] bytes, int beginIndex, int endIndex) {
        // make sure the ranges make sense
        if(endIndex < beginIndex)
            throw new IllegalArgumentException("The end index must be greater than or equal " +
                   "to beginning index. Got range [" + beginIndex + "," + endIndex + ")");
        if(endIndex > bytes.length)
            throw new IllegalArgumentException("End index (" + endIndex + ") cannot be greater " +
                   "than the length (" + bytes.length + ").");

        this.bytes = bytes;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    /**
     * Returns a byte array starting from the given position. That is, calling {@link #getAt} on
     * this object with an argument of <code>position</code> is equivalent to calling {@link
     * #getAt} with an argument of 0 on the returned object.
     *
     * @throws IllegalArgumentException if position is less than 0.
     */
    public ByteArray from(int position) {
        if(position < 0)
            throw new IndexOutOfBoundsException("Position may not be less than 0. " +
                    "Got: " + position);
        else if(position >= this.size())
            throw new IndexOutOfBoundsException("Position may not be greater than size " +
                    "(" + this.size() + "). Got: " + position);
        else if(position == 0)
            return this;
        else
            return new ByteArray(this.bytes, this.beginIndex + position, this.endIndex);
    }

    /**
     * Returns a byte array that ends at the position before the given end index. That is, calling
     * {@link #getAt} on this object with an argument of <code>position</code> will return a byte
     * array of length (position - 1).
     *
     * @throws IllegalArgumentException if position is less than 0 or larger than the length
     */
    public ByteArray to(int position) {
        if(position < 0)
            throw new IndexOutOfBoundsException("Position may not be less than 0. " +
                    "Got: " + position);
        else if(position > this.size())
            throw new IndexOutOfBoundsException("Position may not be greater than size " +
                    "(" + this.size() + "). Got: " + position);
        else if(position == this.size())
            return this;
        else
            return new ByteArray(this.bytes, this.beginIndex, this.beginIndex + position);
    }

    /**
     * Appends the given ByteArray to this ByteArray, returning a new, combined ByteArray.
     */
    public ByteArray append(ByteArray other) {
        if(this.size() == 0)
            return other;
        else if(other == null || other.size() == 0)
            return this;

        byte[] a1 = this.toArray();
        byte[] a2 = other.toArray();
        byte[] n = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, n, 0, a1.length);
        System.arraycopy(a2, 0, n, a1.length, a2.length);

        return new ByteArray(n);
    }

    /**
     * Appends the given ByteArrays, in order, to this ByteArray, returning the new ByteArray.
     */
    public ByteArray append(List<ByteArray> others) {
        if(others == null || others.isEmpty())
            return this;

        int totalSize = this.size();
        for(ByteArray other : others) {
            totalSize += other.size();
        }

        // new, combined byte array
        byte[] combined = new byte[totalSize];

        // copy the current array in
        byte[] bytes = this.toArray();
        System.arraycopy(bytes, 0, combined, 0, bytes.length);

        // position is the position at which the next byte to write should be written
        int position = bytes.length;

        // copy the remaining arrays
        for(ByteArray other : others) {
            byte[] otherBytes = other.toArray();
            System.arraycopy(otherBytes, 0, combined, position, otherBytes.length);
            position += otherBytes.length;
        }

        return new ByteArray(combined);
    }

    /**
     * Combines the given ByteArrays.
     */
    public static ByteArray combine(List<ByteArray> arrays) {
        if(arrays.isEmpty()) {
            return new ByteArray(new byte[0]);
        }
        else if(arrays.size() == 1) {
            return arrays.get(0);
        }
        else {
            ByteArray first = arrays.get(0);
            List<ByteArray> remaining = arrays.subList(1, arrays.size());
            return first.append(remaining);
        }
    }

    /**
     * Combines the given ByteArrays.
     */
    public static ByteArray combine(ByteArray... arrays) {
        return combine(Arrays.asList(arrays));
    }

    /**
     * Returns the byte in the given position.
     */
    public byte getAt(int position) {
        if(position < 0 || position >= this.size())
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        return this.bytes[this.beginIndex + position];
    }

    /**
     * Returns the size of this byte array.
     */
    public int size() {
        return this.endIndex - this.beginIndex;
    }

    /**
     * Returns true if this byte array is of length 0.
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Returns a byte array representation of the current ByteArray.
     */
    public byte[] toArray() {
        if(this.beginIndex == 0 && this.endIndex == this.bytes.length) {
            return this.bytes;
        }
        else {
            byte[] bytes = new byte[this.size()];
            System.arraycopy(this.bytes, this.beginIndex, bytes, 0, this.size());
            return bytes;
        }
    }

    @Override
    public int compareTo(ByteArray other) {
        // if this isn't a subslice of an array,
        // we can use Guava's unsafe comparator for a significant speed boost.
        // TODO(xorlev): it should be possible to use this for slices too, but we'll need
        // to vendor Guava's LEXICOGRAPHICAL
        if(this.beginIndex == 0
                && this.endIndex == this.bytes.length
                && other.beginIndex == 0
                && other.endIndex == other.bytes.length) {
            return LEXICOGRAPHICAL_COMPARATOR.compare(this.bytes, other.bytes);
        }
        else {
            // current index
            int index1 = this.beginIndex;
            int index2 = other.beginIndex;

            while(index1 < this.endIndex && index2 < other.endIndex) {
                // get as ints, so we compare bytes in an unsigned manner, not individually signed
                // bytes
                int one = this.bytes[index1] & 0xFF;
                int two = other.bytes[index2] & 0xFF;
                if(one < two) {
                    return -1;
                }
                else if(one > two) {
                    return 1;
                }
                else {
                    index1++;
                    index2++;
                }
            }

            // got to the end of at least one array, and did not find the difference
            if(index1 == this.endIndex && index2 == other.endIndex)
                return 0;
            else if(index1 == this.endIndex && index2 != other.endIndex)
                return -1;
            else
                return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof ByteArray) {
            return Arrays.equals(this.toArray(), ((ByteArray)o).toArray());
        }
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.toArray());
    }

    @Override
    public String toString() {
        return Arrays.toString(this.toArray());
    }
}
