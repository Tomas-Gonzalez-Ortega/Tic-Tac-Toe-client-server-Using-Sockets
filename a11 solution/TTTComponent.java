import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.stream.*;
import javax.swing.*;

public class TTTComponent extends JComponent {

	private final TTTPresenter game;
	private final Player me;

	public TTTComponent(TTTPresenter game, Player me) {
		this.game = game;
		this.me = me;

		setLayout(new BorderLayout());

		// button panel
		JPanel plnInBoard = new JPanel();
		plnInBoard.setLayout(new GridLayout(3,3));
		add(plnInBoard, BorderLayout.CENTER);
		// add the board buttons
		for(int i = 0; i < TTTModel.BOARD_LEN; i++) {
			JButton btn = new JButton();
			final int ii = i;
			game.addModelListener((pce) -> {
				TTTModel model = (TTTModel)pce.getNewValue();
				Player p = model.board[ii];
				if(p == null) {
					btn.setText("");
					btn.setEnabled(true);
				} else {
					btn.setText(p.toString());
					btn.setEnabled(false);
				}
				if(game.isRoundOver()) {
					btn.setEnabled(false);
				}
			}); // button's game change listener
			btn.addActionListener((ae) -> {
				game.move(me, ii);
			}); // button's action listener
			plnInBoard.add(btn);
		} // for board buttons

		// top status
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(1,3));
		add(top, BorderLayout.NORTH);
		JLabel lblOutStatus = new JLabel();
		JLabel lblOutScore = new JLabel();
		top.add(new JLabel("You are " + me.toString()));
		top.add(lblOutStatus);
		top.add(lblOutScore);
		game.addModelListener((pce) -> {
			Player p = ((TTTModel)pce.getNewValue()).whosMove;
			lblOutStatus.setText(p + "'s move.");
			int[] newScore = ((TTTModel)pce.getNewValue()).score;
			String score = IntStream.range(0, Player.values().length)
				.mapToObj(i -> Player.values()[i] + ":" + newScore[i])
				.collect(Collectors.joining(" "));
			lblOutScore.setText("Score: " + score);
		});

		// dialog for end of round/end of game
		game.addModelListener((pce) -> {
			if(game.isPlayerQuit()) {
				playerQuit(lblOutStatus);
			} else if(game.isRoundOver()) {
				displayWinner(lblOutStatus);
			}
		});
	}

	private void playerQuit(JLabel lblOutStatus) {
		String msg = "Other player quit.";
		lblOutStatus.setText(msg);
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this, msg);
			System.exit(0);
		});
	}

	private void displayWinner(JLabel lblOutStatus) {
		String msg = game.isDraw() ? "Draw." : game.getWinner() + " wins.";
		lblOutStatus.setText(msg);
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this, msg);
			game.nextRound();
			while(game.isRoundOver()) {
				JOptionPane.showMessageDialog(this, "Waiting for other player.");
				if(game.isPlayerQuit()) {
					playerQuit(lblOutStatus);
					break;
				}
			}
		});
	}
}
