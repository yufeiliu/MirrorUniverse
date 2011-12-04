package mirroruniverse.g7;

public class SimpleState {

	public static final int maxNextStates = 8;

	private static byte[][] map;
	private static int rows;
	private static int cols;

	public static void init(byte[][] m) {
		map = m;
		rows = map.length;
		cols = map[0].length;
	}

	private short i, j;

	public static SimpleState dumpState() {
		return new SimpleState(-1, -1);
	}

	public SimpleState(int i, int j) {
		this.i = (short) i;
		this.j = (short) j;
	}

	public SimpleState(SimpleState p) {
		i = p.i;
		j = p.j;
	}

	public boolean goal() {
		return map[i][j] == 2;
	}

	public boolean inMap() {
		return i >= 0 && i < rows && j >= 0 && j < cols;
	}

	public int next(SimpleState[] nextState) {
		int c = 0;
		for (int d = 1 ; d != 9 ; ++d) {
			int di = Player.dirToCode[d][0];
			int dj = Player.dirToCode[d][1];
			// must move
			if (!inMap())
				continue;
			int m = map[i + di][j + dj];
			if (m == -1 || m == 1)
				continue;
			// valid next state
			nextState[c].i = (short) (i + di);
			nextState[c].j = (short) (j + dj);
			c++;
		}
		return c;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleState))
			return false;
		SimpleState p = (SimpleState) obj;
		return i == p.i && j == p.j;
	}

	public int hashCode() {
		return i * rows + j;
	}
}
