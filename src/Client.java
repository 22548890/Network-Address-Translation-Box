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
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private boolean internal;

    public Client(Socket socket, boolean internal) {
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
            System.exit(0);
        }
        this.internal = internal; 
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
        for (int i = 0; i < 3; i++) {
            ip += "." + r.nextInt(256);
        }
        return ip;
    }
    
    public static void main(String[] args) {
        int natPort = 1234;

        System.out.println("Internal client (true/false): ");
        Scanner scanner = new Scanner(System.in);
        boolean internal = Boolean.parseBoolean(scanner.nextLine());

        System.out.println("NAT-box IP: ");
        String natIP = scanner.nextLine();
        scanner.close();
        
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

        Client client = new Client(socket, internal);
    }
}
