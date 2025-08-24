package protocol;

import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import java.security.SecureRandom;

/**
 * Represents engine of the protocol, so all functions that must have some internal state during the run of the protocol.
 * <p>
 * Represents XOF function - SHAKE128, hash function - SHA3-256, and random function - SecureRandom().
 * Heavily inspired by
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/Symmetric.java
 * and
 * https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMEngine.java
 * </p>
 */
class Engine {
    final int xofBlockBytes;
    private final SHAKEDigest xof;
    private final SHA3Digest sha3Digest256;
    private final SHAKEDigest shakeDigest;
    private final SecureRandom random;

    Engine() {
        this.xofBlockBytes = 168;
        this.xof = new SHAKEDigest(128);
        this.sha3Digest256 = new SHA3Digest(256);
        this.shakeDigest = new SHAKEDigest(256);
        this.random = new SecureRandom();
    }

    void xofAbsorb(byte[] seed) {
        xof.reset();
        xof.update(seed, 0, seed.length);
    }

    void xofSqueezeBlocks(byte[] out, int outOffset, int outLen) {
        xof.doOutput(out, outOffset, outLen);
    }

    void hash(byte[] out, byte[] in) {
        sha3Digest256.update(in, 0, in.length);
        sha3Digest256.doFinal(out, 0);
    }

    void prf(byte[] out, byte[] seed) {
        shakeDigest.update(seed, 0, seed.length);
        shakeDigest.doFinal(out, 0, out.length);
    }

    public void getRandomBytes(byte[] buf) {
        this.random.nextBytes(buf);
    }

    public int getRandomBit() {
        return this.random.nextInt(2);
    }
}