import java.beans.PropertyChangeListener;

public interface TTTPresenter {

	public Player getWhosMove();
	public int[] getScore();
	public Player[] getBoard();
	public Win getWin();
	public Player getWinner();
	public boolean isRoundOver();
	public boolean isDraw();

	public void move(Player p, int pos);
	public void nextRound();

	public void playerQuit();
	public boolean isPlayerQuit();

	public void addModelListener(PropertyChangeListener l);
}
