package com.fullcontact.hbase.marshal;

import com.google.common.annotations.VisibleForTesting;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Marshal is a system for performing custom serialization to/from byte arrays.
 *
 * The serialization is done in a byte-comparable manner; this allows the resulting arrays to be
 * lexographically compared. This is useful in systems such as HBase, which only deals with byte
 * arrays. A Marshal is similar to a list of primitive types or a limited set of objects (of
 * potentially different types) backed by objects serialized in a {@link ByteArray}. Marshals may
 * include other marshals or byte arrays as a nested component. Type information is stored for
 * each object, allowing for typed reconstruction of known types, pretty printing, and stable sort
 * orders without mixing of types in the sorting.
 *
 * The serialized form of a marshal looks like the following:
 *  [type code][entry data][separator byte][type code][entry data] ... [type code][entry data]
 *
 * @author Brandon Vargo
 */
public class Marshal implements Comparable<Marshal> {
    @VisibleForTesting
    static final byte SEPARATOR = (byte)0xFE;
    private static final byte[] SEPARATOR_ARRAY = { SEPARATOR };
    private static final ByteArray SEPARATOR_BYTE_ARRAY = new ByteArray(SEPARATOR_ARRAY);

    // an empty Marshal
    private static final List<Entry> EMPTY_CONTENTS = Collections.emptyList();
    public static final Marshal EMPTY = new Marshal(EMPTY_CONTENTS);

    /**
     * Known types allowed to be added to a marshal.
     *
     * Note that these values *cannot* be changed without breaking binary compatibility.
     */
    private static enum EntryType {
        BYTE              ((byte)0x00, ByteType.INSTANCE),
        BYTE_ARRAY        ((byte)0x01, ByteArrayType.INSTANCE),
        DOUBLE            ((byte)0x02, DoubleType.INSTANCE),
        INTEGER           ((byte)0x03, IntegerType.INSTANCE),
        LONG              ((byte)0x04, LongType.INSTANCE),
        STRING            ((byte)0x05, StringType.INSTANCE),
        MARSHAL           ((byte)0x06, MarshalType.INSTANCE),
        // SEPARATOR (0xFE) is reserved and cannot be used
        EMPTY             ((byte)0xFF, null);                 // indicates a completely empty marshal

        private final byte typeCode;
        private final AbstractType<?> type;

        private EntryType(byte typeCode, AbstractType<?> type) {
            this.typeCode = typeCode;
            this.type = type;
        }

        /**
         * A single byte that indicates the type code for this entry type.
         */
        public byte getTypeCode() {
            return typeCode;
        }

        public AbstractType<?> getType() {
            return this.type;
        }
    }

    /**
     * Map of type code to entry type.
     */
    private static final Map<Byte, EntryType> entryTypeCodes;
    static {
        Map<Byte, EntryType> m = new HashMap<Byte, EntryType>();
        for(EntryType type : EntryType.values()) {
            m.put(type.getTypeCode(), type);
        }
        entryTypeCodes = Collections.unmodifiableMap(m);
    }

    /**
     * An entry in the marshal.
     */
    private static class Entry<T> {
        /**
         * The type of the data in this entry.
         */
        private final EntryType entryType;

        /**
         * The unescaped data for this entry.
         */
        private ByteArray data = null;

        /**
         * The field object stored by this entry.
         */
        private T fieldObject = null;

        /**
         * Creates an entry of the given type for the given field object.
         */
        public Entry(EntryType entryType, T fieldObject) {
            if(entryType == null)
                throw new MarshalException("Entry type not provided or was invalid.");
            if(fieldObject == null)
                throw new NullPointerException("Field object not provided; must be non-null.");
            this.entryType = entryType;
            this.fieldObject = fieldObject;
        }

        /**
         * Private for use by {@link #fromBytes} and {@link #fromData}.
         */
        private Entry(EntryType entryType) {
            if(entryType == null)
                throw new MarshalException("Entry type not provided or was invalid.");
            this.entryType = entryType;
        }

        /**
         * Creates an entry from the given entry type and byte array.
         */
        public static Entry fromBytes(EntryType entryType, ByteArray data) {
            if(data == null)
                throw new NullPointerException("Data not provided; must be non-null.");
            if(data.size() == 0)
                throw new MarshalException("Data type must provide non-empty data for the serialization.");

            Entry entry = new Entry(entryType);
            entry.data = data;

            return entry;
        }

        /**
         * Creates an entry by reading the object from data input for the specified entry type.
         */
        // unchecked for field object conversion
        @SuppressWarnings("unchecked")
        public static Entry fromData(EntryType entryType, DataInput dataInput) throws IOException {
            Entry entry = new Entry(entryType);
            entry.fieldObject = entry.getDataType().read(dataInput);
            return entry;
        }

        /**
         * Write the current entry (without a type byte) to the data output.
         */
        public void write(DataOutput dataOutput) throws IOException {
            this.getDataType().write(this.getFieldObject(), dataOutput);
        }

        /**
         * Gets the entry type for the data of this entry.
         */
        public EntryType getEntryType() {
            return this.entryType;
        }

        /**
         * Get the data type of this entry. Note that this field can also be retrieved using the entry type, but we have
         * this additional method for type safety.
         */
        // unchecked conversion when initializing data type; we must manually ensure that the types always map
        @SuppressWarnings("unchecked")
        private AbstractType<T> getDataType() {
            return (AbstractType<T>)this.getEntryType().getType();
        }

        /**
         * Gets the data in this entry. The data contained in the byte array has the type equal to the type of this
         * entry.
         */
        public ByteArray getData() {
            if(this.data == null)
                this.data = this.getDataType().marshal(this.fieldObject);
            return this.data;
        }

        /**
         * Gets the field object stored in this entry. This is equivalent to demarshaling the data contained in this
         * entry.
         */
        public T getFieldObject() {
            if(this.fieldObject == null)
                this.fieldObject = this.getDataType().demarshal(this.data);
            return this.fieldObject;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || !(o instanceof Entry))
                return false;

            Entry entry = (Entry)o;

            if(this.entryType != entry.getEntryType())
                return false;
            if(!this.getFieldObject().equals(entry.getFieldObject()))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            // enum's hashCode is not stable across JVMs, so use the type code
            int result = this.entryType.getTypeCode();
            result = 31 * result + this.getFieldObject().hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "{" + this.getEntryType().name() + "-" + this.getFieldObject() + "}";
        }
    }

    /**
     * Builder for a marshal.
     */
    public static class Builder {
        private List<Entry> contents;

        public Builder() {
            this.contents = new ArrayList<Entry>();
        }

        public Builder(Marshal marshal) {
            this.contents = marshal.contents;
        }

        public Builder addByte(byte b) {
            EntryType type = EntryType.BYTE;
            this.contents.add(new Entry<Byte>(type, b));
            return this;
        }

        public Builder addByteArray(ByteArray byteArray) {
            EntryType type = EntryType.BYTE_ARRAY;
            this.contents.add(new Entry<ByteArray>(type, byteArray));
            return this;
        }

        public Builder addDouble(double d) {
            EntryType type = EntryType.DOUBLE;
            this.contents.add(new Entry<Double>(type, d));
            return this;
        }

        public Builder addInteger(int i) {
            EntryType type = EntryType.INTEGER;
            this.contents.add(new Entry<Integer>(type, i));
            return this;
        }

        public Builder addLong(long l) {
            EntryType type = EntryType.LONG;
            this.contents.add(new Entry<Long>(type, l));
            return this;
        }

        public Builder addString(String s) {
            if(s == null)
                throw new NullPointerException("Null string cannot be added to a marshal.");
            EntryType type = EntryType.STRING;
            this.contents.add(new Entry<String>(type, s));
            return this;
        }

        public Builder addMarshal(Marshal m) {
            if(m == null)
                throw new NullPointerException("Null marshal cannot be added to a marshal.");
            EntryType type = EntryType.MARSHAL;
            this.contents.add(new Entry<Marshal>(type, m));
            return this;
        }

        /**
         * Appends the given marshal to the marshal under construction. This equivalent to calling add for every entry
         * in the provided marshal. This differs from {@link #addMarshal} in that addMarshal will add a Marshal as a
         * sub-element, while this method exgtends the current data structure with the contents of another Marshal.
         */
        public Builder appendMarshal(Marshal m) {
            this.contents.addAll(m.contents);
            return this;
        }

        public Marshal build() {
            if(this.contents.isEmpty())
                return Marshal.EMPTY;
            else
                return new Marshal(this);
        }
    }

    /**
     * Contents of the marshal.
     */
    private List<Entry> contents;

    /**
     * Create a new marshal from a buidler.
     */
    private Marshal(Builder builder) {
        this.contents = builder.contents;
    }

    /**
     * Create a new marshal from a list of entries.
     */
    private Marshal(List<Entry> contents) {
        this.contents = contents;
    }

    /**
     * Create a new marshal object from the serialized byte form in native compatibility mode.
     */
    public Marshal(ByteArray data) {
        this(data, null);
    }

    /**
     * Create a new marshal object from the serialized byte form with the given compatibility mode.
     *
     * If the compatibility mode is not specified, then no compatibility mode (native mode) is used.
     */
    public Marshal(ByteArray data, MarshalCompatibilityMode compatibilityMode) {
        this.contents = new ArrayList<Entry>();

        // if no data, then do not read anything and leave the marshal empty
        if(data == null || data.size() == 0)
            return;

        // check for an empty byte array, encoded using a single empty type byte
        if(data.getAt(0) == EntryType.EMPTY.getTypeCode())
            return;

        // split data into entries
        while(true) {
            // read the type code
            byte typeCode = data.getAt(0);

            // adjust type code if in compatibility mode
            if(compatibilityMode != null) {
                typeCode = compatibilityMode.convertType(typeCode);
            }

            // get the type
            EntryType type = entryTypeCodes.get(typeCode);
            if(type == null)
                throw new MarshalException("Type code " + typeCode + " is invalid.");

            // advance past the type code
            data = data.from(1);

            // find the position of the first separator character that is not escaped
            int separatorPosition = findSeparator(data, SEPARATOR);

            // get data, unescape, and save
            ByteArray escapedValueBytes = data.to(separatorPosition);
            ByteArray valueBytes = unescape(escapedValueBytes, SEPARATOR);
            this.contents.add(Entry.fromBytes(type, valueBytes));

            // if next position is the same as size, break
            if(separatorPosition == data.size())
                break;

            // advance past the separator
            data = data.from(separatorPosition + 1);
        }
    }

    /**
     * Returns the Marshal as a single, serialized ByteArray.
     */
    public ByteArray toBytes() {
        List<ByteArray> results = new ArrayList<ByteArray>();
        for(Entry e : this.contents) {
            byte[] typeBytes = { e.getEntryType().getTypeCode() };
            ByteArray type = new ByteArray(typeBytes);
            ByteArray escapedData = escape(e.getData(), SEPARATOR);

            if(!results.isEmpty())
                results.add(SEPARATOR_BYTE_ARRAY);
            results.add(type);
            results.add(escapedData);
        }

        if(results.isEmpty()) {
            // if there is nothing in the marshal, then encode the marshal as a single empty type byte
            byte[] emptyBytes = { EntryType.EMPTY.getTypeCode() };
            return new ByteArray(emptyBytes);
        }
        else {
            return ByteArray.combine(results);
        }
    }

    /**
     * Write the current marshal to the data output.
     */
    public void write(DataOutput dataOutput) throws IOException {
        // length
        dataOutput.writeInt(this.contents.size());

        for(Entry e : this.contents) {
            // type byte
            dataOutput.write(e.getEntryType().getTypeCode());

            // data
            e.write(dataOutput);
        }
    }

    /**
     * Read the current marshal to the data input.
     */
    public static Marshal read(DataInput dataInput) throws IOException {
        // number of elements to read
        int length = dataInput.readInt();

        // contents array
        List<Entry> contents = new ArrayList<Entry>(length);

        // read data
        for(int i = 0; i < length; i++) {
            // type byte
            byte typeCode = dataInput.readByte();

            // type
            EntryType type = entryTypeCodes.get(typeCode);
            if(type == null)
                throw new MarshalException("Type code " + typeCode + " is invalid.");

            // data
            Entry e = Entry.fromData(type, dataInput);
            contents.add(e);
        }

        return new Marshal(contents);
    }

    /**
     * Returns the type of the field at the given index.
     */
    public AbstractType<?> getTypeAt(int index) {
        return this.contents.get(index).getEntryType().getType();
    }

    /**
     * Returns the object at the given index, regardless of type.
     */
    public Object getAt(int index) {
        Entry entry = this.contents.get(index);
        return entry.getFieldObject();
    }

    public byte getByteAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof Byte) {
            return (Byte)o;
        }
        else {
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not byte array.");
        }
    }

    public ByteArray getByteArrayAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof ByteArray) {
            return (ByteArray)o;
        }
        else {
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not ByteArray.");
        }
    }

    public double getDoubleAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof Double)
            return (Double)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Double.");
    }

    public int getIntegerAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof Integer)
            return (Integer)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Integer.");
    }

    public long getLongAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof Long)
            return (Long)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Long.");
    }

    public String getStringAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof String)
            return (String)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not String.");
    }

    public Marshal getMarshalAt(int index) {
        Object o = this.getAt(index);
        if(o instanceof Marshal)
            return (Marshal)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Marshal.");
    }

    /**
     * A subrange of the current Marshal.
     *
     * @param fromIndex The low endpoint (inclusive) of the sublist to return.
     * @param toIndex The high endpoint (exclusive) of the sublist to return.
     */
    public Marshal subrange(int fromIndex, int toIndex) {
        return new Marshal(this.contents.subList(fromIndex, toIndex));
    }

    /**
     * A subrange of the current Marshal starting at the given index and continuing through the end of the Marshal.
     */
    public Marshal from(int fromIndex) {
        return subrange(fromIndex, this.size());
    }

    /**
     * A subrange of the current Marshal starting at the start of the Marshal continuing up to, but not including, the
     * given end index.
     */
    public Marshal to(int toIndex) {
        return subrange(0, toIndex);
    }

    /**
     * Returns the number of components in the size.
     */
    public int size() {
        return this.contents.size();
    }

    @Override
    public int compareTo(Marshal other) {
        ByteArray bytes1 = this.toBytes();
        ByteArray bytes2 = other.toBytes();

        return bytes1.compareTo(bytes2);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(!(o instanceof Marshal))
            return false;

        Marshal other = (Marshal)o;
        if(!this.contents.equals(other.contents))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return this.contents.hashCode();
    }

    @Override
    public String toString() {
        return this.contents.toString();
    }

    /**
     * Escapes all escapeByte bytes by prepending the escapeByte byte.
     *
     * For example, if escapeByte were 2 and the array was { 0, 1, 2, 3 }, then the output array would be
     * { 0, 1, 2, 2, 3 }.
     */
    @VisibleForTesting
    static ByteArray escape(ByteArray input, byte escapeByte) {
        // count number of bytes that need to be escaped
        int bytesToEscape = 0;
        for(int i = 0; i < input.size(); i++) {
            if(input.getAt(i) == escapeByte) {
                bytesToEscape++;
            }
        }

        // allocate the new byte array
        byte[] escaped = new byte[input.size() + bytesToEscape];

        // escape the bytes
        for(int sourceIndex = 0, targetIndex = 0; sourceIndex < input.size(); sourceIndex++, targetIndex++) {
            byte b = input.getAt(sourceIndex);
            if(b == escapeByte) {
                // write the escape byte
                escaped[targetIndex] = escapeByte;
                targetIndex++;
            }

            // copy the byte from the source to the target
            escaped[targetIndex] = input.getAt(sourceIndex);
        }

        return new ByteArray(escaped);
    }

    /**
     * Unescapes all escaped unescapeByte bytes by removing the extra unescapeByte byte.
     *
     * For example, if unescapeByte were 2 and the array was { 0, 1, 2, 2, 3 }, then the output array would be
     * { 0, 1, 2, 3 }.
     *
     * Any invalid escapings are ignored.
     */
    @VisibleForTesting
    static ByteArray unescape(ByteArray input, byte unescapeByte) {
        // count number of bytes that need to be unescaped
        int bytesToUnescape = 0;
        boolean lastWasEscaped = false;
        for(int i = 0; i < input.size(); i++) {
            if(input.getAt(i) == unescapeByte) {
                if(lastWasEscaped) {
                    bytesToUnescape++;
                    lastWasEscaped = false;
                }
                else {
                    lastWasEscaped = true;
                }
            }
            else {
                // clear the escape byte if this is not an escape character, since there was an invalid escaping
                lastWasEscaped = false;
            }
        }

        // allocate the new byte array
        byte[] unescaped = new byte[input.size() - bytesToUnescape];

        // unescape the bytes
        lastWasEscaped = false;
        for(int sourceIndex = 0, targetIndex = 0; sourceIndex < input.size(); sourceIndex++, targetIndex++) {
            byte b = input.getAt(sourceIndex);
            if(b == unescapeByte) {
                if(lastWasEscaped) {
                    lastWasEscaped = false;

                    // fall through write the single byte
                }
                else {
                    // escape byte; skip
                    // this removes one from the index, and then the loop adds it back again, so the position does not
                    // change
                    targetIndex--;
                    lastWasEscaped = true;
                    continue;
                }
            }
            else if(lastWasEscaped) {
                // not the escape byte, but the last byte was an escape byte, meaning this was an inproper escape
                lastWasEscaped = false;

                // in addition, need to write the last byte, since it was skipped thinking that the next character would be
                // an escape character
                unescaped[targetIndex] = unescapeByte;
                targetIndex++;
            }

            // copy the byte from the source to the target
            unescaped[targetIndex] = input.getAt(sourceIndex);
        }

        if(lastWasEscaped) {
            // last character was an invalid escape character; write it out
            unescaped[unescaped.length - 1] = unescapeByte;
        }

        return new ByteArray(unescaped);
    }

    /**
     * Finds the position of the first non-escaped byte that matches the escape character.
     *
     * If there is no non-escaped escape byte, then return value is one greater than the length of the input, as if
     * there were a separator one position past the end of the array.
     */
    @VisibleForTesting
    static int findSeparator(ByteArray input, byte escapeByte) {
        int i = 0;
        int size = input.size();
        while(i < size) {
            if(input.getAt(i) == escapeByte) {
                if(i == size - 1) {
                    // separator is the last byte
                    return i;
                }
                else {
                    // there is a following byte
                    byte following = input.getAt(i + 1);
                    if(following == escapeByte) {
                        // the following byte is an escape byte, so this is not a separator
                        // advance past the two escape bytes
                        i += 2;
                    }
                    else {
                        // the following byte is not an ecscape byte; this is a separator!
                        return i;
                    }
                }
            }
            else {
                // current byte is not an escape byte
                i++;
            }
        }

        // did not find the proper byte, but we consumed the entire byte array
        // return an index one past the end of the array
        return size;
    }
}
