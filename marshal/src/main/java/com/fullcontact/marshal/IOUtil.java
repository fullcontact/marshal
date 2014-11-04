package com.fullcontact.marshal;

import com.google.common.base.Optional;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * I/O utility functions.
 *
 * @author Brandon Vargo
 */
final class IOUtil {
    /**
     * Encodes a non-negative integer as a variable-length byte array.
     *
     * The var int is a series of bytes where the lower 7 bits is the data, and the upper bit is set
     * iff there are more bytes to read (not unlike UTF-8's encoding mechanism).
     */
    public static byte[] encodeVarInt(int v) {
        checkArgument(v >= 0, "Integer to encode must be non-negative.");

        int B = 128;

        if(v < (1<<7)) {
            byte[] d = { (byte)(v) };
            return d;
        }
        else if(v < (1<<14)) {
            byte[] d = {
                (byte)((v | B) & 0xFF),
                (byte)((v>>7) & 0xFF)
            };
            return d;
        }
        else if(v < (1<<21)) {
            byte[] d = {
                (byte)(v | B),
                (byte)((v>>7) | B),
                (byte)(v>>14)
            };
            return d;
        }
        else if(v < (1<<28)) {
            byte[] d = {
                (byte)(v | B),
                (byte)((v>>7) | B),
                (byte)((v>>14) | B),
                (byte)(v>>21)
            };
            return d;
        }
        else {
            byte[] d = {
                (byte)(v | B),
                (byte)((v>>7) | B),
                (byte)((v>>14) | B),
                (byte)((v>>21) | B),
                (byte)(v>>28)
            };
            return d;
        }
    }

    /**
     * Decodes a non-negative integer encoded as a variable-length byte array.
     */
    public static Optional<Integer> decodeVarInt(ByteArray data) {
        int decoded = 0;

        for(int i = 0; i < data.size(); i++) {
            byte b = data.getAt(i);
            decoded |= ((b & 127) << (i * 7));

            // too big to be a signed integer
            // >= 8 if the number is just slightly bigger than 2^31-1
            // < 0 if the number is way bigger than 2^31-1, taking another frame (signed comparison)
            if(i == 4 && (b >= 8 || b < 0))
                return Optional.absent();

            // if no more bytes, stop reading
            // since bytes are signed in java, this is true iff the MSB is not set
            if(b >= 0)
                break;
        }

        return Optional.of(decoded);
    }

    /**
     * Writes a variable-size integer to the output.
     *
     * Uses less storage than writing a fixed-width integer for large input.
     */
    public static void writeVarInt(int n, DataOutput output) throws IOException {
        byte[] encoded = encodeVarInt(n);
        output.write(encoded);
    }

    /**
     * Reads a variable-size integer from the input.
     *
     * @throws IOException if the integer is invalid or there was an error reading the input.
     */
    public static int readVarInt(DataInput input) throws IOException {
        int decoded = 0;

        int i = 0;
        while(true) {
            byte b = input.readByte();
            decoded |= ((b & 127) << (i * 7));

            // too big to be a signed integer
            // >= 8 if the number is just slightly bigger than 2^31-1
            // < 0 if the number is way bigger than 2^31-1, taking another frame (signed comparison)
            if(i == 4 && (b >= 8 || b < 0))
                throw new IOException("Invalid variable-encoded integer.");

            // if no more bytes, stop reading
            // since bytes are signed in java, this is true iff the MSB is not set
            if(b >= 0)
                break;

            i++;
        }

        return decoded;
    }
}
