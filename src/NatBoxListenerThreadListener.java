import java.util.Scanner;

public class NatBoxListenerThreadListener implements Runnable {
    private NatBox box;

    public NatBoxListenerThreadListener(NatBox box) {
        this.box = box;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String command = sc.nextLine();
            if (command.equals("table")) {
                box.printTable();
            } else if (command.equals("exit")) {
                box.closeServerSocket();
                sc.close();
                System.exit(0);
            } else {
                System.out.println("Unknown command");
                System.out.println("Available commands: table, exit");
            }
        }

    }

}