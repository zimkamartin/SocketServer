package protocol;

import java.math.BigInteger;

/**
 * Extracted (and modified) all needed functions from MLKEM.
 * <p>
 * Functions are modified so they can dynamically adapt to different N, Q, ETA.
 * However, their building blocks are heavily inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMIndCpa.java
 * and
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/CBD.java
 * </p>
 */
class Mlkem {

    private final int n;
    private final BigInteger q;

    Mlkem(int n, BigInteger q) {
        this.n = n;
        this.q = q;
    }

    // TODO: Change it for dynamic n, q.
    private int rejectionSampling(Polynomial outputBuffer, int coeffOff, int len, byte[] inpBuf, int inpBufLen) {
        int ctr, pos;  // number of sampled coeffs and possition in inpBuf
        BigInteger val0, val1, val2, val3;  // candidates for coefficients
        ctr = pos = 0;
        while (ctr < len && pos + 15 <= inpBufLen) {
            // The following should look like this:
            // val0 = 00C[0]1.1/4 C[01]1.1/2 C[01]2.1/2 C[02]1.1/2 C[02]2.1/2 C[03]1.1/2 C[03]2.1/2 C[13]1.1/2
            // val1 = 00C[0]2.1/4 C[04]1.1/2 C[04]2.1/2 C[05]1.1/2 C[05]2.1/2 C[06]1.1/2 C[06]2.1/2 C[13]2.1/2
            // val2 = 00C[0]3.1/4 C[07]1.1/2 C[07]2.1/2 C[08]1.1/2 C[08]2.1/2 C[09]1.1/2 C[09]2.1/2 C[14]1.1/2
            // val3 = 00C[0]4.1/4 C[10]1.1/2 C[10]2.1/2 C[11]1.1/2 C[11]2.1/2 C[12]1.1/2 C[12]2.1/2 C[14]2.1/2,
            // where each block is precisely 4 bits
            // and 00C[0]1.1/4 stands for 2 zero bits followed by first fourth from byte on position 0 in inpBuf.
            val0 = BigInteger.valueOf(((((int)(inpBuf[pos +  0] & 0xFF)) << 22) & 0x30000000) |
                    ((((int)(inpBuf[pos +  1] & 0xFF)) << 20) & 0x0FF00000) |
                    ((((int)(inpBuf[pos +  2] & 0xFF)) << 12) & 0x000FF000) |
                    ((((int)(inpBuf[pos +  3] & 0xFF)) <<  4) & 0x00000FF0) |
                    ((((int)(inpBuf[pos + 13] & 0xFF)) >>  4) & 0x0000000F));
            val1 = BigInteger.valueOf(((((int)(inpBuf[pos +  0] & 0xFF)) << 24) & 0x30000000) |
                    ((((int)(inpBuf[pos +  4] & 0xFF)) << 20) & 0x0FF00000) |
                    ((((int)(inpBuf[pos +  5] & 0xFF)) << 12) & 0x000FF000) |
                    ((((int)(inpBuf[pos +  6] & 0xFF)) <<  4) & 0x00000FF0) |
                    ((((int)(inpBuf[pos + 13] & 0xFF)) >>  0) & 0x0000000F));
            val2 = BigInteger.valueOf(((((int)(inpBuf[pos +  0] & 0xFF)) << 26) & 0x30000000) |
                    ((((int)(inpBuf[pos +  7] & 0xFF)) << 20) & 0x0FF00000) |
                    ((((int)(inpBuf[pos +  8] & 0xFF)) << 12) & 0x000FF000) |
                    ((((int)(inpBuf[pos +  9] & 0xFF)) <<  4) & 0x00000FF0) |
                    ((((int)(inpBuf[pos + 14] & 0xFF)) >>  4) & 0x0000000F));
            val3 = BigInteger.valueOf(((((int)(inpBuf[pos +  0] & 0xFF)) << 28) & 0x30000000) |
                    ((((int)(inpBuf[pos + 10] & 0xFF)) << 20) & 0x0FF00000) |
                    ((((int)(inpBuf[pos + 11] & 0xFF)) << 12) & 0x000FF000) |
                    ((((int)(inpBuf[pos + 12] & 0xFF)) <<  4) & 0x00000FF0) |
                    ((((int)(inpBuf[pos + 14] & 0xFF)) >>  0) & 0x0000000F));
            pos = pos + 15;
            if (val0.compareTo(q) < 0) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val0);
                ctr++;
            }
            if (ctr < len && val1.compareTo(q) < 0) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val1);
                ctr++;
            }
            if (ctr < len && val2.compareTo(q) < 0) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val2);
                ctr++;
            }
            if (ctr < len && val3.compareTo(q) < 0) {
                outputBuffer.setCoeffIndex(coeffOff + ctr, val3);
                ctr++;
            }
        }
        return ctr;
    }

    // TODO: Change it for dynamic n, q.
    // TODO: Solve q.intValue()
    void generateUniformPolynomialNtt(Engine e, Polynomial a, byte[] seed) {
        int KyberGenerateMatrixNBlocks = (int)  // its value is 23  // !!! Conversions BigInteger -> int
                (
                        (
                                (float) 30 * n / 8  // how many bytes do I need to sample
                                        * (float) (1 << 30) / q.intValue()  // (probability of success) ^ -1, SO together it is in average how many bytes we need for sampling
                                        + (float) e.xofBlockBytes
                        )
                                / e.xofBlockBytes  // thanks to `+ xBB / xBB` we now have the closest needed higher amount of xBB
                );
        int k, ctr, off;
        int buflen = KyberGenerateMatrixNBlocks * e.xofBlockBytes;  // currently it is not divisible by 15!
        byte[] buf = new byte[buflen];
        e.xofAbsorb(seed);
        e.xofSqueezeBlocks(buf, 0, buflen);

        ctr = rejectionSampling(a, 0, n, buf, buflen);  // number of samples coefficients

        while (ctr < n) {  // we did not sample enough coeffs
            off = buflen % 15;  // how many unused bytes is in buf?
            for (k = 0; k < off; k++) {  // move unused bytes to the beginning of the buf
                buf[k] = buf[buflen - off + k];
            }
            e.xofSqueezeBlocks(buf, off, buflen - off);  // fill the rest of buf
            ctr += rejectionSampling(a, ctr, n - ctr, buf, buflen);
        }
    }

    private static long convertByteTo24BitUnsignedInt(byte[] x, int offset) {
        long r = (long)(x[offset] & 0xFF);
        r = r | (long)((long)(x[offset + 1] & 0xFF) << 8);
        r = r | (long)((long)(x[offset + 2] & 0xFF) << 16);
        return r;
    }

    // TODO: Change it for dynamic eta.
    // TODO: Figure out suitable eta value for given n and q.
    void generateCbdPolynomial(Polynomial r, byte[] bytes, int eta) {
        long t, d;
        int a, b;

        for (int i = 0; i < n / 4; i++) {  // When eta is equal to 3.  // !!! Conversion to int !!!
            t = convertByteTo24BitUnsignedInt(bytes, 3 * i);
            d = t & 0x00249249;
            d = d + ((t >> 1) & 0x00249249);
            d = d + ((t >> 2) & 0x00249249);
            for (int j = 0; j < 4; j++)
            {
                a = (short)((d >> (6 * j + 0)) & 0x7);
                b = (short)((d >> (6 * j + 3)) & 0x7);
                short diffShort = (short)(a - b);
                BigInteger diffBI = BigInteger.valueOf(diffShort);
                r.setCoeffIndex(4 * i + j, diffBI.mod(q));
            }
        }
    }
}
