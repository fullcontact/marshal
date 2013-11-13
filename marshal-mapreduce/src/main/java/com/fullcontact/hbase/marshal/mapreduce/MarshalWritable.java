package com.fullcontact.hbase.marshal.mapreduce;

import com.fullcontact.hbase.marshal.Marshal;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Writable that reads and writes {@see Marshal} instances.
 *
 * Comparison is done on a pure binary format using only the comparator. A call to compareTo will
 * cause an UnsupportedOperationException. This format is undefined, but is guaranteed to return
 * zero when two marshals are equal, is guaranteed to return non-zero when two marshals are not
 * equal, and is stable. Otherwise, comparison is undefined.
 *
 * @author Brandon Vargo
 */
public class MarshalWritable implements WritableComparable<MarshalWritable> {
    private Marshal marshal = null;

    static {
        WritableComparator.define(MarshalWritable.class, new Comparator());
    }

    /**
     * Constructor for the writable framework only!
     */
    public MarshalWritable() {
        this(null);
    }

    /**
     * Create a writeable from the given Marshal.
     */
    public MarshalWritable(Marshal marshal) {
        this.marshal = marshal;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.marshal.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.marshal = Marshal.read(dataInput);
    }

    /**
     * Returns the Marshal that this writable currently has loaded. Returns null if there is no current Marshal.
     */
    public Marshal get() {
        return marshal;
    }

    /**
     * Sets the current Marshal for this writable.
     */
    public void set(Marshal marshal) {
        this.marshal = marshal;
    }

    /**
     * Use the comparator instead of this method. This method will cause an UnsupportedOperationException.
     */
    @Override
    public int compareTo(MarshalWritable other) {
        throw new UnsupportedOperationException("Use the comparator.");
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;

        if(o instanceof MarshalWritable) {
            MarshalWritable other = (MarshalWritable)o;
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
            this(MarshalWritable.class);
        }

        public Comparator(Class<? extends MarshalWritable> cls) {
           super(cls);
        }

        @Override
        public int compare(byte[] bytes1, int startIndex1, int length1, byte[] bytes2, int startIndex2, int length2) {
            return WritableComparator.compareBytes(bytes1, startIndex1, length1,
                    bytes2, startIndex2, length2);
        }
    }
}
