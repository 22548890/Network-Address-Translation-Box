import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.plaf.synth.SynthSpinnerUI;

public class Client {

    private String mac;
    private String ip;
    private String assignedIP = null;
    private String natIP;
    static String natMAC;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private boolean internal;

    public Client(Socket socket, boolean internal, String natIP) {
        this.mac = randomMAC();
        if (internal) {
            this.ip = randomInternalIP();
        } else {
            this.ip = randomExternalIP();
        }
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

    public static String getNatMAC(InetAddress NATip) {
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
        } catch (UnknownHostException e) {
            return "ERROR";
        }
    }

    public void instruction() {
        Paquet paquet = null;
        // read from console
        Scanner scanner = new Scanner(System.in);

        while (socket.isConnected()) {
            try {
                String in = scanner.nextLine();
                if (in.equals("send")) {
                    System.out.print("Type message: ");
                    Scanner sc = new Scanner(System.in);
                    String text = sc.nextLine();
                    System.out.print("IP to send to: ");
                    System.out.println();
                    String ipTST = sc.nextLine();
                    // Paquet(String destinationMAC, String sourceMAC, String sourceIP, String
                    // destinationIP, String text)
                    // continue here
                    paquet = new Paquet(natMAC, mac, ip, ipTST, text);

                    ous.writeObject(paquet);
                    ous.flush();
                } else if (in.equals("/exit")) {
                    closeEverything();
                    System.exit(0);
                } else if (in.equals("ping")) {

                } else if (in.equals("list")) {
                    // shows list of connected users

                } else if (in.equals("whoami")) {
                    System.out.println("Assigned IP: " + assignedIP + "\n" + "IP: " + ip + "\n" + "MAC: " + mac);

                } else if (in.equals("help")) {
                    System.out.println("send - send a message to another user");
                    System.out.println("ping - ping a user");
                    System.out.println("list - list all connected users");
                    System.out.println("whoami - show your IP and MAC");
                    System.out.println("/exit - exit the program");
                } else {
                    System.out.println("Invalid command");
                }

            }

            catch (Exception e) {
                System.out.println("Shutting down");
                closeEverything();
                e.printStackTrace();
                System.exit(0);
            }
            // receive paquet
            // try {
            // paquet = (Paquet) ois.readObject();
            // System.out.println("Received: " + paquet.getText() + " from " +
            // paquet.getSourceIP());
            // } catch (Exception e) {
            // System.out.println("ERROR: Reading object");
            // closeEverything();
            // e.printStackTrace();
            // System.exit(0);
            // }
        }

    }

    public void listenForPaquet() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(socket, ois, ous);
        Thread thread = new Thread(clientListenerThread);
        thread.start(); // waiting for broadcasted msgs
    }

    public void shareInfo() {
        try {
            ous.writeObject(ip);
            ous.flush();
            ous.writeObject(mac);
            ous.flush();

            natMAC = (String) ois.readObject();

        } catch (IOException e) {
            System.out.println("ERROR: With sharing IPs and MACs");
            closeEverything();
            System.exit(0);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void dhcpRequest() {
        try {
            // sending mac address
            if (internal) { // request IP address from pool
                ous.writeObject("true");
                ous.flush();

                assignedIP = (String) ois.readObject();
                System.out.println("IP assigned from pool: " + assignedIP);
                System.out.println();
            } else {
                ous.writeObject("false");
                ous.flush();
            }
        } catch (Exception e) {
            System.out.println("ERROR: With dchp Request");
            closeEverything();
            System.exit(0);
        }
    }

    public boolean isInternal() {
        return internal;
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
        int natPort = 2345;

        // System.out.print("Internal client (true/false): ");
        // Scanner sc = new Scanner(System.in);
        // boolean internal = Boolean.parseBoolean(sc.nextLine());
        boolean internal = true;
        // System.out.println();

        // System.out.print("NAT-box IP: ");
        String natIP = "0.0.0.0";// sc.nextLine();
        // System.out.println();

        Socket socket = null;
        try {
            socket = new Socket(natIP, natPort);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Unknown host");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR: Connecting socket");
            System.exit(0);
        }
        Client client = new Client(socket, internal, natIP);

        client.dhcpRequest();
        client.shareInfo();
        System.out.println("NAT-box MAC: " + natMAC);

        client.listenForPaquet();
        // wait for system.in
        client.instruction();

        client.closeEverything();

    }
}