package protocol;

import java.math.BigInteger;

public class Polynomial {

    private final BigInteger[] coefficients;

    Polynomial(BigInteger[] coefficients) {
        this.coefficients = coefficients;
    }

    public void setCoeffIndex(int i, BigInteger val)
    {
        this.coefficients[i] = val;
    }

    public BigInteger[] getCoeffs()
    {
        return this.coefficients;
    }
}
