import java.beans.PropertyChangeListener;

public class ClientPresenter extends AbstractPresenter {

	private final static String MOVE = "move";

	protected void _move(int pos) {
		// send a "move" event
		listeners.firePropertyChange(MOVE, null, pos);
	}

	public void nextRound() {
		// do nothing;
	}

	public void update(TTTModel model) {
		this.model = model;
		fireModelChangedEvent();
	}

	public void addMoveListener(PropertyChangeListener l) {
		addListener(MOVE, l);
	}
}
