package protocol;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Represents a polynomial modulo (X^N + 1) with all coefficients modulo Q.
 * <p>
 * These polynomials are basic building blocks in the protocol.
 * </p>
 */
class Polynomial {

    /**
     * From the lowest to highest position in array, constant to X^(N-1) coefficients are stored.
     */
    private final BigInteger[] coefficients;
    /**
     * For communication with the client we want to know how many bytes do we need to store each coefficient.
     */
    private final int coeffBytes;

    Polynomial(BigInteger[] coefficients, BigInteger q) {
        this.coefficients = coefficients;
        this.coeffBytes = (int) ((q.subtract(BigInteger.ONE).bitLength() + 1 + 7) / 8);  // + 1 because of the sign bit
        // ^^ ceiling
    }

    void setCoeffIndex(int i, BigInteger val)
    {
        this.coefficients[i] = val;
    }

    BigInteger getCoeffIndex(int i)
    {
        return this.coefficients[i];
    }

    BigInteger[] getCoeffs()
    {
        return this.coefficients;
    }

    // Serialize Polynomial → Byte[]
    public byte[] toBytes() {
        int coeffSize = coefficients.length * coeffBytes;
        // Allocate buffer for all coefficients
        ByteBuffer buffer = ByteBuffer.allocate(coeffSize);
        // Write each coefficient
        for (BigInteger coeff : coefficients) {
            byte[] raw = coeff.toByteArray();
            byte[] fixed = new byte[coeffSize];  // filled by zeros by default
            System.arraycopy(raw, 0, fixed, coeffSize - raw.length, raw.length);
            buffer.put(fixed);
        }
        return buffer.array();
    }
}
