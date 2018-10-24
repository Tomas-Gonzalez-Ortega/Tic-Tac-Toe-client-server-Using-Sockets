// import statements
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.stream.*;
import java.util.Arrays;

/**
* TicTacToe game - Client side class
* @author Po Chang Chiu
* @author Tomas Gonzalez Ortega
* @version 2017-07-30 11:34am
* @see <a href="https://d2l.langara.bc.ca/d2l/lms/dropbox/user/folder_submit_files.d2l?db=51872&grpid=95747&isprv=0&bp=0&ou=88736">a11: Sockets - Tic-Tak-Toe</a>
*/
public class TTTPlayer extends JFrame {
    /**
    * Connection instance variables
    */
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String serverIP;
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
    private int[][] wins = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };
    private int serverScore = 0;
    private int playerScore = 0;
    private Boolean connected;
    private Boolean roundOver = false;

    /**
    * Contructs the client side Tic-Tac-Toe game layout
    */
    public TTTPlayer(String host) {
        super("TicTacToe Player");
        serverIP = host;
        myTurn = true;
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
            sendMessage("END");
            System.exit(0);
        });
        newGame.addActionListener(a -> {
            myTurn = true;
            resetBoth();
        });
        buttons[0].addActionListener(a -> {
            if (myTurn) {
                buttons[0].setText("X");
                buttons[0].setEnabled(false);
                sendMessage("0");
                myTurn = false;
            }
        });
        buttons[1].addActionListener(a -> {
            if (myTurn) {
                buttons[1].setText("X");
                buttons[1].setEnabled(false);
                sendMessage("1");
                myTurn = false;
            }
        });
        buttons[2].addActionListener(a -> {
            if (myTurn) {
                buttons[2].setText("X");
                buttons[2].setEnabled(false);
                sendMessage("2");
                myTurn = false;
            }
        });
        buttons[3].addActionListener(a -> {
            if (myTurn) {
                buttons[3].setText("X");
                buttons[3].setEnabled(false);
                sendMessage("3");
                myTurn = false;
            }
        });
        buttons[4].addActionListener(a -> {
            if (myTurn) {
                buttons[4].setText("X");
                buttons[4].setEnabled(false);
                sendMessage("4");
                myTurn = false;
            }
        });
        buttons[5].addActionListener(a -> {
            if (myTurn) {
                buttons[5].setText("X");
                buttons[5].setEnabled(false);
                sendMessage("5");
                myTurn = false;
            }
        });
        buttons[6].addActionListener(a -> {
            if (myTurn) {
                buttons[6].setText("X");
                buttons[6].setEnabled(false);
                sendMessage("6");
                myTurn = false;
            }
        });
        buttons[7].addActionListener(a -> {
            if (myTurn) {
                buttons[7].setText("X");
                buttons[7].setEnabled(false);
                sendMessage("7");
                myTurn = false;
            }
        });
        buttons[8].addActionListener(a -> {
            if (myTurn) {
                buttons[8].setText("X");
                buttons[8].setEnabled(false);
                sendMessage("8");
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
    private void changeXToO(String location) {
        buttons[Integer.parseInt(location)].setText("O");
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
            socket = new Socket(InetAddress.getByName(serverIP), 6789);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            whileConnected();
        } catch(EOFException e) {
            System.out.println("Client terminated connection");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            closeServer();
        }
    }

    /**
    * Listens for the server's actions and updates boarder while connected to the server
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
                    changeXToO(message);
                    myTurn = true;
                    if(noMoreButtons() && roundOver == false) {
                        updateScore("Draw");
                        sendMessage("Draw");
                    }
                } else if (message.equals("reset")) {
                    resetBoard();
                } else if (message.equals("Wins!")) {
                    updateScore("Server");
                    setClickable(false);
                    roundOver = true;
                } else if (message.equals("Lost!")) {
                    updateScore("Player");
                    setClickable(false);
                } else if (message.equals("Draw")) {
                    updateScore("Draw");
                }
            } catch(ClassNotFoundException e) {
                System.out.println("I don't know that object type");
            }
        } while(!message.equals("END"));
        setClickable(false);
        resetButtons();
    }

    /**
    * Sends the button message to the server if it is his turn
    * @param message String argument that indicates the button clicked
    */
    private void sendButtonMessage(String message) {
        if(myTurn) {
            try {
                output.writeObject(message);
                output.flush();
            } catch(IOException e) {
                System.out.println("ERROR: can't send message, no server found\n");
            }
        }
    }

    /**
    * Sends message to the server regardless who's turn
    * @param message String argument that indicates the messaged sent
    */
    private void sendMessage(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch(IOException e){
            System.out.println("ERROR: can't send message, no server found\n");
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
        } catch (Exception e) {
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
}
