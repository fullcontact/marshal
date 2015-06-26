package com.fullcontact.marshal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Known types allowed to be added to a marshal.
 *
 * Note that these values *cannot* be changed without breaking binary compatibility.
 */
public enum EntryType {
    BYTE              ((byte)0x00, ByteType.INSTANCE),
    BYTE_ARRAY        ((byte)0x01, ByteArrayType.INSTANCE),
    DOUBLE            ((byte)0x02, DoubleType.INSTANCE),
    INTEGER           ((byte)0x03, IntegerType.INSTANCE),
    LONG              ((byte)0x04, LongType.INSTANCE),
    STRING            ((byte)0x05, StringType.INSTANCE),
    MARSHAL           ((byte)0x06, MarshalType.INSTANCE),
    // SEPARATOR (0xFE) is reserved and cannot be used
    // formally the empty Marshal indicator; now left for compatibility
    LEGACY_EMPTY      ((byte)0xFF, null);

    private static final ImmutableMap<Byte, EntryType> ENTRY_TYPE_CODES;
    static {
        ImmutableMap.Builder<Byte, EntryType> builder = ImmutableMap.builder();
        for(EntryType type : EntryType.values()) {
            builder.put(type.getTypeCode(), type);
        }
        ENTRY_TYPE_CODES = builder.build();
    }

    private final byte typeCode;
    private final AbstractType<?> type;

    private EntryType(byte typeCode, AbstractType<?> type) {
        this.typeCode = typeCode;
        this.type = type;
    }

    static Optional<EntryType> forCode(byte b) {
        EntryType result = ENTRY_TYPE_CODES.get(b);
        return Optional.fromNullable(result);
    }

    /**
     * A single byte that indicates the type code for this entry type.
     */
    byte getTypeCode() {
        return typeCode;
    }

    AbstractType<?> getType() {
        return this.type;
    }
}
