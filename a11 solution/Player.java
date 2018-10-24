public enum Player {
	X, O;

	public Player getNext() {
		switch (this) {
			case X: return O;
			case O: return X;
			default: throw new IllegalStateException();
		}
	}
}
