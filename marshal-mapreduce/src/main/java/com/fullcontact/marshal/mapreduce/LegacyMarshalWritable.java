package com.fullcontact.marshal.mapreduce;

import com.fullcontact.marshal.Marshal;
import com.fullcontact.marshal.MarshalCompatibilityMode;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Writable that reads {@see Marshal} instances.
 *
 * This version uses the legacy string mode for decoding marshals. Writing is not supported.
 *
 * @author Brandon Vargo
 */
public class LegacyMarshalWritable implements WritableComparable<LegacyMarshalWritable> {
    private Marshal marshal = null;

    static {
        WritableComparator.define(LegacyMarshalWritable.class, new Comparator());
    }

    /**
     * Constructor for the writable framework only!
     */
    public LegacyMarshalWritable() {
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        throw new UnsupportedOperationException("Writing not supported.");
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.marshal = Marshal.read(dataInput, MarshalCompatibilityMode.LEGACY_STRINGS);
    }

    /**
     * Returns the Marshal that this writable currently has loaded. Returns null if there is no current Marshal.
     */
    public Marshal get() {
        return marshal;
    }

    /**
     * Use the comparator instead of this method. This method will cause an UnsupportedOperationException.
     */
    @Override
    public int compareTo(LegacyMarshalWritable other) {
        throw new UnsupportedOperationException("Use the comparator.");
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;

        if(o instanceof LegacyMarshalWritable) {
            LegacyMarshalWritable other = (LegacyMarshalWritable)o;
            if(this.get().equals(other.get()))
                return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.marshal != null ? this.marshal.hashCode() : 0);
    }

    @Override
    public String toString() {
        return this.marshal.toString();
    }

    public static class Comparator extends WritableComparator {
        public Comparator() {
            this(LegacyMarshalWritable.class);
        }

        public Comparator(Class<? extends LegacyMarshalWritable> cls) {
           super(cls);
        }

        @Override
        public int compare(byte[] bytes1, int startIndex1, int length1, byte[] bytes2, int startIndex2, int length2) {
            return WritableComparator.compareBytes(bytes1, startIndex1, length1,
                    bytes2, startIndex2, length2);
        }
    }
}
