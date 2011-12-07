package mirroruniverse.g6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {

	public static final boolean DEBUG = false;
	public static final boolean SID_DEBUG = true;
	public static final boolean SID_DEBUG_VERBOSE = false;
	private static final boolean CRASH_ON_ERROR = true;
	
	private static final int MAX_MAP_SIZE = 100;
	private static final int INTERNAL_MAP_SIZE = MAX_MAP_SIZE * 2;
	private static final double NUM_MOVES = 8;
	
	
	/* TODO: tune these */
	private static final int WEIGHT_ON_PATH_LENGTH = 1;
	private static final int WEIGHT_ON_SQUARES_UNCOVERED_BY_OTHER_PLAYER = 3;
	private static final int WEIGHT_ON_KEEPING_ALIGNMENT = 10;
	
	private boolean leftExitReachable;
	private boolean rightExitReachable;
	
	// Stores maps.
	private int[][] left = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	private int[][] right = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	
	//TODO: tune this
	private static final int PATHS_TO_TRY_IN_EXPLORATION = 50;
	
	private Node currentLocationLeft;
	private Node currentLocationRight;
	
	/*
	 * The current location of each player within the 200x200 grid.
	 */
	private int x1, x2, y1, y2;
	
	/*
	 * sight radius on the left, and on the right
	 */
	private int r1 = -1, r2 = -1;
	
	private boolean exitsFound;
	private boolean leftExitFound;
	private boolean rightExitFound;
	
	private boolean leftExited;
	private boolean rightExited;
	
	private boolean radiiDiscovered;
	
	private boolean didExhaustiveCheck;
	
	private boolean computedSolution;
	private boolean computedSolutionWhenRightFullyExplored;
	private boolean computedSolutionWhenLeftFullyExplored;
	
	private boolean leftExplored;
	private boolean rightExplored;


	private HashMap<String, Node> cacheLeft = new HashMap<String, Node>();
	private HashMap<String, Node> cacheRight = new HashMap<String, Node>();
	
	private LinkedList<Edge> exploreGoal = new LinkedList<Edge>();
	
	
	private ArrayList<Pair<Integer,Integer>> leftTwitching = new ArrayList<Pair<Integer,Integer>>();
	private ArrayList<Pair<Integer,Integer>> rightTwitching = new ArrayList<Pair<Integer,Integer>>();
	
	/*
	 * Array of moves for the solution. null if unsolved.
	 */
	private Solution solution;
	
	/*
	 * Solver used to provide the solution. Can be swapped out to use
	 * a different method if necessary.
	 */
	private Solver solver;
	
	
	public G6Player() {
		// Set all points to be unknown.
		for (int i = 0; i < INTERNAL_MAP_SIZE; i++) {
			for (int j = 0; j < INTERNAL_MAP_SIZE; j++) {
				left[i][j] = Utils.entitiesToShen(Entity.UNKNOWN);
				right[i][j] = Utils.entitiesToShen(Entity.UNKNOWN);
			}
		}
		// Start coordinates to be the center of map so that we can
		// deal with a 100x100 map in any direction. Subtract by 1
		// to account for 0 based indexing.
		x1 = x2 = y1 = y2 = INTERNAL_MAP_SIZE / 2 - 1;
		solver = new DFASolver();
	}
	
	private boolean shouldRecomputeSolution() {		
		// recompute when:
			// we finish examining a fake solution
			// when we first see the exits, 
			// after we've explored all of either board.
			// when we're adjacent to an exit
		if (solution != null && solution.isFake() && solution.isCompleted()) {
			return true;
		}
		if (computedSolutionWhenLeftFullyExplored &&
				computedSolutionWhenRightFullyExplored) {
			return false;
		}
		if (solution == null || solution.getDiff() > 0) {
			if (isRightFullyExplored() && !computedSolutionWhenLeftFullyExplored) {
				if (isLeftFullyExplored()) {
					computedSolutionWhenRightFullyExplored = true;	
				}
				computedSolutionWhenLeftFullyExplored = true;
				return true;
			}
			if (isLeftFullyExplored() && !computedSolutionWhenRightFullyExplored) {
				computedSolutionWhenRightFullyExplored = true;
				return true;
			}
			if (!computedSolution) {
				computedSolution = true;
				return true;
			}
			if (isNextToExit(left, x1, y1) || isNextToExit(right, x2, y2)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isNextToExit(int[][] knowledge, int x, int y) {
		int exit_y = -999;
		int exit_x = -999;
		for(int i = 0; i < knowledge.length; i++)
			for(int j = 0; j < knowledge[0].length; j++)
				if(knowledge[i][j] == Utils.entitiesToShen(Entity.EXIT)) {
					exit_y = i;
					exit_x = j;
				}
		
		return (Math.abs(exit_y - y) == 1) || (Math.abs(exit_x - x) == 1);
	}
	
	private boolean isFullyExplored() {
		return isRightFullyExplored() && isLeftFullyExplored();
	}
	
	private boolean isLeftFullyExplored() {
		if (!leftExitFound) {
			return false;
		}
		if (leftExplored) {
			return true;
		}
		leftExplored = isFullyExplored(left);
		return leftExplored;
	}

	private boolean isRightFullyExplored() {
		if (!rightExitFound) {
			return false;
		}
		if (rightExplored) {
			return true;
		}
		rightExplored = isFullyExplored(right);
		return rightExplored;
	}

	private boolean isFullyExplored(int[][] map) {
		for (int i = 0; i < INTERNAL_MAP_SIZE; i++) {
			for (int j = 0; j < INTERNAL_MAP_SIZE; j++) {
				if (map[i][j] == Utils.entitiesToShen(Entity.UNKNOWN)) {
					// Check if a neighbor is 0
					for (int dy = -1; dy <= 1; dy++) {
						int newI = i + dy;
						if (newI >= INTERNAL_MAP_SIZE || newI < 0) {
							continue;
						}
						for (int dx = -1; dx <= 1; dx++) {
							int newJ = j + dx;
							if (newJ >= INTERNAL_MAP_SIZE || newJ < 0) {
								continue;
							}
							if (map[newI][newJ] ==
									Utils.entitiesToShen(Entity.SPACE)) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * @return node that represents current location
	 */
	private Node updateGraph(HashMap<String, Node> cache, int[][] v, int x, int y, int r) {
		
		int iMax = v.length - 1;
		int jMax = v[0].length - 1;
		int iMedian = v.length/2;
		int jMedian = v[0].length/2;
		
		Node cur = null;
		
		if (DEBUG) System.out.println("===================== " + x + "," + y);
		if (DEBUG) Utils.print2DArray(v);
		
		for (int i = 0; i < v.length; i++) {
			for (int j = 0; j < v[0].length; j++) {
				
				int curX = x + (i-iMedian);
				int curY = y + (j-jMedian);
				
				Node n = getFromCache(cache, v[i][j], curX, curY);
				if (Utils.shenToEntities(v[i][j]==Utils.entitiesToShen(Entity.PLAYER) ? Utils.entitiesToShen(Entity.SPACE) : v[i][j]) == Entity.OBSTACLE) {
					continue;
				}
				
				for (int d = 1; d <= 8; d++) {
					int dj = MUMap.aintDToM[d][0];
					int di = MUMap.aintDToM[d][1];
					
					if (i + di <= iMax && i + di >= 0 &&
							j + dj <= jMax && j + dj >= 0 && curX + di >=0 && curY + dj >= 0) {
						int neighborVal = v[i+di][j+dj];
						if (Utils.shenToEntities(neighborVal) == Entity.SPACE || Utils.shenToEntities(neighborVal) == Entity.PLAYER || Utils.shenToEntities(neighborVal) == Entity.EXIT) {
							Node neighbor = getFromCache(cache, (v[i+di][j+dj]==Utils.entitiesToShen(Entity.PLAYER) ? Utils.entitiesToShen(Entity.SPACE) : v[i+di][j+dj]), curX + di, curY + dj);
							Edge edge = new Edge();
							edge.from = n;
							edge.to = neighbor;
							edge.move = Utils.shenToMove(d);
							n.edges.add(edge);
						
						} else if (Utils.shenToEntities(neighborVal) == Entity.OBSTACLE) {
							Edge edge = new Edge();
							edge.from = n;
							edge.to = n;
							edge.move = Utils.shenToMove(d);
							n.edges.add(edge);
						}
					}
				}
				
				if (i==iMedian && j==jMedian) {
					cur = n;
				}
			}
		}
		if (DEBUG) System.out.println("cur location: " + cur);
		return cur;
	}
	
	private Node getFromCache(HashMap<String, Node> cache, int val, int x, int y) {
		if (!cache.containsKey(makeKey(x,y))) {
			Node n = new Node();
			n.entity = Utils.shenToEntities(val);
			n.x = x;
			n.y = y;
			cache.put(makeKey(x,y), n);
			if (DEBUG) System.out.println("added node to cache: " + makeKey(x,y) + ": " + n);
			return n;
		} else {
			//System.out.println("didn't add, already exist");
			return cache.get(makeKey(x,y));
		}
	}
	
	private String makeKey(int i, int j) {
		return i+","+j;
	}
	
	int explore(int[][] leftView, int[][] rightView) {
		currentLocationLeft = updateGraph(cacheLeft, leftView, x1, y1, r1);
		currentLocationRight = updateGraph(cacheRight, rightView, x2, y2, r2);
		
		if (exploreGoal == null || exploreGoal.size()==0) {
			exploreGoal = getFringe(!isLeftExitReachable());
			
			if (DEBUG) {
				System.out.println("Goal path generated");
				System.out.println(exploreGoal);
			}
		}
		
		int dir;
		
		if (exploreGoal==null || exploreGoal.size()==0) {
			// TODO - we can do something smarter than random. This seems to
			// happen after one player has exited.
			dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		//System.out.println(exploreGoal);
		Edge edge = exploreGoal.remove(0);
		dir = Utils.moveToShen(edge.move);
		
		/*
		if (isTwitching()) {
			if (DEBUG) System.out.println("***twitching, random walk!");
			exploreGoal = null;
			dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		*/
		
		int oldX1 = x1, oldY1 = y1, oldX2 = x2, oldY2 = y2;
		
		updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
		
		if (oldX1==x1 && oldY1 == y1 && oldX2 == x2 && oldY2 == y2) {
			if (DEBUG) System.out.println("***got stuck, random walk!");
			exploreGoal = null;
			// TODO - under what cases does this happen?
			dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		
		return dir;
	}

	private boolean isTwitching() {
		int MAX_CACHE = 10;
		if(leftTwitching.size() < MAX_CACHE || rightTwitching.size() < MAX_CACHE) {
			leftTwitching.add(new Pair<Integer,Integer>(x1, y1));
			rightTwitching.add(new Pair<Integer, Integer>(x2, y2));
			return false;
		}
			
		leftTwitching.remove(0);
		rightTwitching.remove(0);
		
		Pair<Integer, Integer> oldestLeft = leftTwitching.get(0);
		Pair<Integer, Integer> oldestRight = rightTwitching.get(0);
		int THRESHOLD = 1;
		for(int i = 0; i < MAX_CACHE; i++) {
			//if either player moved outside of the threshold even once, they are not twitching
			if((Math.abs(oldestLeft.getFront() - x1) > THRESHOLD && Math.abs(oldestLeft.getBack() - y1) > THRESHOLD)
				|| (Math.abs(oldestRight.getFront() - x2) > THRESHOLD && Math.abs(oldestRight.getBack() - y2) > THRESHOLD))
				return false;
		}
		return true;
	}
	
	/*
	 * TODO: this is buggy, x and y are not converted correctly, I'm running into weird collisions
	 *       in rightCache
	 */
	private void updateCentersAndExitStatus(int[][] leftView, int[][] rightView,
			int leftMid, int rightMid, int dx, int dy) {
		int rightEntity = rightView[rightMid + dy][rightMid + dx];
		int leftEntity = leftView[leftMid + dy][leftMid + dx];
		int space = Utils.entitiesToShen(Entity.SPACE);
		int exit = Utils.entitiesToShen(Entity.EXIT);
		
		if (DEBUG) System.out.println("dx: " + dx + ", dy: " + dy);
		if (DEBUG) System.out.println("left entity: " + Utils.shenToEntities(leftEntity));
		if (DEBUG) System.out.println("right entity: " + Utils.shenToEntities(rightEntity));
		
		if (leftEntity == space || leftEntity == exit) {
			x1 += dy;
			y1 += dx;
		}
		if (leftEntity == exit) {
			leftExited = true;
		}
		if (rightEntity == space || rightEntity == exit) {
			x2 += dy;
			y2 += dx;
		} 
		if (rightEntity == exit) {
			rightExited = true;
		}
	}

	// Random, but don't step on exit.
	private int exploreRandom(int[][] leftView, int[][] rightView) {
		int exit = Utils.entitiesToShen(Entity.EXIT);
		int dir = 0;
		int dx, dy;
		int leftMidX = leftView.length / 2;
		int leftMidY = leftView[0].length / 2;
		int rightMidX = rightView.length / 2;
		do {
			int[] deltas = { -1, 0, 1 };
			dx = deltas[(int) (Math.random() * 3)];
			dy = deltas[(int) (Math.random() * 3)];
		} while(leftView[leftMidX + dx][leftMidY + dy] == exit ||
				rightView[rightMidX + dx][rightMidX + dy] == exit);
		dir = Utils.moveToShen(Utils.dxdyToMove(dx, dy));
		if (DEBUG) {
			System.out.println(dir);
		}
		return dir;
	}
	
	//@Override
	public int lookAndMove(int[][] leftView, int[][] rightView) {
		int ret = -1;
		try {
			ret = doLookAndMove(leftView, rightView);
		} catch (Exception e) {
			e.printStackTrace();
			if (CRASH_ON_ERROR) {
				System.exit(1);
			} else {
				ret = (int) (Math.random() * NUM_MOVES) + 1;
			}
		}
		return ret;
	}

	private int doLookAndMove(int[][] leftView, int[][] rightView) {
		if (!radiiDiscovered) {
			if (G6Player.SID_DEBUG) {
				System.out.println("Player activated.");
			}
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
			radiiDiscovered = true;
		}
		
		updateKnowledge(left, x1, y1, leftView);
		updateKnowledge(right, x2, y2, rightView);
		
		int dir = -1;

		if (!leftExited && !rightExited) {
			dir = getMultiSolutionStep();
		}

		if (dir < 0) {
			if (leftExited || rightExited || isFullyExplored()) {
				dir = getSingleSolutionStep();
			}
			if (dir < 0) {
				return explore(leftView, rightView);
			}
		}

		int[] dirArray = MUMap.aintDToM[dir];
		int dx = dirArray[0];
		int dy = dirArray[1];

		updateCentersAndExitStatus(leftView, rightView, r1, r2, dx, dy);

		if (SID_DEBUG) {
			System.out.println(Utils.shenToMove(dir));
		}

		return dir;
	}

	private int getSingleSolutionStep() {
		// If there's no solution or old solution is completed.
		if (solution == null || solution.isCompleted()) {
			if (leftExited) {
				solution = solver.solve(right);
			} else if (rightExited) {
				solution = solver.solve(left);
			} else {
				// If we just need to solve one because
				// we've explored everything and haven't found a good solution.
				Solution leftSolution = solver.solve(right);
				Solution rightSolution = solver.solve(left);
				if (leftSolution.numTotalSteps() <= rightSolution.numTotalSteps()) {
					solution = leftSolution;
				} else {
					solution = rightSolution;
				}
			}
		}
		if (solution != null) {
			return solution.getNextStep();
		}
		return -1;
	}

	/*
	 * Shen's code does not show the player.
	 */
	private void addPlayerToView(int[][] view) {
		// 0-indexed, and add 1 for integer division ==> we can just divide by
		// 2 (view.length is always odd)
		int midX = view.length / 2;
		int midY = view[0].length / 2;
		view[midY][midX] = Utils.entitiesToShen(Entity.PLAYER);
	}
	
	/*
	 * Update the knowledge grids.
	 */
	private void updateKnowledge(int[][] knowledge, int x, int y, int[][] view) {
		addPlayerToView(view);
		
		int leftX = x - view.length / 2;
		int botY = y - view.length / 2;
		
		for (int i = 0; i < view.length; i++) {
			for (int j = 0; j < view[0].length; j++) {
				int newY = leftX + i;
				int newX = botY + j;
				
				if (newY >= 0 && newX >= 0 && newY < knowledge.length &&
						newX < knowledge[0].length) {
					knowledge[newY][newX] = view[i][j];
				}
			}
		}
	}
	
	//TODO:
	//  no accidental stepping on exit
	//  check if path is walkable: why stuck against wall?
	//  fix twitching behavior
	
	private LinkedList<Edge> getFringe(boolean prioritizeLeft) {
		
		Node main, other;
		
		if (prioritizeLeft) { 
			main = currentLocationLeft;
			other = currentLocationRight;
		} else {
			main = currentLocationRight;
			other = currentLocationLeft;
		}
		
		Queue<NodeWrapper> expanded = new LinkedList<NodeWrapper>();
		Set<NodeWrapper> visited = new HashSet<NodeWrapper>();
		
		List<Pair<Integer, LinkedList<Edge>>> paths = new ArrayList<Pair<Integer, LinkedList<Edge>>>();
		
		//Given main, bfs on main
		expanded.add(new NodeWrapper(main));
		visited.add(new NodeWrapper(main));
		
		if (DEBUG) System.out.println();
		if (DEBUG) System.out.println(main);
		while (!expanded.isEmpty()) {
			NodeWrapper cur = expanded.remove();
			
			if (DEBUG) System.out.print(".");
			
			if (cur.node.edges.size() < 8 && cur.node.entity == Entity.SPACE) {
				int uncoveredInOtherMap = squaresUncovered(other, cur.path);
				int obstaclesEncountered = obstaclesEncountered(other, cur.path);
				
				if (uncoveredInOtherMap > -1) {
					//TODO: apparently uncoveredInOtherMap alone is a horrible ranking heuristic on random maps
					//TODO: why 10? just putting an arbitrary value for now
					paths.add(new Pair<Integer, LinkedList<Edge>>(-WEIGHT_ON_PATH_LENGTH * cur.path.size() + WEIGHT_ON_SQUARES_UNCOVERED_BY_OTHER_PLAYER * uncoveredInOtherMap - obstaclesEncountered * WEIGHT_ON_KEEPING_ALIGNMENT, cur.path));
					
					if (paths.size() >= PATHS_TO_TRY_IN_EXPLORATION) {
						break;
					}
				}
			}
			
			for (Edge e : cur.node.edges) {
				
				if (e.to.entity == Entity.EXIT) {
					if (prioritizeLeft) {
						leftExitReachable = true;
					} else {
						rightExitReachable = true;
					}
					continue;
				}
				if (e.to.hashCode() == e.from.hashCode()) continue;
				
				NodeWrapper to = new NodeWrapper(e.to);
				if (!visited.contains(to)) {
					to.path.addAll(cur.path);
					to.path.add(e);
					expanded.add(to);
					visited.add(to);
				}
			}
		}
		
		if (paths.isEmpty()) {
			if (DEBUG) System.out.println("\nThis is bad, no paths found!");
			return null;
		}
		
		Collections.sort(paths);
		
		LinkedList<Edge> result = paths.get(0).getBack();
		
		if (DEBUG) {
			System.out.println();
			for (Edge e : result) {
				System.out.print(" => " + e.to.entity + "(" + e.to.x + "," + e.to.y + ")");
			}
			System.out.println();
		}
		
		return result;
	}
	
	/*
	 * return -1 if the path actually steps over an exit on the other map
	 */
	private int squaresUncovered(Node startingNode, List<Edge> path) {
		
		Node start = startingNode;
		int uncovered = 0;
		
		for (Edge e : path) {
			if (start.entity == Entity.EXIT) return -1;
			if (start.edges.size() < 8) uncovered++;
			
			boolean found = false;
			
			for (Edge e2 : start.edges) {
				if (e.move == e2.move) {
					start = e2.to;
					found = true;
					break;
				}
			}
			
			if (!found) {
				return uncovered;
			}
		}
		
		return uncovered;
	}
	
private int obstaclesEncountered(Node start, List<Edge> path) {
		
		int obstacles = 0;
		
		for (Edge e : path) {
			boolean found = false;
			
			for (Edge e2 : start.edges) {
				if (e.move == e2.move) {
					if (e2.to.hashCode() == start.hashCode()) obstacles++;
					start = e2.to;
					found = true;
					break;
				}
			}
			
			if (!found) {
				return obstacles;
			}
		}
		
		return obstacles;
	}
	
	private boolean areExitsFound() {
		// Exits can never be "unfound", so just cache our knowledge to avoid
		// unnecessary computation.
		if (!exitsFound) {
			exitsFound = isLeftExitFound() && isRightExitFound(); 
		}
		return exitsFound;
	}
	
	private boolean isLeftExitReachable() {
		if (!isLeftExitFound()) {
			if (DEBUG) System.out.println("Still not reachable 1");
			return false;
		}
		
		if (leftExitReachable) return true;
		
		Node start = currentLocationLeft;
		
		HashSet<Node> visited = new HashSet<Node>();
		Queue<Node> expanded = new LinkedList<Node>();
		expanded.add(start);
		visited.add(start);
		
		while (!expanded.isEmpty()) {
			Node fringe = expanded.remove();
			if (visited.contains(fringe)) continue;
			
			if (fringe.entity == Entity.EXIT) {
				leftExitReachable = true;
				return true;
			}
			
			visited.add(fringe);
			for (Edge e : fringe.edges) {
				expanded.add(e.to);
			}
		}
		
		if (DEBUG) System.out.println("Still not reachable 2");
		return false;
	}
	
	private boolean isRightExitReachable() {
		if (!isRightExitFound()) return false;
		
		if (rightExitReachable) return true;
		
		Node start = currentLocationRight;
		
		HashSet<Node> visited = new HashSet<Node>();
		Queue<Node> expanded = new LinkedList<Node>();
		expanded.add(start);
		visited.add(start);
		
		while (!expanded.isEmpty()) {
			Node fringe = expanded.remove();
			if (visited.contains(fringe)) continue;
			
			if (fringe.entity == Entity.EXIT) {
				rightExitReachable = true;
				return true;
			}
			
			visited.add(fringe);
			for (Edge e : fringe.edges) {
				expanded.add(e.to);
			}
		}
		
		return false;
	}
	
	private boolean isLeftExitFound() {
		if (!leftExitFound) {
			leftExitFound = isExitSeen(left);
		}
		return leftExitFound;
	}
	
	private boolean isRightExitFound() {
		if (!rightExitFound) {
			rightExitFound = isExitSeen(right);
		}
		return rightExitFound;
	}
	
	/*
	 * If the exit is in a particular view.
	 */
	private boolean isExitSeen(int[][] knowledge) {
		for(int i = 0; i < knowledge.length; i++) {
			for(int j = 0; j < knowledge[0].length; j++) {
				if(knowledge[i][j] == Utils.entitiesToShen(Entity.EXIT)) {
					return true;
				}
			}
		}
		return false;
	}

	private int getMultiSolutionStep() {
		if (areExitsFound()) {
			if (!shouldRecomputeSolution()) {
				if (solution == null) {
					return -1; 
				} else {
					return solution.getNextStep();
				}
			}
			if (!didExhaustiveCheck && isFullyExplored()) {
				didExhaustiveCheck = true;
				solution = solver.solve(left, right, Solver.MAX_CUTOFF_TIME,
						Solver.DEFAULT_MIN_DISTANCE, Solver.MAX_DISTANCE);
			} else {
				if (SID_DEBUG) {
					System.out.println(";;;;;");
					System.out.println(Arrays.deepToString(right));
					System.out.println("------");
					System.out.println(Arrays.deepToString(left));
				}
				solution = solver.solve(left, right);
			}
			
			if (solution != null) {
				return solution.getNextStep();
			}
		}
		return -1;
	}

	private class Node {
		public Entity entity;
		public int x;
		public int y;
		public HashSet<Edge> edges = new HashSet<Edge>();
		
		public String toString() {
			return entity.toString();
		}
		
		public int hashCode() {
			return x * 1000 + y;
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof Node)) return false;
			return x==((Node)other).x && y == ((Node)other).y;
		}
	}
	
	//This is used during bfs search
	private class NodeWrapper {
		public Node node;
		public LinkedList<Edge> path = new LinkedList<Edge>();
		public NodeWrapper(Node n) { node = n; }
		
		public int hashCode() {
			return node.hashCode();
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof NodeWrapper)) return false;
			return node.equals(((NodeWrapper)other).node);
		}
	}
	
	private class Edge {
		public Node from;
		public Node to;
		public Move move;
		public int hashCode() {
			return move.hashCode();
		}
		public boolean equals(Object other) {
			if (!(other instanceof Edge)) return false;
			return move == ((Edge)other).move;
		}
		public String toString() { return move.toString(); }
	}
}
