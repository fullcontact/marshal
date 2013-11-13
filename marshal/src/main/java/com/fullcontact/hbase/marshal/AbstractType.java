package com.fullcontact.hbase.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Base class for all types that can be marshaled using the marshaling system.
 *
 * @author Brandon Vargo
 */
abstract class AbstractType<T> {
    /**
     * Marshals the given type into a byte array. The ordering should be byte lexographical if ordering is supported.
     */
    public abstract ByteArray marshal(T object);

    /**
     * Demarshals the byte array into the given object type.
     */
    public abstract T demarshal(ByteArray data);

    /**
     * Write the current value to the data output. This need not be lexographical.
     */
    public abstract void write(T object, DataOutput dataOutput) throws IOException;

    /**
     * Read the current value to the data input.
     */
    public abstract T read(DataInput dataInput) throws IOException;
}
