import java.util.stream.*;

public class HostPresenter extends AbstractPresenter implements TTTPresenter {

	protected void _move(int pos) {
		model.board[pos] = model.whosMove;
		model.moves++;

		model.win = Stream.of(Win.values())
			.filter(w -> w.isWin(model.board))
			.findAny()
			.orElse(null);
		if(model.win != null) {
			model.winner = model.win.getWinner(model.board);
			model.score[model.winner.ordinal()]++;
		}

		if(isRoundOver()) {
			model.whosMove = null;
		} else {
			model.whosMove = model.whosMove.getNext();
		}
	}
}
