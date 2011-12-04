package mirroruniverse.g7;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class Player implements mirroruniverse.sim.Player {

	private static final int maxRows = 100;
	private static final int maxCols = 100;

	private static final int fullMapRows = maxRows * 2 - 1;
	private static final int fullMapCols = maxCols * 2 - 1;

	private byte[][] fullLeftMap = new byte [fullMapRows][fullMapCols];
	private byte[][] fullRightMap = new byte [fullMapRows][fullMapCols];

	private HashSet <Integer> leftRowDis = new HashSet <Integer> ();
	private HashSet <Integer> leftColDis = new HashSet <Integer> ();

	private HashSet <Integer> rightRowDis = new HashSet <Integer> ();
	private HashSet <Integer> rightColDis = new HashSet <Integer> ();

	private int i1, j1, i2, j2;

	private boolean leftExplored = false;
	private boolean rightExplored = false;

	private boolean exitLeftFound = false;
	private boolean exitRightFound = false;

	private boolean exitLeftReached = false;
	private boolean exitRightReached = false;

	private int pathPos = 0;
	private int[] path = new int [0];

	public Player() {
		fill(fullLeftMap, -1);
		fill(fullRightMap, -1);
		i1 = i2 = fullMapRows / 2;
		j1 = j2 = fullMapCols / 2;
		fullLeftMap[i1][j1] = 0;
		fullRightMap[i2][j2] = 0;
	}

	private static boolean inMap(int i, int j) {
		return i >=0 && j >= 0 && i < fullMapRows && j < fullMapCols;
	}

	private boolean canLeftMove(int dir) {
		if (fullLeftMap[i1][j1] == 2)
			return false;
		int di = dirToCode[dir][0];
		int dj = dirToCode[dir][1];
		int ni = i1 + di;
		int nj = j1 + dj;
		if (!inMap(ni, nj))
			return false;
		return oneOf(fullLeftMap[ni][nj], 0, 2);
	}

	private boolean canRightMove(int dir) {
		if (fullRightMap[i2][j2] == 2)
			return false;
		int di = dirToCode[dir][0];
		int dj = dirToCode[dir][1];
		int ni = i2 + di;
		int nj = j2 + dj;
		if (!inMap(ni, nj))
			return false;
		return oneOf(fullRightMap[ni][nj], 0, 2);
	}

	private void move(int dir) {
		int di = dirToCode[dir][0];
		int dj = dirToCode[dir][1];
		if (canLeftMove(dir)) {
			i1 += di;
			j1 += dj;
		}
		if (canRightMove(dir)) {
			i2 += di;
			j2 += dj;
		}
	}

	private boolean leftOnGoal() {
		return fullLeftMap[i1][j1] == 2;
	}

	private boolean rightOnGoal() {
		return fullRightMap[i2][j2] == 2;
	}

	private static boolean consistent(int before, int after) {
		return after != -1 && (before == -1 || after == before);
	}

	private void dump(int[][] visionLeft, int[][] visionRight) {
		// dump the left map
		boolean leftUpdated = false;
		int visionLeftRows = visionLeft.length;
		int visionLeftCols = visionLeft[0].length;
		int vi1 = visionLeftRows / 2;
		int vj1 = visionLeftCols / 2;
		for (int i = 0 ; i != visionLeftRows ; ++i)
			for (int j = 0 ; j != visionLeftCols ; ++j) {
				int mi = i - vi1 + i1;
				int mj = j - vj1 + j1;
				if (inMap(mi, mj)) {
					int before = fullLeftMap[mi][mj];
					int after = visionLeft[i][j];
					if (!consistent(before, after))
						throw new RuntimeException("Inconsistent vision");
					if (before == -1) {
						leftUpdated = true;
						if (after == 2)
							exitLeftFound = true;
						fullLeftMap[mi][mj] = (byte) after;
					}
				} else if (visionLeft[i][j] != 1)
					throw new RuntimeException("Inconsistent vision");
			}
		// cut parts of left map
		if (leftUpdated) {
			int[] boundaries = boundaries(fullLeftMap, 0, 2);
			for (int i = 0 ; i != fullMapRows ; ++i)
				if (abs(i - boundaries[0]) >= maxRows || abs(i - boundaries[1]) >= maxRows)
					if (!leftRowDis.contains(i)) {
						leftRowDis.add(i);
						for (int j = 0 ; j != fullMapCols ; ++j) {
							if (!oneOf(fullLeftMap[i][j], 1, -1))
								throw new RuntimeException("Inconsistent vision");
							fullLeftMap[i][j] = 1;
						}
					}
			for (int j = 0 ; j != fullMapCols ; ++j)
				if (abs(j - boundaries[2]) >= maxRows || abs(j - boundaries[3]) >= maxRows)
					if (!leftColDis.contains(j)) {
					leftColDis.add(j);
						for (int i = 0 ; i != fullMapRows ; ++i) {
							if (!oneOf(fullLeftMap[i][j], 1, -1))
								throw new RuntimeException("Inconsistent vision");
							fullLeftMap[i][j] = 1;
						}
					}
			// check if left exit is reachable
			if (exitLeftFound && !exitLeftReached) {
				SimpleState.init(fullLeftMap);
				SimpleState start = new SimpleState(i1, j1);
				if (Search.reachable(start)) {
					exitLeftReached = true;
					System.out.println("Left reached: " + turns);
				}
			}
			// check if left map explored completely
			if (exitLeftReached) {
				int[] explorationBoundaries = boundaries(fullLeftMap, 0, 2);
				if (!perimeterContains(fullLeftMap, explorationBoundaries, 0, -1, 2))
					leftExplored = true;
			}
		}
		// dump the right map
		boolean rightUpdated = false;
		int visionRightRows = visionRight.length;
		int visionRightCols = visionRight[0].length;
		int vi2 = visionRightRows / 2;
		int vj2 = visionRightCols / 2;
		for (int i = 0 ; i != visionRightRows ; ++i)
			for (int j = 0 ; j != visionRightCols ; ++j) {
				int mi = i - vi2 + i2;
				int mj = j - vj2 + j2;
				if (inMap(mi, mj)) {
					int before = fullRightMap[mi][mj];
					int after = visionRight[i][j];
					if (!consistent(before, after))
						throw new RuntimeException("Inconsistent vision");
					if (before == -1) {
						rightUpdated = true;
						if (after == 2)
							exitRightFound = true;
						fullRightMap[mi][mj] = (byte) after;
					}
				} else if (visionRight[i][j] != 1)
					throw new RuntimeException("Inconsistent vision");
			}
		if (rightUpdated) {
			// cut parts of right map
			int[] boundaries = boundaries(fullRightMap, 0, 2);
			for (int i = 0 ; i != fullMapRows ; ++i)
				if (abs(i - boundaries[0]) >= maxRows || abs(i - boundaries[1]) >= maxRows)
					if (!rightRowDis.contains(i)) {
						rightRowDis.add(i);
						for (int j = 0 ; j != fullMapCols ; ++j) {
							if (!oneOf(fullRightMap[i][j], 1, -1))
								throw new RuntimeException("Inconsistent vision");
							fullRightMap[i][j] = 1;
						}
					}
			for (int j = 0 ; j != fullMapCols ; ++j)
				if (abs(j - boundaries[2]) >= maxRows || abs(j - boundaries[3]) >= maxRows)
					if (!rightColDis.contains(j)) {
						rightColDis.add(j);
						for (int i = 0 ; i != fullMapRows ; ++i) {
							if (!oneOf(fullRightMap[i][j], 1, -1))
								throw new RuntimeException("Inconsistent vision");
							fullRightMap[i][j] = 1;
						}
					}
			// check if left exit is reachable
			if (exitRightFound && !exitRightReached) {
				SimpleState.init(fullRightMap);
				SimpleState start = new SimpleState(i2, j2);
				if (Search.reachable(start)) {
					exitRightReached = true;
					System.out.println("Right reached: " + turns);
				}
			}
			// check if right map explored completely
			if (exitRightReached) {
				int[] explorationBoundaries = boundaries(fullRightMap, 0, 2);
				if (!perimeterContains(fullRightMap, explorationBoundaries, 0, -1, 2))
					rightExplored = true;
			}
		}
	}

	public static final int[][] dirToCode = {
		{ 0, 0}, { 0, 1}, {-1, 1},
		{-1, 0}, {-1,-1}, { 0,-1},
		{ 1,-1}, { 1, 0}, { 1, 1}};

	public static final int[][] codeToDir = {
		{4, 3, 2},
		{5, 0, 1},
		{6, 7, 8}};

	// Turn when the first one stepped on goal
	private int oneOnGoal = 0;

	// Turns in the game
	private int turns = 0;

	private LinkedList <Integer> history = new LinkedList <Integer> ();

	public int lookAndMove(int[][] left, int[][] right) {
		int dir = -1;
		try {
		// Update turns
		turns++;
		// Add information to the map
		dump(left, right);
		// Decide next direction
		dir = pickDirection();
		// Move the player to that direction and return it
		move(dir);
		// Check if one moved on goal
		if (oneOnGoal == 0 && (leftOnGoal() || rightOnGoal()))
			oneOnGoal = turns;
		// If both in place game is finished
		if (leftOnGoal() && rightOnGoal()) {
			// Print some statistics
			System.out.println("Turns:    " + turns);
			System.out.println("Interval: " + (turns - oneOnGoal));
		}
		history.addLast(dir);
		} catch (Exception e) {
			boolean first = true;
			for (int d : history) {
				if (first)
					first = false;
				else
					System.out.print(", ");
				System.out.print(d);
			}
			System.out.println("\n-1, " + history.size());
		}
		return dir;
	}

	private int pickDirection() {
		// Starting state for searching
		State start = null;
		// We know the solution
		if (leftExplored && rightExplored)
			// Return next state
			return path[pathPos++];
		// If we need to run a search
		else if ((exitLeftReached && exitRightReached) || pathPos == path.length) {
			// Get left core of the map
			int[] lbounds = boundaries(fullLeftMap, 0, 2);
			lbounds = expand(fullLeftMap, lbounds);
			byte[][] leftCore = subArray(fullLeftMap, lbounds);
			// Get right core of the map
			int[] rbounds = boundaries(fullRightMap, 0, 2);
			rbounds = expand(fullRightMap, rbounds);
			byte[][] rightCore = subArray(fullRightMap, rbounds);
			State.init(leftCore, rightCore);
			start = new State(i1-lbounds[0], j1-lbounds[2], i2-rbounds[0], j2-rbounds[2]);
		}
		// Now we see both exits
		if (exitLeftReached && exitRightReached) {
			// Solve the problem with current information
			State[] encodedPath = Search.toGoal(start);
			// If we have solved it optimally or we
			// have perfect knowledge of both maps
			if ((leftExplored && rightExplored) ||
			    (encodedPath != null && State.searchEarlyOptimal(encodedPath))) {
				// Set the solution and return first move
				path = decodePath(encodedPath);
				pathPos = 1;
				// Return first state
				return path[0];
			}
		}
		// Pre-determined path to follow
		if (pathPos != path.length)
			return path[pathPos++];
		State[] explorePath = Search.explore(start, exitLeftReached, exitRightReached);
		// If aligned fails discard it
		if (State.keepAlignment && explorePath == null) {
			State.keepAlignment = false;
			explorePath = Search.explore(start, exitLeftReached, exitRightReached);
		}
		// If one must exit do it
		if (State.keepNonExit && explorePath == null) {
			State.keepNonExit = false;
			explorePath = Search.explore(start, exitLeftReached, exitRightReached);
		}
		// Sanity check
		if (explorePath == null)
			throw new RuntimeException("No map exit");
		// decode and cut last position
		path = decodePath(explorePath);
		if (State.keepAlignment)
			path = Arrays.copyOf(path, path.length - 1);
		pathPos = 1;
		return path[0];
	}

	private static int[] decodePath(State[] path) {
		int size = path.length - 1;
		int[] solution = new int [size];
		for (int i = 0 ; i != size ; ++i)
			solution[i] = State.direction(path[i], path[i+1]);
		return solution;
	}

	private static int[] boundaries(byte[][] data, int ... codes) {
		int north = Integer.MAX_VALUE;
		int south = -1;
		int west = Integer.MAX_VALUE;
		int east = -1;
		for (int i = 0 ; i != data.length ; ++i)
			for (int j = 0 ; j != data[i].length ; ++j)
				if (oneOf(data[i][j], codes)) {
					if (i < north) north = i;
					if (i > south) south = i;
					if (j < west)  west = j;
					if (j > east)  east = j;
				}
		int[] ret = {north, south, west, east};
		return ret;
	}

	private static boolean perimeterContains(byte[][] data, int[] boundaries, int ... codes) {
		int north = boundaries[0];
		int south = boundaries[1];
		int west  = boundaries[2];
		int east  = boundaries[3];
		int[] extendedBoundaries = expand(data, boundaries);
		int extNorth = extendedBoundaries[0];
		int extSouth = extendedBoundaries[1];
		int extWest  = extendedBoundaries[2];
		int extEast  = extendedBoundaries[3];
		if (extNorth != north)
			for (int j = west ; j <= east ; ++j)
				if (oneOf(data[extNorth][j], codes))
					return true;
		if (extSouth != south)
			for (int j = west ; j <= east ; ++j)
				if (oneOf(data[extSouth][j], codes))
					return true;
		if (extEast != east)
			for (int i = north ; i <= south ; ++i)
				if (oneOf(data[i][extEast], codes))
					return true;
		if (extWest != west)
			for (int i = north ; i <= south ; ++i)
				if (oneOf(data[i][extWest], codes))
					return true;
		if (extNorth != north && extEast != east)
			if (oneOf(data[extNorth][extEast], codes))
				return true;
		if (extNorth != north && extWest != west)
			if (oneOf(data[extNorth][extWest], codes))
				return true;
		if (extSouth != south && extEast != east)
			if (oneOf(data[extSouth][extEast], codes))
				return true;
		if (extSouth != south && extWest != west)
			if (oneOf(data[extSouth][extWest], codes))
				return true;
		return false;
	}

	private static int[] expand(byte[][] data, int[] boundaries) {
		int rows = data.length;
		int cols = data[0].length;
		int[] res = new int [4];
		res[0] = max(0, boundaries[0]-1);
		res[1] = min(rows-1, boundaries[1]+1);
		res[2] = max(0, boundaries[2]-1);
		res[3] = min(cols-1, boundaries[3]+1);
		return res;
	}

	private static byte[][] subArray(byte[][] data, int[] boundaries) {
		int north = boundaries[0];
		int south = boundaries[1];
		int west  = boundaries[2];
		int east  = boundaries[3];
		int rows = south - north + 1;
		int cols = east - west + 1;
		byte[][] res = new byte [rows][cols];
		for (int i = north ; i <= south ; ++i)
			for (int j = west ; j <= east ; ++j)
				res[i-north][j-west] = data[i][j];
		return res;
	}

	public static void fill(byte[][] data, int value) {
		byte bvalue = (byte) value;
		for (int i = 0 ; i != data.length ; ++i)
			for (int j = 0 ; j != data[i].length ; ++j)
				data[i][j] = bvalue;
	}

	public static void printBoundaries(int[] boundaries) {
		System.out.println("North: " + boundaries[0]);
		System.out.println("South: " + boundaries[1]);
		System.out.println("West:  " + boundaries[2]);
		System.out.println("East:  " + boundaries[3]);
	}

	public static boolean rectangle(int[][] arr) {
		for (int i = 0 ; i != arr.length ; ++i)
			if (arr[i].length != arr[0].length)
				return false;
		return true;
	}

	static void print(byte[][] array) {
		print(array, -1, -1);
	}

	public static void print(byte[][] array, int ti, int tj) {
		for (int i = 0 ; i != array.length ; ++i) {
			for (int j = 0 ; j != array[i].length ; ++j) {
				if (array[i][j] < 0)
					System.out.print("- ");
				else if (ti == i && tj == j)
					System.out.print("! ");
				else
					System.out.print(array[i][j] + " ");
			}
			System.out.println("");
		}
	}

	public static String printDirection(int dir) {
		switch (dir) {
			case 0: return "Still";
			case 1: return "East";
			case 2: return "North-East";
			case 3: return "North";
			case 4: return "North-West";
			case 5: return "West";
			case 6: return "South-West";
			case 7: return "South";
			case 8: return "South-East";
		}
		return null;
	}

	private static boolean oneOf(int v, int ... x) {
		for (int i = 0 ; i != x.length ; ++i)
			if (v == x[i])
				return true;
		return false;
	}

	private static int min(int ... x) {
		int min = Integer.MAX_VALUE;
		for (int i = 0 ; i != x.length ; ++i)
			if (x[i] < min)
				min = x[i];
		return min;
	}

	private static int max(int ... x) {
		int max = Integer.MIN_VALUE;
		for (int i = 0 ; i != x.length ; ++i)
			if (x[i] > max)
				max = x[i];
		return max;
	}

	private static int abs(int x) {
		return x < 0 ? -x : x;
	}
}
