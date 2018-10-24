import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class TTTModel implements Serializable {

	public final static int BOARD_LEN = 9;

	public final int[] score;
	public final Player[] board;
	public final Player movesFirst;
	public Player whosMove;
	public Win win;
	public Player winner;
	public int moves;
	public boolean playerQuit;

	public TTTModel() {
		this(Player.X, new int[Player.values().length]);
	}

	public TTTModel(Player firstMove, int[] aScore) {
		score = aScore;
		board = new Player[BOARD_LEN];
		movesFirst = firstMove;
		whosMove = firstMove;
		win = null;
		winner = null;
		moves = 0;
		playerQuit = false;
	}

	private TTTModel(TTTModel other) {
		this.score = Arrays.copyOf(other.score, other.score.length);
		this.board = Arrays.copyOf(other.board, other.board.length);
		this.movesFirst = other.movesFirst;
		this.whosMove = other.whosMove;
		this.win = other.win;
		this.winner = other.winner;
		this.moves = other.moves;
		this.playerQuit = other.playerQuit;
	}

	public String toString() {
		return new StringBuilder()
			.append(Arrays.toString(score))
			.append(Arrays.toString(board))
			.append(movesFirst)
			.append(whosMove)
			.append(win)
			.append(winner)
			.append(moves)
			.append(playerQuit)
			.toString();
	}

	public TTTModel clone() {
		return new TTTModel(this);
	}

	public boolean equals(Object o) {
		return o instanceof TTTModel
			&& equals((TTTModel)o);
	}

	private boolean equals(TTTModel o) {
		return Arrays.equals(this.board, o.board)
			&& this.movesFirst == o.movesFirst
			&& this.whosMove == o.whosMove
			&& this.win == o.win
			&& this.winner == o.winner
			&& this.moves == o.moves
			&& this.playerQuit == o.playerQuit;
	}

	public int hashCode() {
		return Objects.hash(board,
			movesFirst,
			win,
			winner,
			moves,
			playerQuit);
	}
}
