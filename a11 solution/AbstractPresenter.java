import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AbstractPresenter implements TTTPresenter {

	private final static Logger LOG = Logger.getLogger(AbstractPresenter.class.getName());
	protected final static String MODEL = TTTModel.class.getName();

	protected final PropertyChangeSupport listeners;
	protected TTTModel model;

	public AbstractPresenter() {
		listeners = new PropertyChangeSupport(this);
		model = new TTTModel();
	}

	protected abstract void _move(int pos);

	public void move(Player p, int pos) {
		synchronized(this) {
			if(isLegalMove(p, pos)) {
				// legal move
				_move(pos);
			} else {
				LOG.warning("Illegal move: " + p + " " + pos);
			}
		} // synch
		// always update in case bad move from out of synch
		fireModelChangedEvent();
	}

	public void nextRound() {
		if(isRoundOver()) {
			if(model.winner != null) {
				model = new TTTModel(model.winner.getNext(), model.score);
			} else {
				model = new TTTModel(model.movesFirst.getNext(), model.score);
			}
		} else {
			LOG.info("NextRound, but game not over... ignored");
		}
		fireModelChangedEvent();
	}

	public void playerQuit() {
		model.playerQuit = true;
		fireModelChangedEvent();
	}

	public boolean isPlayerQuit() {
		return model.playerQuit;
	}

	public boolean isLegalMove(Player p, int pos) {
		return model != null && p == model.whosMove
			&& model.board[pos] == null && !isRoundOver();
	}

	public Player getWhosMove() {
		return model.whosMove;
	}

	public int[] getScore() {
		return Arrays.copyOf(model.score, model.score.length);
	}

	public Player[] getBoard() {
		return Arrays.copyOf(model.board, model.board.length);
	}

	public Win getWin() {
		return model.win;
	}

	public Player getWinner() {
		return model.winner;
	}

	public boolean isRoundOver() {
		return model.win != null || model.moves == model.board.length || model.playerQuit;
	}

	public boolean isDraw() {
		return model.moves == model.board.length && model.win == null;
	}

	protected void addListener(String name, PropertyChangeListener l) {
		listeners.addPropertyChangeListener(name, l);
	}

	public void addModelListener(PropertyChangeListener l) {
		addListener(MODEL, l);
		PropertyChangeEvent pce = new PropertyChangeEvent(this,
			MODEL, null, model.clone());
		l.propertyChange(pce);
	}

	protected void fireModelChangedEvent() {
		listeners.firePropertyChange(TTTModel.class.getName(), null,
			model != null ? model.clone() : null);
	}

	public boolean equals(Object o) {
		return equals((TTTModel)o);
	}
	private boolean equals(TTTModel o) {
		return Objects.equals(this.model, o);
	}
}
