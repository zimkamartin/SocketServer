package protocol;

import java.math.BigInteger;

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

    Polynomial(BigInteger[] coefficients) {
        this.coefficients = coefficients;
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
}
