package mirroruniverse.g7;

public class State {

	/* State that works with 2-level BFS search
	 *
	 *  getNext:
	 *   Returns all possible next states from a state
	 *   if one player has reached his goal then he does
	 *   not move anymore while the other player moves
	 *
	 *  isFinal:
	 *   Returns if the state can progress to the next
	 *   level of search, that is if one of the two
	 *   players has reached his goal
	 *
	 *  isGoal:
	 *   Both players have reached their goal
	 *
	 *  isFinalOptimal:
	 *   The state is a final state and is also the
	 *   optimal final state, so the search can
	 *   discard all other final stages and keep
	 *   only this one to expand until the goal
	 */

	public static final int maxNextStates = 8;
	public static final byte unusedMoveCode = 30;
	public static final byte nullMoveCode = 0;

	private static byte[][] leftMap;
	private static byte[][] rightMap;

	private static int leftRows;
	private static int leftCols;

	private static int rightRows;
	private static int rightCols;

	private static boolean zeroPossible;

	public static boolean keepAlignment = true;
	public static boolean keepNonExit = true;

	private static State end;

	private byte i1, j1, i2, j2;

	public static int leftSize() {
		return leftRows * leftCols;
	}

	public static int rightSize() {
		return rightRows * rightCols;
	}

	public static void init(byte[][] left, byte[][] right) {
		/* Keep starting state */
		State end = dumpState();
		/* Start and end flags */
		boolean leftGoalFound = false;
		/* Set left map */
		leftRows = left.length;
		leftCols = left[0].length;
		leftMap = left;
		for (int i = 0 ; i != leftRows ; ++i)
			for (int j = 0 ; j != leftCols ; ++j)
				if (leftMap[i][j] == 2) {
					leftMap[i][j] = 0;
					end.i1 = (byte) i;
					end.j1 = (byte) j;
					if (leftGoalFound)
						throw new IllegalArgumentException();
					leftGoalFound = true;
				}
		/* Reset start and end flags */
		boolean rightGoalFound = false;
		/* Set right map */
		rightRows = right.length;
		rightCols = right[0].length;
		rightMap = right;
		for (int i = 0 ; i != rightRows ; ++i)
			for (int j = 0 ; j != rightCols ; ++j)
				if (rightMap[i][j] == 2) {
					rightMap[i][j] = 0;
					end.i2 = (byte) i;
					end.j2 = (byte) j;
					if (rightGoalFound)
						throw new IllegalArgumentException();
					rightGoalFound = true;
				}
		/* Store the goal and return the start */
		State.end = end;
		/* Check if zero solution is possible */
		zeroPossible = zeroPossible();
	}

	public static boolean goalExists() {
		return end.i1 != -1 && end.j1 != -1 && end.i2 != -1 && end.j2 != -1;
	}

	public static State dumpState() {
		return new State(-1, -1, -1, -1);
	}

	public static boolean zeroPossible() {
		for (int di = -1 ; di <= 1 ; ++di)
			for (int dj = -1 ; dj <= 1 ; ++dj)
				if (di != 0 || dj != 0) {
					int i1 = end.i1 + di;
					int j1 = end.j1 + dj;
					int i2 = end.i2 + di;
					int j2 = end.j2 + dj;
					if (inMapLeft(i1, j1) && leftMap[i1][j1] == 0 &&
					    inMapRight(i2, j2) && rightMap[i2][j2] == 0)
						return true;
				}
		return false;
	}

	public static boolean searchEarlyOptimal(State[] solution) {
		int len = solution.length;
		if (!solution[len - 1].searchGoal())
			return false;
		if (len == 1)
			return true;
		if (zeroPossible)
			return !solution[len - 2].searchFinal();
		if (len == 2)
			return true;
		return !solution[len - 3].searchFinal();
	}

	public State(int i1, int j1, int i2, int j2) {
		this.i1 = (byte) i1;
		this.j1 = (byte) j1;
		this.i2 = (byte) i2;
		this.j2 = (byte) j2;
	}

	public State(State par) {
		i1 = par.i1;
		j1 = par.j1;
		i2 = par.i2;
		j2 = par.j2;
	}

	private boolean leftOnGoal() {
		return i1 == end.i1 && j1 == end.j1;
	}

	private boolean rightOnGoal() {
		return i2 == end.i2 && j2 == end.j2;
	}

	public boolean searchGoal() {
		return leftOnGoal() && rightOnGoal();
	}

	public boolean searchFinal() {
		return leftOnGoal() || rightOnGoal();
	}

	public boolean searchOptimalFinal() {
		if (zeroPossible)
			return searchGoal();
		if (!searchFinal())
			return false;
		int dist1 = distanceSquared(i1, j1, end.i1, end.j1);
		int dist2 = distanceSquared(i2, j2, end.i2, end.j2);
		return dist1 <= 2 && dist2 <= 2;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof State))
			return false;
		State p = (State) obj;
		return i1 == p.i1 && j1 == p.j1 && i2 == p.i2 && j2 == p.j2;
	}

	public int id() {
		int rightSize = rightCols * rightRows;
		return (i1 * leftCols + j1) * rightSize + (i2 * rightCols + j2);
	}

	public static void byId(State s, int id) {
		int rightSize = rightCols * rightRows;
		int p1 = id / rightSize;
		int p2 = id % rightSize;
		s.i1 = (byte) (p1 / leftCols);
		s.j1 = (byte) (p1 % leftCols);
		s.i2 = (byte) (p2 / rightCols);
		s.j2 = (byte) (p2 % rightCols);
	}

	private static boolean inMapLeft(int i, int j) {
		return i >= 0 && i < leftRows && j >= 0 && j < leftCols;
	}

	private static boolean inMapRight(int i, int j) {
		return i >= 0 && i < rightRows && j >= 0 && j < rightCols;
	}

	public int searchNext(byte[] move, State[] nextState) {
		// if one has reached his goal he will not move
		boolean finLeft = i1 == end.i1 && j1 == end.j1;
		boolean finRight = i2 == end.i2 && j2 == end.j2;
		boolean immLeft, immRight;
		int c = 0;
		for (int d = 1 ; d != 9 ; ++d) {
			int dir = d;
			int di = Player.dirToCode[d][0];
			int dj = Player.dirToCode[d][1];
			if (finLeft || !inMapLeft(i1 + di, j1 + dj))
				immLeft = true;
			else {
				int m = leftMap[i1 + di][j1 + dj];
				// cannot move to unknown territory
				if (m == -1) continue;
				immLeft = (m == 1);
			}
			if (finRight || !inMapRight(i2 + di, j2 + dj))
				immRight = true;
			else {
				int m = rightMap[i2 + di][j2 + dj];
				// cannot move to unknown territory
				if (m == -1) continue;
				immRight = (m == 1);
			}
			// one at least must move
			if (immLeft && immRight)
				continue;
			if (!immLeft) {
				nextState[c].i1 = (byte) (i1 + di);
				nextState[c].j1 = (byte) (j1 + dj);
			} else {
				nextState[c].i1 = i1;
				nextState[c].j1 = j1;
				dir += 10;
			}
			if (!immRight) {
				nextState[c].i2 = (byte) (i2 + di);
				nextState[c].j2 = (byte) (j2 + dj);
			} else {
				nextState[c].i2 = i2;
				nextState[c].j2 = j2;
				dir += 20;
			}
			move[c++] = (byte) dir;
		}
		return c;
	}

	public boolean exploreGoal() {
		return leftMap[i1][j1] == -1 || rightMap[i2][j2] == -1;
	}

	public static void print() {
		System.out.println("Left:");
		Player.print(leftMap);
		System.out.println("Right:");
		Player.print(rightMap);
	}

	public int exploreNext(byte[] move, State[] nextState) {
		// if one has reached his goal he will not move
		boolean finLeft = i1 == end.i1 && j1 == end.j1;
		boolean finRight = i2 == end.i2 && j2 == end.j2;
		// no point to keep alignment if one has finished
		boolean aligned = !finLeft && !finRight && keepAlignment;
		boolean immLeft, immRight;
		int c = 0;
		for (int d = 1 ; d != 9 ; ++d) {
			boolean leftNowOnGoal = false;
			boolean rightNowOnGoal = false;
			int dir = d;
			int di = Player.dirToCode[d][0];
			int dj = Player.dirToCode[d][1];
			if (finLeft || !inMapLeft(i1 + di, j1 + dj))
				immLeft = true;
			else
				immLeft = leftMap[i1 + di][j1 + dj] == 1;
			if (finRight || !inMapRight(i2 + di, j2 + dj))
				immRight = true;
			else
				immRight = rightMap[i2 + di][j2 + dj] == 1;
			// one at least must move
			if (immLeft && immRight)
				continue;
			// both must move is alignment must be kept
			if (aligned && (immLeft || immRight))
				continue;
			if (!immLeft) {
				nextState[c].i1 = (byte) (i1 + di);
				nextState[c].j1 = (byte) (j1 + dj);
				if (nextState[c].i1 == end.i1 && nextState[c].j1 == end.j1)
					leftNowOnGoal = true;
			} else {
				nextState[c].i1 = i1;
				nextState[c].j1 = j1;
				dir += 10;
			}
			if (!immRight) {
				nextState[c].i2 = (byte) (i2 + di);
				nextState[c].j2 = (byte) (j2 + dj);
				if (nextState[c].i2 == end.i2 && nextState[c].j2 == end.j2)
					rightNowOnGoal = true;
			} else {
				nextState[c].i2 = i2;
				nextState[c].j2 = j2;
				dir += 20;
			}
			// we do no let anyone terminate if he has not already
			if (keepNonExit && (leftNowOnGoal || rightNowOnGoal))
				continue;
			move[c++] = (byte) dir;
		}
		return c;
	}

	public int unexplored(boolean leftOrRight) {
		// if one has reached his goal he will not move
		boolean finLeft = i1 == end.i1 && j1 == end.j1;
		boolean finRight = i2 == end.i2 && j2 == end.j2;
		State nextState = dumpState();
		boolean immLeft, immRight;
		int c = 0;
		for (int d = 0 ; d != 9 ; ++d) {
			int di = Player.dirToCode[d][0];
			int dj = Player.dirToCode[d][1];
			if (finLeft || !inMapLeft(i1 + di, j1 + dj))
				immLeft = true;
			else
				immLeft = leftMap[i1 + di][j1 + dj] == 1;
			if (finRight || !inMapRight(i2 + di, j2 + dj))
				immRight = true;
			else
				immRight = rightMap[i2 + di][j2 + dj] == 1;
			if (!immLeft) {
				nextState.i1 = (byte) (i1 + di);
				nextState.j1 = (byte) (j1 + dj);
			} else {
				nextState.i1 = i1;
				nextState.j1 = j1;
			}
			if (!immRight) {
				nextState.i2 = (byte) (i2 + di);
				nextState.j2 = (byte) (j2 + dj);
			} else {
				nextState.i2 = i2;
				nextState.j2 = j2;
			}
			if (leftOrRight && (!immLeft || d == 0) && leftMap[nextState.i1][nextState.j1] == -1)
				c++;
			if (!leftOrRight && (!immRight || d == 0) && rightMap[nextState.i2][nextState.j2] == -1)
				c++;
		}
		return c;
	}

	public static int direction(State from, State to) {
		int di = to.i1 - from.i1;
		int dj = to.j1 - from.j1;
		if (di != 0 || dj != 0)
			return Player.codeToDir[di+1][dj+1];
		di = to.i2 - from.i2;
		dj = to.j2 - from.j2;
		return Player.codeToDir[di+1][dj+1];
	}

	private static boolean isValidLeft(int i, int j) {
		return inMapLeft(i, j) && leftMap[i][j] == 0;
	}

	private static boolean isValidRight(int i, int j) {
		return inMapRight(i, j) && rightMap[i][j] == 0;
	}

	public static State trace(State to, int dir) {
		boolean im1 = dir > 10 && dir < 20;
		boolean im2 = dir > 20;
		dir %= 10;
		int di = Player.dirToCode[dir][0];
		int dj = Player.dirToCode[dir][1];
		State from = new State(to);
		if (!im1 && isValidLeft(to.i1 - di, to.j1 - dj)) {
			from.i1 -= di;
			from.j1 -= dj;
		}
		if (!im2 && isValidRight(to.i2 - di, to.j2 - dj)) {
			from.i2 -= di;
			from.j2 -= dj;
		}
		return from;
	}

	public String toString() {
		return "[" + i1 + "," + j1 + "][" + i2 + "," + j2 + "]";
	}

	private static int distanceSquared(int x, int y, int tx, int ty) {
		int dx = x - tx;
		int dy = y - ty;
		return dx * dx + dy * dy;
	}
}
