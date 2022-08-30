import java.io.IOException;
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
    // private HashMap<String, String> arp = new HashMap<String, String>();

    public NatBox(ServerSocket serverSocket, String ip) {
        this.serverSocket = serverSocket;
        this.ip = ip;
        this.mac = randomMAC();
        // arp.put(ip, mac);
        for (int i = 1; i <= poolSize; i++) {
            pool.add("10.0.0." + i);
        }
    }

    public void start() {
        System.out.println("NAT-box IP: " + ip);
        System.out.println("NAT-box MAC: " + mac);
        System.out.println();
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandlers.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void tcpSend(Paquet p) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getClientIP().equals(p.getDestinationIP())) {
                handler.tcpSendToThisClient(p);
            }
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

    // public String arpGet(String ip) { return arp.get(ip); }

    // public void arpPut(String ip, String mac) {
    //     arp.put(ip, mac);
    // }

    public String getIP() { return ip; }

    public String getMAC() { return mac; }

    public int getAivalablePort() { return ++aivalablePort; }

    public String getClientMACFromIP(String ip) { 
        for (ClientHandler handler : clientHandlers) {
            if (handler.getClientIP().equals(ip)) {
                return handler.getClientMAC();
            }
        }
        return null;
    }

    public int getClientPortFromIP(String ip) { 
        for (ClientHandler handler : clientHandlers) {
            if (handler.getClientIP().equals(ip)) {
                return handler.getClientPort();
            }
        }
        return 0;
    }

    public void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client Disconnected");
        System.out.println("Client MAC: " + clientHandler.getClientMAC());
        System.out.println("Client IP: " + clientHandler.getClientIP());
        System.out.println();
    }

    public boolean clientHandlersContain(ClientHandler clientHandler) {
        if (clientHandlers.contains(clientHandler)) return true;
        else return false;
    }

    public boolean isIPInternal(String ip) { 
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getClientIP().equals(ip) && clientHandler.isInternal()) {
                return true;
            }
        }
        return false;
    }

    public boolean isIPExternal(String ip) { 
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getClientIP().equals(ip) && !clientHandler.isInternal()) {
                return true;
            }
        }
        return false;
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
        int port = 1234;

        System.out.print("Public IP address: ");
        Scanner sc = new Scanner(System.in);
        String pIP = sc.nextLine();
        System.out.println();

        ServerSocket serverSocket = new ServerSocket(port);
        NatBox box = new NatBox(serverSocket, pIP);
        box.start();
    }
}
