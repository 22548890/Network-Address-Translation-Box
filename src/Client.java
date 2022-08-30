import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    public static final int ECHO_REPLY = 0;
    public static final int ECHO_REQUEST = 8;
    public static final int DHCP_REPLY = 1;
    public static final int DHCP_REQUEST = 2;
    public static final int ARP_REPLY = 3;
    public static final int ARP_REQUEST = 4;

    private String personalMAC;
    private String personalIP;
    private String ip;
    private String natIP;
    private String natMAC = null;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private boolean internal;

    public Client(Socket socket, boolean internal, String natIP) {
        this.personalMAC = randomMAC();
        if (internal) {
            this.personalIP = randomInternalIP();
        } else {
            this.personalIP = randomExternalIP();
        }
        this.ip = personalIP;
        this.socket = socket;
        try {
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.ous = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("ERROR: creating socket streams");
            closeEverything();
            System.exit(0);
        }
        this.internal = internal;
        this.natIP = natIP;
    }

    public void start() {
        while (socket.isConnected()) {
            try {
                Scanner sc = new Scanner(System.in);
                String text = sc.nextLine();
                handleCommand(text);
            } catch (Exception e) {
                // System.out.println("ERROR: Reading object");
                closeEverything();
                // e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private void handleCommand(String cmd) throws IOException, ClassNotFoundException {
        Paquet paquet = null;
        Scanner sc = null;
        switch (cmd) {
            case "/exit":
                closeEverything();
                System.exit(0);
                break;

            case "/send":
                sc = new Scanner(System.in);
                System.out.print("Enter destination IP: ");
                String destIP = sc.nextLine();
                System.out.print("Enter destination Port: ");
                int destPort = sc.nextInt();
                System.out.print("Enter message: ");
                String msg = sc.nextLine();
                System.out.println();
                paquet = new Paquet(personalMAC, null, ip, destIP, Integer.parseInt(getLocalPort()), destPort,
                        ECHO_REQUEST, msg);// send request
                ous.writeObject(paquet);
                ous.flush();

            case "/ping":
                sc = new Scanner(System.in);
                System.out.print("IP to send to: ");
                String ipTST = sc.nextLine();
                int portTST = 0;
                if (!internal) {
                    System.out.print("Port to send to: ");
                    portTST = Integer.parseInt(sc.nextLine());
                }
                System.out.print("Ping message: ");
                String text = sc.nextLine();
                System.out.println();

                // get corresponding mac and port of destination
                paquet = new Paquet(personalMAC, null, ip, ipTST, socket.getLocalPort(), portTST, ARP_REQUEST, text);
                ous.writeObject(paquet);
                ous.flush();
                // String macTST = (String) ois.readObject();

                // paquet = new Paquet(personalMAC, macTST, ip, ipTST, socket.getLocalPort(),
                // portTST, ECHO_REQUEST, text);
                // ous.writeObject(paquet);
                // ous.flush();
                break;

            case "/whoami":
                System.out.println("--------------------------------");
                System.out.println("PERSONAL MAC: " + personalMAC);
                System.out.println("PERSONAL IP: " + personalIP);
                String type;
                if (internal)
                    type = "INTERNAL";
                else
                    type = "EXTERNAL";
                System.out.println("TYPE: " + type);
                System.out.println("LOCAL IP: " + getIP());
                System.out.println("PORT: " + getLocalPort());
                System.out.println("--------------------------------");
                System.out.println();
                break;

            case "/help":
                System.out.println("Commands:");
                System.out.println("/exit: close the client");
                System.out.println("/ping: send a ping to a client");
                System.out.println("/whoami: show your info");
                System.out.println("/send: send a message to a client");
                System.out.println("");
                break;
            default:
                System.out.println("Type '/help' to view possible commands");
                System.out.println();
                return;
        }
    }

    private String getLocalPort() {
        return String.valueOf(socket.getLocalPort());
    }

    public void dhcpRequest() {
        try {
            // send request
            String text = "external";
            if (internal)
                text = "internal";
            Paquet paquet = new Paquet(personalMAC, null, personalIP, null, socket.getLocalPort(), 0, DHCP_REQUEST,
                    text);
            ous.writeObject(paquet);
            ous.flush();
        } catch (Exception e) {
            System.out.println("ERROR: With dchp Request");
            closeEverything();
            System.exit(0);
        }
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public void setNatMAC(String natMAC) {
        this.natMAC = natMAC;
    }

    public String getPersonalMAC() {
        return personalMAC;
    }

    public String getPersonalIP() {
        return personalIP;
    }

    public String getIP() {
        return ip;
    }

    public String getNatMAC() {
        return natMAC;
    }

    public String getNatIP() {
        return natIP;
    }

    public boolean isInternal() {
        return internal;
    }

    public void listenForPaquet() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(socket, ois, ous, this);
        Thread thread = new Thread(clientListenerThread);
        thread.start();
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

    private String randomInternalIP() {
        Random r = new Random();
        String ip = "10";
        for (int i = 0; i < 3; i++) {
            ip += "." + r.nextInt(256);
        }
        return ip;
    }

    private String randomExternalIP() {
        Random r = new Random();
        String ip = r.nextInt(256) + "";
        if (ip == "10")
            ip = "11";
        for (int i = 0; i < 3; i++) {
            ip += "." + r.nextInt(256);
        }
        return ip;
    }

    /**
     * Closes socket and streams neatly
     */
    public void closeEverything() {
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

    public static void main(String[] args) throws IOException {
        int natPort = 1234;

        System.out.print("Internal client (true/false): ");
        Scanner sc = new Scanner(System.in);
        boolean internal = Boolean.parseBoolean(sc.nextLine());
        System.out.println();

        System.out.println("--------------------------------------------------------");
        System.out.println("NAT-BOX INFO:");
        System.out.println("--------------------------------------------------------");
        System.out.print("NAT-Box IP: ");
        String natIP = sc.nextLine();

        Socket socket = null;
        try {
            socket = new Socket(natIP, natPort);
            socket.setSoTimeout(5000);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Unknown host");
            System.exit(0);
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR: Connection timed out...");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR: Couldn't get the connection to " + natIP);
            System.exit(0);
        }

        Client client = new Client(socket, internal, natIP);
        client.dhcpRequest();
        client.listenForPaquet();
        client.start();
        client.closeEverything();

    }
}