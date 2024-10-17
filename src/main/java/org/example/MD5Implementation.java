package org.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MD5Implementation implements Hashing {

    // MD5 buffer variables (A, B, C, D)
    private int A = 0x67452301;
    private int B = 0xefcdab89;
    private int C = 0x98badcfe;
    private int D = 0x10325476;

    // Constants for the MD5 algorithm
    private static final int[] S = {
            7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
            5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
            4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
            6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    };

    private static final int[] T = new int[64];
    static {
        for (int i = 0; i < 64; i++) {
            T[i] = (int) (long) ((1L << 32) * Math.abs(Math.sin(i + 1)));
        }
    }

    // Padding the message
    public byte[] padMessage(byte[] input) {
        int originalLength = input.length;
        int paddedLength = (originalLength + 8 + 64) / 64 * 64; // Add 64-bit length and pad to 512-bit block
        byte[] padded = Arrays.copyOf(input, paddedLength);

        // Add 1 bit followed by zeros
        padded[originalLength] = (byte) 0x80;

        // Add original length in bits as a 64-bit number at the end
        long bitLength = (long) originalLength * 8;
        for (int i = 0; i < 8; i++) {
            padded[padded.length - 8 + i] = (byte) (bitLength >>> (8 * i));
        }
        return padded;
    }

    // Process a single 512-bit block
    private void processBlock(byte[] block) {
        int[] X = new int[16];

        // Decode block into 16 32-bit words
        ByteBuffer buffer = ByteBuffer.wrap(block);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 16; i++) {
            X[i] = buffer.getInt();
        }

        // Save A, B, C, D
        int AA = A;
        int BB = B;
        int CC = C;
        int DD = D;

        // Main MD5 loop (4 rounds)
        for (int i = 0; i < 64; i++) {
            int F, g;
            if (i < 16) {
                F = (B & C) | (~B & D);
                g = i;
            } else if (i < 32) {
                F = (D & B) | (~D & C);
                g = (5 * i + 1) % 16;
            } else if (i < 48) {
                F = B ^ C ^ D;
                g = (3 * i + 5) % 16;
            } else {
                F = C ^ (B | ~D);
                g = (7 * i) % 16;
            }
            F = F + A + T[i] + X[g];
            A = D;
            D = C;
            C = B;
            B = B + Integer.rotateLeft(F, S[i]);
        }

        // Add results to A, B, C, D
        A += AA;
        B += BB;
        C += CC;
        D += DD;
    }

    @Override
    public byte[] compute(byte[] input) {
        byte[] paddedMessage = padMessage(input);

        // Process each 512-bit block
        for (int i = 0; i < paddedMessage.length / 64; i++) {
            byte[] block = Arrays.copyOfRange(paddedMessage, i * 64, (i + 1) * 64);
            processBlock(block);
        }

        // Produce the final 128-bit (16-byte) result
        ByteBuffer result = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        result.putInt(A).putInt(B).putInt(C).putInt(D);
        return result.array();
    }
}
