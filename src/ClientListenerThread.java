import java.io.*;
import java.net.*;

public class ClientListenerThread implements Runnable {

    public static final int ECHO_REPLY = 0;
    public static final int ECHO_REQUEST = 8;
    public static final int DHCP_REPLY = 1;
    public static final int DHCP_REQUEST = 2;
    public static final int ARP_REPLY = 3;
    public static final int ARP_REQUEST = 4;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private Client client;

    public ClientListenerThread(Socket socket, ObjectInputStream ois, ObjectOutputStream ous, Client client) {
        this.socket = socket;
        this.ois = ois;
        this.ous = ous;
        this.client = client;
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

    private void handlePaquet(Paquet p) {
        int type = p.getType();
        switch (type) {
            case ECHO_REPLY:
                break;
            case ECHO_REQUEST:
                System.out.println("[" + p.getSourceIP() + "]: " + p.getText());
                System.out.println();
                printPaquet(p);
                break;
            case DHCP_REPLY:
                client.setNatMAC(p.getSourceMAC());
                client.setIP(p.getDestinationIP());

                System.out.println("NAT-box MAC: " + client.getNatMAC());
                System.out.println();
                System.out.println("Personal MAC: " + client.getPersonalMAC());
                System.out.println("Personal IP: " + client.getPersonalIP());
                if (client.isInternal()) System.out.println("Local IP: " + client.getIP());
                System.out.println("Port: " + socket.getLocalPort());
                System.out.println();

                break;
            case DHCP_REQUEST:
                // nothing
                break;
            case ARP_REPLY:
                p.setType(ECHO_REQUEST);
                try {
                    ous.writeObject(p);
                    ous.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ARP_REQUEST:
                // nothing
                break;
            default:
                System.out.println("ERROR: Invalid Paquet Type ");
                System.exit(0);
        }
    }

    private void printPaquet(Paquet p) {
        System.out.println("-------------------------------------");
        System.out.println("         Paquet Details");
        System.out.println("-------------------------------------");
        System.out.println("Paquet Type: " + p.getType());
        System.out.println("Source MAC : " + p.getSourceMAC());
        System.out.println("Source IP  : " + p.getSourceIP());
        System.out.println("Source Port: " + p.getSourcePort());
        System.out.println("Dest MAC   : " + p.getDestinationMAC());
        System.out.println("Dest IP    : " + p.getDestinationIP());
        System.out.println("Dest Port  : " + p.getDestinationPort());
        System.out.println("Text       : " + p.getText());
        System.out.println("-------------------------------------");
        System.out.println();
    }

    private void closeEverything() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (ous != null) {
                ous.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
