package com.fullcontact.marshal;

import com.google.common.base.Optional;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;

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

    /**
     * Writes the string to the data output.
     *
     * Java's {@link DataOutput#writeUTF(String)} is limited to 2^16-1 characters, as it writes a
     * length as a short and then the data. This version supports all strings by using a
     * variable-encoded int, using the same modified UTF-8 format for the data. Note that the
     * formats are not interchangeable.
     *
     * @throws IOException if the output could not be read.
     * @throws UTFDataFormatException if the input "modified UTF-8" format is longer than 2^31-1
     * bytes.
     */
    public static void writeUtf(String s, DataOutput output) throws IOException {
        // find the length of the encoded modified utf-8 format
        // note that string length is an int, so utflen could wrap around
        // if this happens, we detect it and blow up
        int utflen = 0;
        {
            int c;
            for(int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                if((c >= 0x0001) && (c <= 0x007F)) {
                    // one byte
                    utflen++;
                }
                else if (c > 0x07FF) {
                    // three bytes
                    utflen += 3;
                }
                else {
                    // two bytes
                    // includes null byte
                    utflen += 2;
                }
            }
        }

        if(utflen < 0)
            throw new UTFDataFormatException("Encoded string is too long.");

        byte[] utflenEncoded = encodeVarInt(utflen);

        // in memory byte array for writing the string
        // length of varint + length of data
        byte[] bytes = new byte[utflenEncoded.length + utflen];
        System.arraycopy(utflenEncoded, 0, bytes, 0, utflenEncoded.length);

        // "write" bytes
        try {
            // start past the varint length
            int count = utflenEncoded.length;

            if(utflen == s.length()) {
                // fast path, for strings that do not have special characters
                for(int i = 0; i < s.length(); i++) {
                    int c = s.charAt(i);
                    bytes[count++] = (byte)c;
                }
            }
            else {
                // slow path, for strings that do have special characters
                for(int i = 0; i < s.length(); i++) {
                    int c = s.charAt(i);
                    if((c >= 0x0001) && (c <= 0x007F)) {
                        // one byte
                        bytes[count++] = (byte)c;

                    }
                    else if(c > 0x07FF) {
                        // three byte
                        bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                        bytes[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                        bytes[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
                    }
                    else {
                        // two byte
                        bytes[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                        bytes[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
                    }
                }
            }

            assert(count == bytes.length);
        }
        catch(IndexOutOfBoundsException e) {
            // this can only happen if utflen wrapper around into the positive again, so our array
            // is too short
            // instead of doing the bounds check ourselves, we let java do it, and then catch the
            // exception here
            throw new UTFDataFormatException("Encoded string is too long.");
        }

        output.write(bytes);
    }

    /**
     * Reads the string from the data input.
     *
     * Java's {@link DataOutput#writeUTF(String)} is limited to 2^16-1 characters, as it writes a
     * length as a short and then the data. This version supports all strings by using a
     * variable-encoded int, using the same modified UTF-8 format for the data. Note that the
     * formats are not interchangeable.
     *
     * @throws IOException if the input could not be read.
     * @throws UTFDataFormatException if the input is not valid "modified UTF-8"
     */
    public static String readUtf(DataInput input) throws IOException {
        int utflen = readVarInt(input);

        byte[] bytes = new byte[utflen];
        input.readFully(bytes, 0, utflen);

        // decode to characters
        // utflen is the upper bound - actual characters may be less
        char[] chars = new char[utflen];
        int chars_count = 0;
        {
            int count = 0;

            // fast path - no special characters
            while(count < utflen) {
                int c = bytes[count] & 0xFF;
                if(c > 127)
                    break;
                count++;
                chars[chars_count++]=(char)c;
            }

            // slow path - special characters
            while(count < utflen) {
                int c = bytes[count] & 0xFF;
                switch (c >> 4) {
                    case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                        // 0xxxxxxx
                        count++;
                        chars[chars_count++] = (char)c;
                        break;
                    case 12: case 13:
                        // 110x xxxx 10xx xxxx
                        count += 2;
                        if(count > utflen)
                            throw new UTFDataFormatException("Malformed input: partial character at end");
                        int c2 = (int)bytes[count-1];
                        if((c2 & 0xC0) != 0x80)
                            throw new UTFDataFormatException("Malformed input around byte " + count);
                        chars[chars_count++] = (char)(((c  & 0x1F) << 6) |
                                                           (c2 & 0x3F) << 0);
                        break;
                    case 14:
                        // 1110 xxxx  10xx xxxx  10xx xxxx
                        count += 3;
                        if(count > utflen)
                            throw new UTFDataFormatException("Malformed input: partial character at end");
                        int char2 = (int)bytes[count-2];
                        int char3 = (int)bytes[count-1];
                        if(((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                            throw new UTFDataFormatException("Malformed input around byte " + (count-1));
                        chars[chars_count++] = (char)(((c     & 0x0F) << 12) |
                                                      ((char2 & 0x3F) << 6)  |
                                                      ((char3 & 0x3F) << 0));
                        break;
                    default:
                        // 10xx xxxx,  1111 xxxx
                        throw new UTFDataFormatException("Malformed input around byte " + count);
                }
            }
        }

        return new String(chars, 0, chars_count);
    }
}
