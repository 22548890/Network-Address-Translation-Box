import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class NatBox {
    private ServerSocket serverSocket;

    public NatBox(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    private String randomMAC() {
        Random r = new Random();
        byte[] mac = new byte[6];
        r.nextBytes(mac);
        mac[0] = (byte)(mac[0] & (byte)254); 
        StringBuilder str = new StringBuilder(18);
        for(byte b : mac){
            if(str.length() > 0)
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
