
import java.io.Serializable;

public class Paquet implements Serializable {

    // Ethernet frame important elements
    private String destinationMAC;
    private String sourceMAC;

    // IP packet/segment important elements
    private String sourceIP;
    private String destinationIP;
    private int sourcePort;
    private int destinationPort;

    // payload
    private String text;

    // extra
    private boolean isCommand;

    public Paquet(String destinationMAC, String sourceMAC, String sourceIP, String destinationIP, String text) {
        this.destinationMAC = destinationMAC;
        this.sourceMAC = sourceMAC;
        this.destinationIP = destinationIP;
        this.sourceIP = sourceIP;
        this.text = text;
        this.isCommand = false;
    }

    public Paquet(String text) {
        this.text = text;
        this.isCommand = true;
    }

    public String getDestinationMAC() {
        return destinationMAC;
    }

    public String getSourceMAC() {
        return sourceMAC;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public String getText() {
        return text;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public void setSourceIP(String assignedIP) {
        this.sourceIP = assignedIP;
    }

    public void setSourceMAC(String mac) {
        this.sourceMAC = mac;
    }

    public void setDestinationMAC(String destinationMAC) {
        this.destinationMAC = destinationMAC;
    }

    public void setDestinationIP(String destinationIP) {
        this.destinationIP = destinationIP;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCommand(boolean command) {
        isCommand = command;
    }
}
