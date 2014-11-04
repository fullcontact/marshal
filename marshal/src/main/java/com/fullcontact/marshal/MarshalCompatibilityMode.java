package com.fullcontact.marshal;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * A compatibility adapter for Marshal objects.
 *
 * @author Brandon Vargo
 */
public enum MarshalCompatibilityMode {
    // sherlock HBase
    SHERLOCK(new ImmutableMap.Builder<Byte, Byte>()
        // byte array
        .put((byte)0, (byte)1)
        // double
        .put((byte)1, (byte)2)
        // integer
        .put((byte)2, (byte)3)
        // long
        .put((byte)3, (byte)4)
        // string
        .put((byte)4, (byte)5)
        // marshal
        .put((byte)5, (byte)6)
        // empty
        .put((byte)6, (byte)0xFF)
        .build()),

    // strings encoded using pre-1.0 marshal library
    LEGACY_STRINGS(new ImmutableMap.Builder<Byte, Byte>()
        // "move" strings to a fake type, legacy strings, so that deserialization works
        .put(EntryType.STRING.getTypeCode(), EntryType.STRING_LEGACY.getTypeCode())
        .build());

    // type conversions
    // the key is the type byte used by the compatibility mode
    // the value is the type byte used by the default implementation
    private final Map<Byte, Byte> typeConversions;

    private MarshalCompatibilityMode(Map<Byte, Byte> typeConversions) {
        if(typeConversions == null)
            typeConversions = Collections.emptyMap();
        this.typeConversions = typeConversions;
    }

    /**
     * Converts a type byte to the type byte used by the native mode.
     */
    public byte convertType(byte inputType) {
        // find a type conversion; if no conversion is given, then the input type will be returned
        Byte nativeType = this.typeConversions.get(inputType);
        if(nativeType == null)
            return inputType;
        else
            return nativeType;
    }
}
