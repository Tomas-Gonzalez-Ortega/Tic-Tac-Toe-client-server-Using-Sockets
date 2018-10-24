// import statements
import javax.swing.JFrame;

/**
* The Server Tic-Tac-Toe main method
*/
public class TTTServerTest {
    /**
    * Creates a new server and a new thread, then it put both to run
    * @param args line arguments -- ignored in this assignment
    */
    public static void main(String[] args) {
        TTTServer server;
        server = new TTTServer();
        Thread t = new Thread(server);
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        t.start();
        server.startRunning();
    }
}
