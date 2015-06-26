package com.fullcontact.marshal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Marshal is a system for performing custom serialization to/from byte arrays.
 *
 * The serialization is done in a byte-comparable manner; this allows the resulting arrays to be
 * lexicographically compared. This is useful in systems such as HBase, which only deals with byte
 * arrays. A Marshal is similar to a list of primitive types or a limited set of objects (of
 * potentially different types) backed by objects serialized in a {@link ByteArray}. Marshals may
 * include other marshals or byte arrays as a nested component. Type information is stored for each
 * object, allowing for typed reconstruction of known types, pretty printing, and stable sort orders
 * without mixing of types in the sorting.
 *
 * The serialized form of a marshal looks like the following:
 *  [type code][entry data][separator][type code][entry data] ... [type code][entry data][separator]
 * A previous version of this library did not include the final separator byte by design and used
 * a different encoding sequence to encode the empty Marshal. This version of the library can
 * deserialize that encoding format, but will serialize using the new format.
 *
 * @author Brandon Vargo
 */
public final class Marshal implements Comparable<Marshal> {
    @VisibleForTesting
    static final byte SEPARATOR = (byte)0xFE;
    private static final byte[] SEPARATOR_ARRAY = { SEPARATOR };
    private static final ByteArray SEPARATOR_BYTE_ARRAY = new ByteArray(SEPARATOR_ARRAY);

    // an empty Marshal
    public static final Marshal EMPTY = new Marshal(ImmutableList.<Entry>of());

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
            this.entryType = entryType;
            this.fieldObject = fieldObject;
        }

        /**
         * Private for use by {@link #fromBytes} and {@link #fromData}.
         */
        private Entry(EntryType entryType) {
            this.entryType = entryType;
        }

        /**
         * Creates an entry from the given entry type and byte array.
         */
        public static Entry fromBytes(EntryType entryType, ByteArray data) throws MarshalException {
            if(data.size() == 0)
                throw new MarshalException("Data type must provide non-empty data for the "  +
                        "serialization.");

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
         * Get the data type of this entry. Note that this field can also be retrieved using the
         * entry type, but we have this additional method for type safety.
         */
        // unchecked conversion when initializing data type
        // we must manually ensure that the types always map
        @SuppressWarnings("unchecked")
        private AbstractType<T> getDataType() {
            return (AbstractType<T>)this.getEntryType().getType();
        }

        /**
         * Gets the data in this entry. The data contained in the byte array has the type equal to
         * the type of this entry.
         */
        public ByteArray getData() {
            if(this.data == null)
                this.data = this.getDataType().marshal(this.fieldObject);
            return this.data;
        }

        /**
         * Gets the field object stored in this entry. This is equivalent to demarshaling the data
         * contained in this entry.
         */
        public T getFieldObject() throws MarshalException {
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
            try {
                if(!this.getFieldObject().equals(entry.getFieldObject()))
                    return false;
            }
            catch(MarshalException e) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            // enum's hashCode is not stable across JVMs, so use the type code
            try {
                int result = this.entryType.getTypeCode();
                result = 31 * result + this.getFieldObject().hashCode();
                return result;
            }
            catch(MarshalException e) {
                return 0;
            }
        }

        @Override
        public String toString() {
            try {
                return "{" + this.getEntryType().name() + "-" + this.getFieldObject() + "}";
            }
            catch(MarshalException e) {
                return "{ INVALID }";
            }
        }
    }

    /**
     * Builder for a marshal.
     */
    public static final class Builder {
        private ImmutableList.Builder<Entry> contents;

        private Builder() {
            this.contents = ImmutableList.builder();
        }

        private Builder(Marshal marshal) {
            this.contents = ImmutableList.builder();
            this.contents.addAll(marshal.contents);
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
         * Appends the given marshal to the marshal under construction. This equivalent to calling
         * add for every entry in the provided marshal. This differs from {@link #addMarshal} in
         * that addMarshal will add a Marshal as a sub-element, while this method extends the
         * current data structure with the contents of another Marshal.
         */
        public Builder appendMarshal(Marshal m) {
            this.contents.addAll(m.contents);
            return this;
        }

        public Marshal build() {
            ImmutableList<Entry> contents = this.contents.build();
            if(contents.isEmpty())
                return Marshal.EMPTY;
            else
                return new Marshal(contents);
        }
    }

    /**
     * Contents of the marshal.
     */
    private ImmutableList<Entry> contents;

    private Marshal(ImmutableList<Entry> contents) {
        this.contents = contents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Marshal m) {
        return new Builder(m);
    }

    /**
     * Reads a marshal from the serialized lexicographic marshal in the byte array.
     */
    public static Marshal fromBytes(ByteArray bytes) throws MarshalException {
        ImmutableList.Builder<Entry> contents = ImmutableList.builder();

        // if no data, then do not read anything and leave the marshal empty
        if(bytes == null || bytes.size() == 0)
            return Marshal.EMPTY;

        // check for an empty byte array, encoded as either a single separator byte or using the
        // legacy empty byte
        if(bytes.getAt(0) == SEPARATOR || bytes.getAt(0) == EntryType.LEGACY_EMPTY.getTypeCode())
            return Marshal.EMPTY;

        // split data into entries
        while(true) {
            // read the type code
            byte typeCode = bytes.getAt(0);

            // get the type
            Optional<EntryType> type = EntryType.forCode(typeCode);
            if(!type.isPresent())
                throw new MarshalException("Type code " + typeCode + " is invalid.");

            // advance past the type code
            bytes = bytes.from(1);

            // find the position of the first separator character that is not escaped
            int separatorPosition = findSeparator(bytes, SEPARATOR);

            // get data, unescape, and save
            ByteArray escapedValueBytes = bytes.to(separatorPosition);
            ByteArray valueBytes = unescape(escapedValueBytes, SEPARATOR);
            contents.add(Entry.fromBytes(type.get(), valueBytes));

            // if next position is the same as size (legacy version, with no terminating
            // separator) or size-1 (new version, with a terminating separator), done processing
            if(separatorPosition == bytes.size() || separatorPosition == (bytes.size() - 1))
                break;

            // advance past the separator
            bytes = bytes.from(separatorPosition + 1);
        }

        ImmutableList<Entry> c = contents.build();
        if(c.isEmpty())
            return Marshal.EMPTY;
        else
            return new Marshal(c);
    }

    /**
     * Reads a marshal from the serialized lexicographic marshal in the byte array.
     */
    public static Marshal fromBytes(byte[] bytes) throws MarshalException {
        return fromBytes(new ByteArray(bytes));
    }

    /**
     * Reads a marshal from the serialized writable data.
     */
    public static Marshal read(DataInput dataInput) throws IOException {
        // number of elements to read
        int length = dataInput.readInt();

        // contents array
        ImmutableList.Builder<Entry> contents = ImmutableList.builder();

        // read data
        for(int i = 0; i < length; i++) {
            // type byte
            byte typeCode = dataInput.readByte();

            // type
            Optional<EntryType> type = EntryType.forCode(typeCode);
            if(!type.isPresent())
                throw new MarshalException("Type code " + typeCode + " is invalid.");

            // data
            Entry entry = Entry.fromData(type.get(), dataInput);
            contents.add(entry);
        }

        ImmutableList<Entry> c = contents.build();
        if(c.isEmpty())
            return Marshal.EMPTY;
        else
            return new Marshal(c);
    }

    /**
     * Returns the Marshal as a single, serialized ByteArray.
     *
     * @return A serialized, full Marshal.
     */
    public ByteArray toByteArray() {
        ByteArray byteArray = this.prefixTerminated(this.contents.size());
        if(byteArray.isEmpty())
            return SEPARATOR_BYTE_ARRAY;
        else
            return byteArray;
    }

    /**
     * Returns the Marshal as a single, serialized byte array.
     */
    public byte[] toBytes() {
        return this.toByteArray().toArray();
    }

    /**
     * Returns the unterminated prefix of the Marshal containing only the first n entries.
     *
     * A separator is not included on the end of the prefix. That is, given two Marshals:
     * a) a:cat:named:kitty:!:
     * b) a:cat:named:kitty_kat:!:
     *
     * Then a.prefix(4) is a:cat:named:kitty, which is a prefix of (b).
     *
     * @param n The number of items to include in the prefix. That is, entries [0,n) will be
     * included in the result.
     * @return The prefix. If the Marshal is empty, then the empty byte array will be returned.
     */
    public ByteArray prefixUnterminated(int n) {
        return ByteArray.combine(this.prefix(n));
    }

    /**
     * @see #prefixUnterminated
     * @return Byte array version of {@link #prefixUnterminated}.
     */
    public byte[] prefixUnterminatedBytes(int n) {
        return this.prefixUnterminated(n).toArray();
    }

    /**
     * Returns the terminated prefix of the Marshal containing only the first n entries.
     *
     * A separator is included on the end of the prefix. That is, given two Marshals:
     * a) a:cat:named:kitty:!:
     * b) a:cat:named:kitty_kat:!:
     *
     * Then a.prefix(4) is a:cat:named:kitty:, which is not a prefix of (b).
     *
     * @param n The number of items to include in the prefix. That is, entries [0,n) will be
     * included in the result.
     * @return The prefix. If the Marshal is empty, then the empty byte array will be returned.
     */
    public ByteArray prefixTerminated(int n) {
        List<ByteArray> prefix = this.prefix(n);
        if(!prefix.isEmpty())
            prefix.add(SEPARATOR_BYTE_ARRAY);

        return ByteArray.combine(prefix);
    }

    /**
     * @see #prefixTerminated
     * @return Byte array version of {@link #prefixTerminated}.
     */
    public byte[] prefixTerminatedBytes(int n) {
        return this.prefixTerminated(n).toArray();
    }

    /**
     * Prefix of the Marshal for the first n entries. The results are not combined, and an end
     * terminator is not included.
     *
     * @param n The number of items to include in the prefix. That is, indices [0,n) will be
     * included in the result.
     * @return A list of byte array parts. The caller is free to mutate the resulting list as
     * needed.
     */
    private List<ByteArray> prefix(int n) {
        checkArgument(n >= 0, "The number of parts in the prefix must be non-negative.");
        checkArgument(n <= this.contents.size(),
                "The number of parts in the prefix must be <= the number of parts.");

        List<ByteArray> results = new ArrayList<ByteArray>();
        {
            int i = 0;
            for(Entry e : this.contents) {
                // process only 0-(n-1) entries
                if(i >= n)
                    break;
                i++;

                byte[] typeBytes = { e.getEntryType().getTypeCode() };
                ByteArray type = new ByteArray(typeBytes);
                ByteArray escapedData = escape(e.getData(), SEPARATOR);

                if(!results.isEmpty())
                    results.add(SEPARATOR_BYTE_ARRAY);
                results.add(type);
                results.add(escapedData);
            }
        }
        return results;
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
     * Returns the type of the field at the given index.
     *
     * @throws IndexOutOfBoundsException
     */
    public EntryType getTypeAt(int index) {
        return this.contents.get(index).getEntryType();
    }

    /**
     * Returns the object at the given index, regardless of type.
     */
    public Object getAt(int index) throws MarshalException {
        Entry entry = this.contents.get(index);
        return entry.getFieldObject();
    }

    public byte getByteAt(int index) throws MarshalException {
        Object o = this.getAt(index);
        if(o instanceof Byte) {
            return (Byte)o;
        }
        else {
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not byte array.");
        }
    }

    public ByteArray getByteArrayAt(int index) throws MarshalException {
        Object o = this.getAt(index);
        if(o instanceof ByteArray) {
            return (ByteArray)o;
        }
        else {
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not ByteArray.");
        }
    }

    public double getDoubleAt(int index) throws MarshalException {
        Object o = this.getAt(index);
        if(o instanceof Double)
            return (Double)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Double.");
    }

    public int getIntegerAt(int index) throws MarshalException {
        Object o = this.getAt(index);
        if(o instanceof Integer)
            return (Integer)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Integer.");
    }

    public long getLongAt(int index) throws MarshalException {
        Object o = this.getAt(index);
        if(o instanceof Long)
            return (Long)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not Long.");
    }

    public String getStringAt(int index) throws MarshalException {
        Object o = this.getAt(index);
        if(o instanceof String)
            return (String)o;
        else
            throw new MarshalException("Type at position " + index + " is " +
                    getTypeAt(index) + ", not String.");
    }

    public Marshal getMarshalAt(int index) throws MarshalException {
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
     * A subrange of the current Marshal starting at the given index and continuing through the
     * end of the Marshal.
     */
    public Marshal from(int fromIndex) {
        return subrange(fromIndex, this.size());
    }

    /**
     * A subrange of the current Marshal starting at the start of the Marshal continuing up to,
     * but not including, the given end index.
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

    /**
     * Whether the marshal is empty.
     */
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    @Override
    public int compareTo(Marshal other) {
        ByteArray bytes1 = this.toByteArray();
        ByteArray bytes2 = other.toByteArray();

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
     * For example, if escapeByte were 2 and the array was { 0, 1, 2, 3 }, then the output array
     * would be { 0, 1, 2, 2, 3 }.
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

        if(bytesToEscape == 0)
            return input;

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
     * For example, if unescapeByte were 2 and the array was { 0, 1, 2, 2, 3 }, then the output
     * array would be { 0, 1, 2, 3 }.
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
                // clear the escape byte if this is not an escape character, since there was an
                // invalid escaping
                lastWasEscaped = false;
            }
        }

        if(bytesToUnescape == 0)
            return input;

        // allocate the new byte array
        byte[] unescaped = new byte[input.size() - bytesToUnescape];

        // unescape the bytes
        lastWasEscaped = false;
        for(int sourceIndex = 0, targetIndex = 0;
                sourceIndex < input.size();
                sourceIndex++, targetIndex++) {
            byte b = input.getAt(sourceIndex);
            if(b == unescapeByte) {
                if(lastWasEscaped) {
                    lastWasEscaped = false;

                    // fall through write the single byte
                }
                else {
                    // escape byte; skip
                    // this removes one from the index, and then the loop adds it back again, so
                    // the position does not
                    // change
                    targetIndex--;
                    lastWasEscaped = true;
                    continue;
                }
            }
            else if(lastWasEscaped) {
                // not the escape byte, but the last byte was an escape byte, meaning this was an
                // inproper escape
                lastWasEscaped = false;

                // in addition, need to write the last byte, since it was skipped thinking that
                // the next character would be an escape character
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
     * If there is no non-escaped escape byte, then return value is one greater than the length of
     * the input, as if there were a separator one position past the end of the array.
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
