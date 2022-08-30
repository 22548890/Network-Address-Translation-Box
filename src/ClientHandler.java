import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private NatBox natbox;
    private boolean internal = false;
    private String assignedIP = null;
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
        } catch (IOException e){
            closeEverything();
        }
    }

    @Override
    public void run() {
        dchp();
        shareInfo();
        if (internal) {
            addToTable();
        }

        Paquet paquet = null;
        while (socket.isConnected()) {
            try {
                paquet = (Paquet) ois.readObject();
                if (paquet.isCommand()) {
                    handleCommand(paquet.getText());
                }
            } catch (IOException e) {
                closeEverything();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void dchp() { 
        boolean internal = false;
        try {
            internal = Boolean.parseBoolean((String) ois.readObject());
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Error with dchp request");
            closeEverything();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR: Error with dchp request");
            closeEverything();
            System.exit(0);
        }

        this.internal = internal;

        try {
            if (internal) {
                assignedIP = natbox.popIPfromPool();
                ous.writeObject(assignedIP);
                ous.flush();

                natPort = natbox.getAivalablePort();
            }
        } catch (IOException e) {
            System.out.println("ERROR: Error with popping IP from pool");
            closeEverything();
            System.exit(0);
        }
        
    }

    private void addToTable() { 
        TableRow row = new TableRow(assignedIP, socket.getPort(), natbox.getIP(), natPort);
        natbox.addRow(row);
    }

    private void shareInfo() {
        try {
            String clientIP = (String) ois.readObject();
            String clientMAC = (String) ois.readObject();
            natbox.arpPut(clientIP, clientMAC);

            ous.writeObject(natbox.getMAC());
            ous.flush();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            natbox.addIPtoPool(assignedIP);
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
