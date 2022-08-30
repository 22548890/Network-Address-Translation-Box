import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class NatBox {
    private final int poolSize = 8;

    private ServerSocket serverSocket;
    private String ip;
    private String mac;
    private int aivalablePort = 0;
    private ArrayList<TableRow> table = new ArrayList<TableRow>();
    private ArrayList<String> pool = new ArrayList<String>();
    private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private HashMap<String, String> arp = new HashMap<String, String>();

    public NatBox(ServerSocket serverSocket, String ip) {
        this.serverSocket = serverSocket;
        this.ip = ip;
        this.mac = randomMAC();
        
        for (int i = 1; i <= poolSize; i++) {
            pool.add("10.0.0." + i);
        }
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress().getHostAddress());
                System.out.println("Client connected");
                
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandlers.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public String popIPfromPool() { 
        if (pool != null) {
            String ip = pool.get(0);
            pool.remove(0);
            return ip;
        } else {
            System.out.println("ERROR: Pool Empty");
            return null;
        }
    }

    public void addIPtoPool(String ip) {
        if (!pool.contains(ip)) {
            pool.add(ip);
        }
    }

    public void addRow(TableRow row) { 
        table.add(row); 
    }

    public void arpPut(String ip, String mac) {
        arp.put(ip, mac);
    }

    public String getIP() { return ip; }

    public String getMAC() { return mac; }

    public int getAivalablePort() { return ++aivalablePort; }

    public void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client Disconnected");
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

    public void printTable() { 
        if (table != null) {
            System.out.println();
            System.out.println("--------------------------------------------------------");
            System.out.println("Client_IP | Client_Port |      NAT-box_IP | NAT-box_Port");
            System.out.println("--------------------------------------------------------");
            for (TableRow row : table) {
                String format = "%9s |%12s |%16s |%13s %n";
                System.out.printf(format, row.getClientIP(), row.getClientPort(), row.getNatIP(), row.getNatPort());
            }
        }
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
        int port = 2345;

        System.out.print("Public IP address: ");
        Scanner sc = new Scanner(System.in);
        String pIP = sc.nextLine();
        System.out.println();

        System.out.println("NAT-box Intialized");
        System.out.println();

        ServerSocket serverSocket = new ServerSocket(port);
        NatBox box = new NatBox(serverSocket, pIP);
        box.start();
    }
}
