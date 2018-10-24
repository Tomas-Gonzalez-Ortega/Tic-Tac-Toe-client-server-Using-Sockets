// import statements
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.stream.*;
import java.util.Arrays;

/**
* TicTacToe game - Server side class
* @author Po Chang Chiu
* @author Tomas Gonzalez Ortega
* @version 2017-07-30 11:34am
* @see <a href="https://d2l.langara.bc.ca/d2l/lms/dropbox/user/folder_submit_files.d2l?db=51872&grpid=95747&isprv=0&bp=0&ou=88736">a11: Sockets - Tic-Tak-Toe</a>
*/
public class TTTServer extends JFrame implements Runnable {
    /**
    * Connection instance variables
    */
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket socket;

    /**
    * GUI instance variables
    */
    private JPanel buttonLayout;
    private final JButton close = new JButton("Close");
    private final JButton newGame = new JButton("New Game");
    private int round = 1;
    private final JTextField rounds = new JTextField("Rounds " + round);
    private final JButton[] buttons = {
        new JButton(" "), new JButton(" "), new JButton(" "),
        new JButton(" "), new JButton(" "), new JButton(" "),
        new JButton(" "), new JButton(" "), new JButton(" ")};
    private JPanel textLayout;
    private JTextField scoreBoard;
    private JTextField announcement;

    /**
    * Game instance variables
    */
    private Boolean myTurn;
    private int[][] wins = new int[][] {
        { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 },
        { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };
    private int serverScore = 0;
    private int playerScore = 0;
    private Boolean connected;
    private Boolean roundOver = false;

    /**
	* Contructs the server side Tic-Tac-Toe game layout
	*/
    public TTTServer() {
        super("TicTacToe Server");
        myTurn = false;
        connected = false;

        setTexts();
        setButtons();
        setClickable(false);

        add(textLayout, BorderLayout.NORTH);
        add(buttonLayout, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setSize(340, 400);
        setVisible(true);
    }

    /**
    * Initializes all the text fields
    */
    private void setTexts() {
        textLayout = new JPanel();
        textLayout.setLayout(new GridLayout(2,1));
        scoreBoard = new JTextField("Server : " + serverScore + " - Player: " + playerScore);
        scoreBoard.setEditable(false);
        announcement = new JTextField("Announcement : ");
        announcement.setEditable(false);
        textLayout.add(scoreBoard);
        textLayout.add(announcement);
    }

    /**
    * Sets up buttons function
    */
    private void setButtons() {
        buttonLayout = new JPanel();
        buttonLayout.setLayout(new GridLayout(4,3));
        for (JButton b : buttons) {
            buttonLayout.add(b);
        }
        buttonLayout.add(close);
        buttonLayout.add(newGame);
        rounds.setEditable(false);
        buttonLayout.add(rounds);

        close.addActionListener(a -> {
            myTurn = true;
            sendButtonMessage("END");
            System.exit(0);
        });
        newGame.addActionListener(a -> {
            myTurn = true;
            resetBoth();
        });
        buttons[0].addActionListener(a -> {
            if (myTurn) {
                buttons[0].setText("O");
                buttons[0].setEnabled(false);
                sendButtonMessage("0");
                myTurn = false;
            }
        });
        buttons[1].addActionListener(a -> {
            if (myTurn) {
                buttons[1].setText("O");
                buttons[1].setEnabled(false);
                sendButtonMessage("1");
                myTurn = false;
            }
        });
        buttons[2].addActionListener(a -> {
            if (myTurn) {
                buttons[2].setText("O");
                buttons[2].setEnabled(false);
                sendButtonMessage("2");
                myTurn = false;
            }
        });
        buttons[3].addActionListener(a -> {
            if (myTurn) {
                buttons[3].setText("O");
                buttons[3].setEnabled(false);
                sendButtonMessage("3");
                myTurn = false;
            }
        });
        buttons[4].addActionListener(a -> {
            if (myTurn) {
                buttons[4].setText("O");
                buttons[4].setEnabled(false);
                sendButtonMessage("4");
                myTurn = false;
            }
        });
        buttons[5].addActionListener(a -> {
            if (myTurn) {
                buttons[5].setText("O");
                buttons[5].setEnabled(false);
                sendButtonMessage("5");
                myTurn = false;
            }
        });
        buttons[6].addActionListener(a -> {
            if (myTurn) {
                buttons[6].setText("O");
                buttons[6].setEnabled(false);
                sendButtonMessage("6");
                myTurn = false;
            }
        });
        buttons[7].addActionListener(a -> {
            if (myTurn) {
                buttons[7].setText("O");
                buttons[7].setEnabled(false);
                sendButtonMessage("7");
                myTurn = false;
            }
        });
        buttons[8].addActionListener(a -> {
            if (myTurn) {
                buttons[8].setText("O");
                buttons[8].setEnabled(false);
                sendButtonMessage("8");
                myTurn = false;
            }
        });
    }

    /**
    * Changes buttons to clickable or nonclickable, newGame and close are unaffected
    * @param yn Boolean argument that allows the state change
    */
    private void setClickable(Boolean yn) {
      for (JButton b : buttons) {
          b.setEnabled(yn);
      }
    }

    /**
    * Changes button from O to X
    * @param location String argument that indicates the button clicked
    */
    private void changeOToX(String location) {
        buttons[Integer.parseInt(location)].setText("X");
        buttons[Integer.parseInt(location)].setEnabled(false);
    }

    /**
    * Resets all the button's name back to blank
    */
    private void resetButtons() {
      for (JButton b : buttons) {
          b.setText(" ");
      }
    }

    /**
    * Checks if all the buttons are disabled, have names, and if they're connected
    * @return true if all 3 conditions are satisfied
    * @return false if at least 1 doesn't
    */
    private Boolean noMoreButtons() {
        if(!Arrays.stream(buttons).anyMatch(b -> b.isEnabled() == true) && connected == true &&
            !Arrays.stream(buttons).anyMatch(b -> b.getText().equals(" "))) {
            return true;
        } else {
            return false;
        }
    }

    /**
    * Gives the server the start up
    */
    public void startRunning() {
        try {
            server = new ServerSocket(6789, 5);
            while (true) {
                try {
                    socket = server.accept();
                    output = new ObjectOutputStream(socket.getOutputStream());
                    output.flush();
                    input = new ObjectInputStream(socket.getInputStream());
                    whileConnected();
                } catch (EOFException eOf) {
                    System.out.println("Server ended the socket!");
                } finally {
                    closeServer();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Listens for the player's actions and updates boarder while connected to the player
    * @throws IOException if message cannot be written to or closed
    */
    private void whileConnected() throws IOException {
        String message = "";
        setClickable(true);
        connected = true;
        do {
            try {
                message = (String) input.readObject();
                if (isInteger(message)) {
                    changeOToX(message);
                    myTurn = true;
                } else if (message.equals("reset")) {
                    resetBoard();
                } else if (message.equals("Wins!")) {
                    updateScore("Player");
                    setClickable(false);
                }
                if(noMoreButtons() && roundOver == false) {
                    updateScore("Draw");
                    sendMessage("Draw");
                }
            } catch(ClassNotFoundException e) {
                System.out.println("Unable to read message");
            }
        } while(!message.equals("END"));
        setClickable(false);
        resetButtons();
    }

    /**
    * Sends the button message to the player if it is his turn
    * @param message String argument that indicates the button clicked
    */
    private void sendButtonMessage(String message) {
        if (myTurn) {
            try {
                output.writeObject(message);
                output.flush();
            } catch(IOException e) {
                System.out.println("ERROR: can't send message, no player found");
            }
        }
    }

    /**
    * Sends message to player regardless who's turn
    * @param message String argument that indicates the messaged sent
    */
    private void sendMessage(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch(IOException e) {
            System.out.println("ERROR: can't send message, no player found");
        }
    }

    /**
    * Closes streams and sockets
    */
    private void closeServer() {
        try {
            output.close();
            input.close();
            socket.close();
            round = 1;
            rounds.setText("Round " + round);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Checks if the string is an integer
    * @param in String argument to be casted into an integer
    * @return true if the String has been able to be parsed into an integer
    * @return false if not
    */
    private boolean isInteger(String in) {
        try {
            Integer.parseInt(in);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
    * Updates serverScore, playerScore, and announcement when a player wins
    * @param winner String argument that carried the winner identity
    */
    private void updateScore(String winner) {
        if (winner.equals("Draw")) {
            announcement.setText("Announcement : Draw");
        } else {
            if (winner.equals("Player")) {
                scoreBoard.setText("Server : " + serverScore + " - Player: " + ++playerScore);
            } else if (winner.equals("Server")) {
                scoreBoard.setText("Server : " + ++serverScore + " - Player: " + playerScore);
            }
            announcement.setText("Announcement : " + winner + " Won!");
        }
    }

    /**
    * Changes all buttons back to clickable without names, updates round, resets announcement, and sends for reset message to player for acknowledgement
    */
    private void resetBoth() {
        resetButtons();
        setClickable(true);
        rounds.setText("Round " + ++round);
        announcement.setText("Announcement : ");
        sendMessage("reset");
        roundOver = false;
        myTurn = true;
    }

    /**
    * Resets only this board
    */
    private void resetBoard() {
        resetButtons();
        setClickable(true);
        rounds.setText("Round " + ++round);
        announcement.setText("Announcement : ");
        roundOver = false;
        myTurn = true;
    }

    /**
    * Calls determineWinner method and only stops if the round is over whereas is a tie or has a winner
    */
    public void run() {
        while(true) {
            if(!roundOver) {
                determineWinner();
            }
        }
    }

    /**
    * Checks if server wins or lose
    */
    private void determineWinner() {
        for (int i = 0; i < 8; i++) {
            if(buttons[wins[i][0]].getText().equals("O")
            && buttons[wins[i][1]].getText().equals("O")
            && buttons[wins[i][2]].getText().equals("O")) {
                sendMessage("Wins!");
                updateScore("Server");
                setClickable(false);
                roundOver = true;
            } else if(buttons[wins[i][0]].getText().equals("X")
            && buttons[wins[i][1]].getText().equals("X")
            && buttons[wins[i][2]].getText().equals("X")) {
                sendMessage("Lost!");
                updateScore("Player");
                setClickable(false);
                roundOver = true;
            }
        }
    }
}
