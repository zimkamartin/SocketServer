package protocol;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

// SOURCE for Server logic: https://www.baeldung.com/java-unix-domain-socket
public class Main {
    private static Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0)
            return Optional.empty();

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);
        return Optional.of(message);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Define a path for the socket file.
        Path socketPath = Path
                .of(System.getProperty("user.home"))
                .resolve("socket");
        // Transform the path into UnixDomainSocketAddress.
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        // Create a server socket channel with a Unix protocol.
        ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        // Bind the server socket channel with the socket address previously created.
        serverChannel.bind(socketAddress);
        // Wait for the first client connection.
        SocketChannel channel = serverChannel.accept();

        readSocketMessage(channel).ifPresent(message -> System.out.printf("[Client message] %s", message));
        // Lastly, Do not forget to delete the socket file.
        Files.deleteIfExists(socketPath);
    }
}