import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class Client {

    private String mac;
    private String ip;
    private String assignedIP = null;
    private String natIP;
    private String natMAC = null;
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

    public void sendPaquet() {
        Paquet paquet = null;
        while (socket.isConnected()) {
            try {
                System.out.print("Type message: ");
                Scanner sc = new Scanner(System.in);
                String text = sc.nextLine();
                if (text.equals("/exit")) {
                    closeEverything();
                    System.exit(0);
                } else if (text.charAt(0) == '/') {
                    paquet = new Paquet(text);
                } else {
                    System.out.print("IP to send to: ");
                    System.out.println();
                    String ipTST = sc.nextLine();
                    // continue here
                }
                ous.writeObject(paquet);
                ous.flush();
            } catch (Exception e) {
                System.out.println("ERROR: Reading object");
                closeEverything();
                e.printStackTrace();
                System.exit(0);
            }
        }
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

    public boolean isInternal() { return internal; }

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
        if (ip == "10") ip = "11";
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
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {
        int natPort = 2345;

        System.out.print("Internal client (true/false): ");
        Scanner sc = new Scanner(System.in);
        boolean internal = Boolean.parseBoolean(sc.nextLine());
        System.out.println();

        System.out.print("NAT-box IP: ");
        String natIP = sc.nextLine();
        System.out.println();
        
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
        client.sendPaquet();


        client.closeEverything();
        
    }
}