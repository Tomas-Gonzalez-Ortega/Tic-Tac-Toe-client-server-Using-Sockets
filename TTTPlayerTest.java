// import statements
import javax.swing.JFrame;

/**
* The Client Tic-Tac-Toe main method
*/
public class TTTPlayerTest {
    /**
    * Creates and runs a new client
    * @param args line arguments -- ignored in this assignment
    */
    public static void main(String[] args) {
        TTTPlayer paul;
        paul = new TTTPlayer("127.0.0.1");
        paul.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        paul.startRunning();
    }
}
