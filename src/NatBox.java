import java.io.*;
import java.net.*;
import java.util.*;

public class NatBox {
    private ServerSocket serverSocket;

    public NatBox(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                InetAddress address = socket.getInetAddress();
                System.out.println(
                        "Client IP: " + address.getHostAddress() + ", MAC: " + getMacAddress(address) + " connected");
                // ClientHandler clientHandler = new ClientHandler(socket);
                System.out.println("Client accepted: " + socket);

                DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));

                boolean done = false;
                while (!done) {
                    try {
                        String line = dis.readUTF();
                        System.out.println(line);
                        done = line.equals("bye");
                    } catch (IOException ioe) {
                        done = true;
                    }
                }

                // Thread thread = new Thread(clientHandler);
                // thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public String getMacAddress(InetAddress ip) throws UnknownHostException {
        try {

            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (SocketException e) {
            return "ERROR";
        }

    }

    private String randomMAC() {
        Random r = new Random();
        byte[] mac = new byte[6];
        r.nextBytes(mac);
        mac[0] = (byte) (mac[0] & (byte) 254);
        StringBuilder str = new StringBuilder(18);
        for (byte b : mac) {
            if (str.length() > 0)
                str.append(":");
            str.append(String.format("%02x", b));
        }
        return str.toString();
    }

    private String randomIP() {
        Random r = new Random();
        String ip = r.nextInt(256) + "";
        for (int i = 0; i < 3; i++) {
            ip += "." + r.nextInt(256);
        }
        return ip;
    }

    /**
     * Method that closes server socket
     */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 1234;

        ServerSocket serverSocket = new ServerSocket(port);
        NatBox box = new NatBox(serverSocket);
        box.start();
    }
}
