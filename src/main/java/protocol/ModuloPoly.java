package protocol;

import java.math.BigInteger;

/**
 * Represents all modulo polynomials in NTT tree.
 * <p>
 * These polynomials are of the form X^powerX P (zeta_indexZeta)^powerZeta,
 * where P is plus if plus, else it is minus.
 * Attribute powerX does not need to be stored.
 * For more details see https://electricdusk.com/ntt.html
 * </p>
 */
class ModuloPoly {

    private final boolean plus;
    private final BigInteger powerZeta;
    private final BigInteger indexZeta;

    ModuloPoly(boolean plus, BigInteger powerZeta, BigInteger indexZeta) {
        this.plus = plus;
        this.powerZeta = powerZeta;
        this.indexZeta = indexZeta;
    }

    boolean getPlus() {
        return plus;
    }

    BigInteger getPowerZeta() {
        return powerZeta;
    }

    BigInteger getIndexZeta() {
        return indexZeta;
    }
}