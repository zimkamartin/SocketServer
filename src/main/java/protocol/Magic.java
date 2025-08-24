package protocol;

import java.math.BigInteger;

/**
 * Represents all functions needed to secretly transform information to other party without intruder knowing.
 * <p>
 * Implements functions Hint function, Signal function and robust Extractor. All from the article
 * https://eprint.iacr.org/2017/1196.pdf
 * Also implements Symmetric modulo as defined in
 * https://youtu.be/h5pfTIE6slU?si=-EeOGTV0QD5QzbpY&t=543
 * </p>
 */
class Magic {

    private final BigInteger q;

    Magic(BigInteger q) {
        this.q = q;
    }

    int hintFunction(BigInteger x, int b) {
        x = symmetricModulo(x);  // Make sure that x is result of a symmetric modulo.
        BigInteger leftBound = q.divide(BigInteger.valueOf(4)).negate().add(BigInteger.valueOf(b));  // floor after the division is implicit here
        BigInteger rightBound = q.divide(BigInteger.valueOf(4)).add(BigInteger.valueOf(b));  // floor after the division is implicit here
        return (x.compareTo(leftBound) >= 0 && x.compareTo(rightBound) <= 0) ? 0 : 1;
    }

    int signalFunction(Engine e, BigInteger y) {
        int b = e.getRandomBit();
        return hintFunction(y, b);
    }

    // We know, that q is odd (since it is prime), so this function is implemented only for this case.
    BigInteger symmetricModulo(BigInteger r) {
        r = r.mod(q);  // Make sure that r is in Z_q.
        return r.compareTo((q.subtract(BigInteger.ONE)).divide(BigInteger.TWO)) <= 0 ? r : r.subtract(q);
    }

    int robustExtractor(BigInteger x, int w) {
        x = symmetricModulo(x);  // Make sure that x is result of a symmetric modulo.
        return symmetricModulo(x.add(BigInteger.valueOf(w).multiply((q.subtract(BigInteger.ONE)).divide(BigInteger.TWO)))).mod(BigInteger.TWO).intValue();
    }
}
