import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    public static final int ECHO_REPLY = 0;
    public static final int ECHO_REQUEST = 8;
    public static final int DHCP_REPLY = 1;
    public static final int DHCP_REQUEST = 2;
    public static final int ARP_REPLY = 3;
    public static final int ARP_REQUEST = 4;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private NatBox natbox;
    private boolean internal;
    private String clientIP = null;
    private String clientMAC;
    private int clientPort;
    private int natPort = 0;
    
    /** 
     * Constructor for this handler
     * @param socket Is the socket that the client connects to
     */
    public ClientHandler(Socket socket, NatBox natbox) {
        try {
            this.socket = socket;
            this.ous = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.natbox = natbox;
            this.clientPort = socket.getPort();
        } catch (IOException e){
            closeEverything();
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                Paquet paquet = (Paquet) ois.readObject();
                handlePaquet(paquet);
            } catch (IOException e) {
                closeEverything();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void tcpSendToThisClient(Paquet paquet) { 
        try {
            ous.writeObject(paquet);
            ous.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInternal() { return internal; }

    public String getClientIP() { return clientIP; }

    public String getClientMAC() { return clientMAC; }

    public int getClientPort() { return clientPort; }

    private void handlePaquet(Paquet p) {
        int type = p.getType();
        switch (type) {
            case ECHO_REPLY:
                // nothing
                break;
            case ECHO_REQUEST:
                forwardPaquet(p);
                break;
            case DHCP_REPLY:
                // nothing
                break;

            case DHCP_REQUEST:
                dhcp(p);
                if (internal) System.out.println("Internal Client Connected");
                else System.out.println("External Client Connected");
                System.out.println("Client MAC: " + clientMAC);
                System.out.println("Client IP: " + clientIP);
                System.out.println("Client Port: " + clientPort);
                if (natPort != 0) {
                    System.out.println("NAT-box Port: " + natPort);
                } 
                System.out.println();
                break;

            case ARP_REPLY:
                // nothing
                break;

            case ARP_REQUEST:
                arp(p);
                break;
            default:
                System.out.println("ERROR: Invalid Paquet Type ");
                System.exit(0);
        }
    }

    private void forwardPaquet(Paquet p) {
        if (internal && natbox.isIPInternal(p.getDestinationIP())) {         // internal -> internal
            natbox.tcpSend(p);
        } else if (internal && natbox.isIPExternal(p.getDestinationIP())) {  // internal -> external

        } else if (!internal && natbox.isIPInternal(p.getDestinationIP())) { // external -> internal

        } else if (!internal && natbox.isIPExternal(p.getDestinationIP())) { // external -> external
            
        } else {
            // TODO: Destination client not connected to NAT
            System.out.println("ERROR: Destination does not exist");
        }
    }

    private void dhcp(Paquet p) { 
        clientMAC = p.getSourceMAC();
        clientIP = p.getSourceIP();
        clientPort = p.getSourcePort();        
        internal = false;
        if (p.getText().equals("internal")) internal = true;
        if (internal) {
            clientIP = natbox.popIPfromPool();
            natPort = natbox.getAivalablePort();
            addToTable();
        } 
        Paquet paquet = new Paquet(natbox.getMAC(), null, null, clientIP, natPort, 0, DHCP_REPLY, null);
        try {
            ous.writeObject(paquet);
            ous.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void arp(Paquet p) {
        String sourceMac = p.getSourceMAC();
        String sourceIP = p.getSourceIP();
        String destIP = p.getDestinationIP();
        int sourcePort = p.getSourcePort();
        String text = p.getText();
        String destMac = natbox.getClientMACFromIP(destIP);
        int destPort = natbox.getClientPortFromIP(destIP);

        Paquet paquet = new Paquet(sourceMac, destMac, sourceIP, destIP, sourcePort, destPort, ARP_REPLY, text);
        try {
            ous.writeObject(paquet);
            ous.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToTable() { 
        TableRow row = new TableRow(clientIP, socket.getPort(), natbox.getIP(), natPort);
        natbox.addRow(row);
    }

    private void handleCommand(String cmd) {
        switch (cmd) {
            case "/exit":
                break;
        }
    }

    private void closeEverything() {
        natbox.removeClientHandler(this);
        if (internal) {
            natbox.addIPtoPool(clientIP);
        }
        try {
            if (ois != null) {
                ois.close();
            }
        } catch (IOException e) {}
        try {
            if (ous != null) {
                ous.close();
            }
        } catch (IOException e) {}
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {}
    }
}
