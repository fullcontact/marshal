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

    @Test
    public void testUtf() throws IOException {
        String str = "aeiouáéíóúäëïöüabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" +
            "-+={}[]()<> `'!@#$%^&*:;/\\|.,\u2603\u0000";
        for(int i = 1; i <= str.length(); i++) {
            String s = str.substring(0, i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            IOUtil.writeUtf(s, new DataOutputStream(baos));

            String decoded = IOUtil.readUtf(
                    new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
            assertEquals(s, decoded);
        }
    }

    @Test
    public void testUtf__long() throws IOException {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < Math.pow(2, 16) + 50; i++) {
            sb.append("1");
        }

        String s = sb.toString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        IOUtil.writeUtf(s, new DataOutputStream(baos));

        String decoded = IOUtil.readUtf(
                new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        assertEquals(s, decoded);
    }

    @Test
    public void testUtf__empty() throws IOException {
        String s = "";

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        IOUtil.writeUtf(s, new DataOutputStream(baos));

        String decoded = IOUtil.readUtf(
                new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        assertEquals(s, decoded);
    }
}
