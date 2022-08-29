import java.io.Serializable;

public class Message implements Serializable {
    private String text, from, to;

    public Message(String text, String from) { 
        this.text = text;
        this.from = from;
        this.to = null;
    }

    public Message(String text, String from, String to) {
        this.text = text;
        this.from = from;
        this.to = to;
    }

    public String text() { return text; }

    public String from() { return from; }

    public String to() { return to; }

}

