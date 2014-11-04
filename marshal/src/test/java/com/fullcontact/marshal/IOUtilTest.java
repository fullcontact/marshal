package com.fullcontact.marshal;

import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests for I/O utilities.
 *
 * @author Brandon Vargo
 */
@RunWith(JUnit4.class)
public class IOUtilTest {
    @Test
    public void testVarInt() {
        Random r = new Random(0);

        for(int i = 0; i < 1000; i++) {
            for(int l = 0; l < 32; l++) {
                int n = Math.abs(r.nextInt()) % (1 << l);
                byte[] encoded = IOUtil.encodeVarInt(n);
                Optional<Integer> decoded = IOUtil.decodeVarInt(new ByteArray(encoded));
                assertEquals(n, (int)decoded.get());
            }
        }
    }

    @Test
    public void testVarIntDirect() throws IOException {
        Random r = new Random(0);

        for(int i = 0; i < 1000; i++) {
            for(int l = 0; l < 32; l++) {
                int n = Math.abs(r.nextInt()) % (1 << l);

                ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
                IOUtil.writeVarInt(n, new DataOutputStream(baos));

                int decoded = IOUtil.readVarInt(
                        new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
                assertEquals(n, decoded);
            }
        }
    }

    @Test
    public void testVarInt__specialCases() {
        int[] ns = { Integer.MAX_VALUE, 0, 1 };
        for(int n : ns) {
            byte[] encoded = IOUtil.encodeVarInt(n);
            Optional<Integer> decoded = IOUtil.decodeVarInt(new ByteArray(encoded));
            assertEquals(n, (int)decoded.get());
        }
    }

    @Test
    public void testVarInt__specialCasesDirect() throws IOException {
        int[] ns = { Integer.MAX_VALUE, 0, 1 };
        for(int n : ns) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            IOUtil.writeVarInt(n, new DataOutputStream(baos));

            int decoded = IOUtil.readVarInt(
                    new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
            assertEquals(n, decoded);
        }
    }

    @Test
    public void testVarInt__tooLong() {
        byte[] b1 = { -1, -1, -1, -1, -1, -1 };
        Optional<Integer> d1 = IOUtil.decodeVarInt(new ByteArray(b1));
        assertEquals(Optional.<Integer>absent(), d1);

        byte[] b2 = { -1, -1, -1, -1, 112 };
        Optional<Integer> d2 = IOUtil.decodeVarInt(new ByteArray(b2));
        assertEquals(Optional.<Integer>absent(), d2);
    }

    @Test(expected=IOException.class)
    public void testVarInt__tooLongDirect1() throws IOException {
        byte[] bytes = { -1, -1, -1, -1, -1, -1 };
        IOUtil.readVarInt(new DataInputStream(new ByteArrayInputStream(bytes)));
    }

    @Test(expected=IOException.class)
    public void testVarInt__tooLongDirect2() throws IOException {
        byte[] bytes = { -1, -1, -1, -1, 112 };
        IOUtil.readVarInt(new DataInputStream(new ByteArrayInputStream(bytes)));
    }
}
