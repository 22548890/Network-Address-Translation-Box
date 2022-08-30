import java.io.*;
import java.net.*;
import java.util.*;

public class ClientListenerThread implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;

    public ClientListenerThread(Socket socket, ObjectInputStream ois, ObjectOutputStream ous) {
        this.socket = socket;
        this.ois = ois;
        this.ous = ous;
    }

    @Override
    public void run() {
        // TODO listen for paquets
        while (socket.isConnected()) {
            try {
                Paquet paquet = (Paquet) ois.readObject();
                System.out.println("[" + paquet.getSourceIP() + "]: " + paquet.getText());
            } catch (IOException e) {
                closeEverything();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

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
