package protocol;

import java.io.IOException;
import java.math.BigInteger;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is a demo (and proof-of-concept) of the protocol https://eprint.iacr.org/2017/1196.pdf
 * <p>
 * N - polynomial size - must be power of 2 and fit into int data type
 * Q - defines Z_Q for coefficients in the polynomials - must be prime, must be congruent with 1 modulo 2 * N
 * ETA - defines Central binomial distribution when generating error polynomials.
 * SOURCE for Server communication logic: https://www.baeldung.com/java-unix-domain-socket
 * SOURCE for simple message exchange: CHATGPT
 * </p>
 */
public class Main {

    private static final int N = 1024;
    private static final BigInteger Q = BigInteger.valueOf(1073479681);
    private static final int ETA = 3;

    static byte[] publicSeed;

    private static void sendMessage(SocketChannel channel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public static void main(String[] args) throws IOException {

        Protocol protocol = new Protocol(N, Q, ETA);

        Path socketPath = Path.of(System.getProperty("user.home")).resolve("socket");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverChannel.bind(socketAddress);

            try (SocketChannel channel = serverChannel.accept()) {

                protocol.phase0(channel);

                // 2. Respond to client
                String reply1 = "Hello, I am the server!";
                sendMessage(channel, reply1);
                System.out.println("[Server] Sent: " + reply1);

                // 3. Receive another message
                String msg2 = readMessage(channel);
                System.out.println("[Client] " + msg2);

                // 4. Final response
                String reply2 = "Goodbye!";
                sendMessage(channel, reply2);
                System.out.println("[Server] Sent: " + reply2);
            }
        } finally {
            Files.deleteIfExists(socketPath); // server owns the socket file
        }
    }
}
