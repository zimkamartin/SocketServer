package protocol;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

// SOURCE for Server logic: https://www.baeldung.com/java-unix-domain-socket
// SOURCE for simple message exchange: CHAT GPT
public class Main {

    private static String readMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0) return null;

        buffer.flip();
        byte[] bytes = new byte[bytesRead];
        buffer.get(bytes);
        return new String(bytes);
    }

    private static void sendMessage(SocketChannel channel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public static void main(String[] args) throws IOException {
        Path socketPath = Path.of(System.getProperty("user.home")).resolve("socket");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverChannel.bind(socketAddress);

            try (SocketChannel channel = serverChannel.accept()) {
                // 1. Receive from client
                System.out.println("[Server] Waiting for client message...");
                String msgFromClient = readMessage(channel);
                System.out.println("[Client] " + msgFromClient);

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
