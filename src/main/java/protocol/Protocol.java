package protocol;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Represents the whole protocol from the article.
 * <p>
 * Source for the protocol: https://eprint.iacr.org/2017/1196.pdf
 * </p>
 */
class Protocol {
    private static final int IDENTITYBYTESIZE = 11;  // all characters are ASCII, so 1 char per byte // that size is just made up
    private static final int PUBLICSEEDBYTESIZE = 34;
    private static final int SALTBYTESIZE = 11;  // that size is just made up
    private final int coeffsByteSize;
    private final int n;
    private final BigInteger q;
    private final int eta;
    private final Engine engine;
    private final Ntt ntt;
    private final Mlkem mlkem;

    Protocol(int n, BigInteger q, int eta) {
        this.n = n;
        this.q = q;
        this.eta = eta;
        this.engine = new Engine();
        this.ntt = new Ntt(this.n, this.q);
        this.mlkem = new Mlkem(this.n, this.q);
        this.coeffsByteSize = n * (int) ((q.subtract(BigInteger.ONE).bitLength() + 1 + 7) / 8);  // + 1 because of the sign bit
        // ^^ ceiling
    }

    private void getEtaNoise(Polynomial r, byte[] seed) {
        byte[] buf = new byte[n * eta / 4];
        engine.prf(buf, seed);
        mlkem.generateCbdPolynomial(r, buf, eta);
    }

    private ByteBuffer readMessage(SocketChannel channel, int size) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            int bytesRead = channel.read(buffer);
            if (bytesRead < 0) return null;

            return buffer;
        } catch (IOException e) {
            // TODO do something
            return ByteBuffer.allocate(0);
        }
    }

    void phase0(SocketChannel channel) {
        // Save a, I, salt and v from the client //
        int totalLen = PUBLICSEEDBYTESIZE + IDENTITYBYTESIZE + SALTBYTESIZE + coeffsByteSize;
        ByteBuffer msgFromClient = readMessage(channel, totalLen);
        assert msgFromClient.capacity() == totalLen;
        // Extract publicSeed
        byte[] publicSeed = new byte[PUBLICSEEDBYTESIZE];
        msgFromClient.get(publicSeed);
        // Extract identity
        byte[] identityBytes = new byte[IDENTITYBYTESIZE];
        msgFromClient.get(identityBytes);
        String identity = new String(identityBytes, StandardCharsets.UTF_8);
        // Extract salt
        byte[] salt = new byte[SALTBYTESIZE];
        msgFromClient.get(salt);
        // Extract polynomial
        byte[] vBytes = new byte[coeffsByteSize];
        msgFromClient.get(vBytes);
        // Polynomial vNtt = Polynomial.fromBytes(vBytes, coeffsByteSize / vNttLength);  TODO
    }

    // TODO add sending and receiving to and from server
    void phase1(byte[] publicSeed) {
        Polynomial constantTwoPolyNtt = ntt.generateConstantTwoPolynomialNtt();
        // pi = as1 + 2e1 //
        // Compute a.
        Polynomial aNtt = new Polynomial(new BigInteger[n], q);
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeed);
        // Compute s1.
        Polynomial s1 = new Polynomial(new BigInteger[n], q);
        byte[] s1RandomSeed = new byte[34];
        engine.getRandomBytes(s1RandomSeed);
        getEtaNoise(s1, s1RandomSeed);
        Polynomial s1Ntt = ntt.convertToNtt(s1);
        // Compute e1.
        Polynomial e1 = new Polynomial(new BigInteger[n], q);
        byte[] e1RandomSeed = new byte[34];
        engine.getRandomBytes(e1RandomSeed);
        getEtaNoise(e1, e1RandomSeed);
        Polynomial e1Ntt = ntt.convertFromNtt(e1);
        // Do all the math
        Polynomial aS1Ntt = ntt.multiplyNttPolys(aNtt, s1Ntt);
        Polynomial twoE1Ntt = ntt.multiplyNttPolys(constantTwoPolyNtt, e1Ntt);
        Polynomial piNtt = ntt.add(aS1Ntt, twoE1Ntt);

    }

    // TODO
    void phase2() {
    }
}
