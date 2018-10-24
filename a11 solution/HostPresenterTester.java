import java.util.*;
import java.util.stream.*;
import java.beans.PropertyChangeListener;

public class HostPresenterTester {
	public static void main(String[] args) {
		new TestRunner(HostPresenterTester.class).run();
	}

	public void testMakeMove() {
		HostPresenter p = new HostPresenter();

		TTTModel expected = new TTTModel();

		// special, wrong Player
		p.move(Player.O, 0);
		assert Objects.equals(p, expected);
		p.move(Player.O, 0);
		assert Objects.equals(p, expected);

		// boundary: legal
		p.move(Player.X, 0);
		expected.board[0] = Player.X;
		expected.whosMove = Player.O;
		expected.moves++;
		assert Objects.equals(p, expected);

		// boundary: legal
		p.move(Player.O, 8);
		expected.board[8] = Player.O;
		expected.whosMove = Player.X;
		expected.moves++;
		assert Objects.equals(p, expected);

		// special: occupied space
		p.move(Player.X, 0); // has X
		Objects.equals(p, expected);
		p.move(Player.X, 8); // has O
		Objects.equals(p, expected);
	}

	public void testWins() {
		HostPresenter p = new HostPresenter();
		assert Arrays.equals(new int[] {0,0}, p.getScore());

		EnumMap<Player, Integer> expScore = new EnumMap<>(Player.class);
		Stream.of(Player.values())
			.forEach(player -> expScore.put(player, 0));

		for(Win w : Win.values()) {
			int[] movesToWin = w.getPos();
			Player toWin = p.getWhosMove();
			for(final int pos : movesToWin) {
				assert !p.isRoundOver();
				assert p.getWin() == null;
				assert p.getWinner() == null;
				assert p.getWhosMove() == toWin;
				assert !p.isDraw();
				p.move(toWin, pos);
				if(pos != movesToWin[movesToWin.length-1]) {
					int randomPlace = randomPlace(p.getBoard(), movesToWin);
					p.move(toWin.getNext(), randomPlace);
					assert !p.isRoundOver();
					assert p.getWin() == null;
					assert p.getWinner() == null;
					assert p.getWhosMove() == toWin;
					assert !p.isDraw();
				}
			}
			assert p.isRoundOver();
			assert p.getWin() == w;
			assert p.getWinner() == toWin;
			assert p.getWhosMove() == null;
			assert !p.isDraw();

			expScore.merge(toWin, 1, Integer::sum);

			assert expScore.get(Player.X) == p.getScore()[Player.X.ordinal()] : "score";
			assert expScore.get(Player.O) == p.getScore()[Player.O.ordinal()] : "score";

			p.nextRound();
			assert p.getWhosMove() == toWin.getNext();
		}
	}

	public void testDraw() {
		HostPresenter p = new HostPresenter();
		assert p.getWhosMove() == Player.X;

		int[] moves = new int[] {0,1,2,6,7,8,3,4,5};
		for(int m : moves) {
			p.move(p.getWhosMove(), m);
		}
		assert p.isRoundOver();
		assert p.getWhosMove() == null;
		assert p.getWin() == null;
		assert p.getWinner() == null;
		assert p.isDraw();

		p.nextRound();
		assert !p.isRoundOver();
		assert p.getWhosMove() == Player.O;
		assert p.getWin() == null;
		assert p.getWinner() == null;
		assert !p.isDraw();
		assert Arrays.equals(new int[] {0,0}, p.getScore());
	}

	public void testListener() {
		HostPresenter p = new HostPresenter();

		final List<TTTModel> models = new LinkedList<>();
		PropertyChangeListener lis = (pce) -> {
			assert pce.getSource() == p;
			assert pce.getNewValue() instanceof TTTModel;
			assert Objects.equals(p, pce.getNewValue());
			for(TTTModel m : models) {
				assert !Objects.equals(m, pce.getNewValue());
			}
			models.add((TTTModel) pce.getNewValue());
		};
		p.addModelListener(lis);
		assert 1 == models.size();

		p.move(Player.X, 0);
		assert 2 == models.size();

		p.move(Player.O, 1);
		assert 3 == models.size();
		p.move(Player.X, 2);
		assert 4 == models.size();
	}

	private static int randomPlace(Player[] board, int[] exclude) {
		return IntStream.range(0, board.length)
			.filter(i -> board[i] == null)
			.filter(i -> IntStream.of(exclude).noneMatch(e -> e == i))
			.findAny()
			.getAsInt();
	}
}
