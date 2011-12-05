package mirroruniverse.g6;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {

	public static final boolean DEBUG = false;
	public static final boolean SID_DEBUG = false;
	private static final boolean CRASH_ON_ERROR = true;
	
	private static final int MAX_MAP_SIZE = 100;
	private static final int INTERNAL_MAP_SIZE = MAX_MAP_SIZE * 2;
	private static final double NUM_MOVES = 8;
	
	// Stores maps.
	private int[][] left = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	private int[][] right = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	
	private Node currentLocationLeft;
	private Node currentLocationRight;
	// kept for debugging purposes
	private int steps;
	
	/*
	 * The current location of each player within the 200x200 grid.
	 */
	private int x1, x2, y1, y2;
	
	/*
	 * sight radius on the left, and on the right
	 */
	private int r1 = -1, r2 = -1;
	
	/*
	 * True if the exists have been found. Used to cache this knowledge
	 * and avoid unnecessary computation.
	 */
	private boolean exitsFound;
	private boolean leftExitFound;
	private boolean rightExitFound;
	
	private boolean leftExited;
	private boolean rightExited;
	
	private int leftUnknown;
	private int rightUnknown;
	
	private boolean radiiDiscovered = false;
	private boolean closeToExit = false;

	private HashMap<String, Node> cacheLeft = new HashMap<String, Node>();
	private HashMap<String, Node> cacheRight = new HashMap<String, Node>();
	
	private LinkedList<Edge> exploreGoal = new LinkedList<Edge>();
	
	
	private ArrayList<Move> twitching = new ArrayList<Move>();
	
	/*
	 * Array of moves for the solution. null if unsolved.
	 */
	private Solution solution;
	
	/*
	 * Solver used to provide the solution. Can be swapped out to use
	 * a different method if necessary.
	 */
	private Solver solver;
	
	private boolean didExhaustiveCheck;
	private boolean computedSolutionWhenFullyExplored;

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
		leftUnknown = left.length * left[0].length;
		rightUnknown = right.length * right[0].length;
	}
	
	private boolean shouldNotRecomputeSolution() {		
		// recompute only if a certain number of squares have been uncovered
		// or if a player moves such that the exit is now in their sight radius.
		
		int newLeftUnknown = numSquaresUnknown(left);
		int newRightUnknown = numSquaresUnknown(right);
		boolean enoughUncovered = leftUnknown - newLeftUnknown > 5 || rightUnknown > newRightUnknown;
		leftUnknown = newLeftUnknown;
		rightUnknown = newRightUnknown;
		
		boolean closeEnough = (isExitInSight(left, x1, y1, r1)) || (isExitInSight(right, x2, y2, r2));
		boolean movedCloseEnough = !closeToExit && closeEnough;
		closeToExit = closeEnough;
		
		return !movedCloseEnough && !enoughUncovered && isFullyExplored();
	}
	
	private int numSquaresUnknown(int[][] knowledge) {
		int count = 0;
		for(int i = 0; i < knowledge.length; i++) {
			for(int j = 0; j < knowledge[0].length; j++) {
				if(knowledge[j][i] == Utils.entitiesToShen(Entity.UNKNOWN)) {
					count++;
				}
			}
		}
		return count;
	}
	
	private boolean isExitInSight(int[][] knowledge, int x, int y, int r) {
		for(int i = x - r; i < x + r + 1; i++) {
			for(int j = y - r; j < y + r + 1; j++) {
				if(knowledge[j][i] == Utils.entitiesToShen(Entity.EXIT)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isFullyExplored() {
		return isLeftFullyExplored() && isRightFullyExplored();
	}
	
	private boolean isRightFullyExplored() {
		return leftExitFound && isFullyExplored(left);
	}

	private boolean isLeftFullyExplored() {
		return rightExitFound && isFullyExplored(right);
	}


	// TODO - test this for correctness, cache
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
		
		//TODO lazy eval these
		int iMax = v.length - 1;
		int jMax = v[0].length - 1;
		int iMedian = v.length/2;
		int jMedian = v[0].length/2;
		
		Node cur = null;
		
		if (DEBUG) System.out.println("===================== " + x + "," + y);
		if (DEBUG) Utils.print2DArray(v);
		
		for (int i = 0; i < v.length; i++) {
			for (int j = 0; j < v[0].length; j++) {
				
				int curX = x1 + (i-iMedian);
				int curY = y1 + (j-jMedian);
				
				Node n = getFromCache(cache, v[i][j], curX, curY);
				if (Utils.shenToEntities(v[i][j]) == Entity.OBSTACLE || Utils.shenToEntities(v[i][j]) == Entity.EXIT) {
					continue;
				}
				
				for (int d = 1; d <= 8; d++) {
					int dj = MUMap.aintDToM[d][0];
					int di = MUMap.aintDToM[d][1];
					
					if (i + di <= iMax && i + di >= 0 &&
							j + dj <= jMax && j + dj >= 0 && curX + di >= 0 && curY + dj >= 0) {
						int neighborVal = v[i+di][j+dj];
						if (Utils.shenToEntities(neighborVal) == Entity.SPACE || Utils.shenToEntities(neighborVal) == Entity.EXIT) {
							Node neighbor = getFromCache(cache, v[i+di][j+dj], curX + di, curY + dj);
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
	
	public int explore(int[][] leftView, int[][] rightView) {
		if (!radiiDiscovered) {
			radiiDiscovered = true;
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
		}
		
		/*
		REMOVE_THIS--;
		if (REMOVE_THIS<=0) {
			int dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		*/
		
		
		currentLocationLeft = updateGraph(cacheLeft, leftView, x1, y1, r1);
		currentLocationRight = updateGraph(cacheRight, rightView, x2, y2, r2);
		
		if (exploreGoal == null || exploreGoal.size()==0) {
			//We always do it by left, then by right
			// TODO: optimize for both
			exploreGoal = getFringe(true);
			if (exploreGoal == null) {
				exploreGoal = getFringe(false);
			}
			
			if (DEBUG) System.out.println("Goal path generated");
			if (DEBUG) System.out.println(exploreGoal);
		}
		
		int dir;
		
		//TODO uh oh, getFringe failed, use random
		if (exploreGoal==null || exploreGoal.size()==0) {
			dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		//System.out.println(exploreGoal);
		Edge edge = exploreGoal.remove(0);
		dir = Utils.moveToShen(edge.move);
		
		if (isTwitching(dir)) {
			if (DEBUG) System.out.println("***twitching, random walk!");
			exploreGoal = null;
			dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		
		int oldX1 = x1, oldY1 = y1, oldX2 = x2, oldY2 = y2;
		
		updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
		
		if (oldX1==x1 && oldY1 == y1 && oldX2 == x2 && oldY2 == y2) {
			if (DEBUG) System.out.println("***got stuck, random walk!");
			exploreGoal = null;
			dir = exploreRandom(leftView, rightView);
			updateCentersAndExitStatus(leftView, rightView, leftView.length/2, rightView.length/2, MUMap.aintDToM[dir][0], MUMap.aintDToM[dir][1]);
			return dir;
		}
		
		return dir;
	}

	private boolean isTwitching(int dir) {
		twitching.add(Utils.shenToMove(dir));
		if (twitching.size()>4) twitching.remove(0);
		
		return (twitching.size()==4 && twitching.get(0)==twitching.get(2) && 
				twitching.get(1)==twitching.get(3) && Utils.reverseMove(twitching.get(0)) == twitching.get(1));
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
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
		}
		
		updateKnowledge(left, x1, y1, leftView);
		updateKnowledge(right, x2, y2, rightView);
		
		int dir;

		dir = getSolutionStep();

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

		steps++;
		return dir;
	}


	private boolean hasExit(int[][] leftView) {
		for (int i[] : leftView) {
			for (int j : i) {
				if (j == Utils.entitiesToShen(Entity.EXIT)) {
					return true;
				}
			}
		}
		return false;
	}


	private int getSingleSolutionStep() {
		// if there's no solution or old solution is completed
		if (solution == null || solution.isCompleted()) {
			if (leftExited) {
				solution = solver.solve(right);
			} else if (rightExited ){
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
		
		//Given main, bfs on main
		expanded.add(new NodeWrapper(main));
		visited.add(new NodeWrapper(main));
		
		if (DEBUG) System.out.println();
		if (DEBUG) System.out.println(main);
		while (!expanded.isEmpty()) {
			NodeWrapper cur = expanded.remove();
			
			if (DEBUG) System.out.print(".");
			if (cur.node.edges.size() < 8 && cur.node.entity == Entity.SPACE) {
				if (DEBUG) System.out.println("Target: " + cur.node.x + "," + cur.node.y);
				return cur.path;
			}
			
			for (Edge e : cur.node.edges) {
				
				if (e.to.entity == Entity.EXIT) continue;
				
				NodeWrapper to = new NodeWrapper(e.to);
				if (!visited.contains(to)) {
					to.path.addAll(cur.path);
					to.path.add(e);
					expanded.add(to);
					visited.add(to);
				}
			}
		}
		
		return null;
	}
	
	//TODO make it return null if no fringe is found
	private ArrayList<Edge> getFringe(Collection<Node> nodeGraph) {
		ArrayList<Deque<Edge>> paths = new ArrayList<Deque<Edge>>();
		HashSet<Edge> visited = new HashSet<Edge>();
		int oldSize = 0;
		
		Deque<Edge> firstFringe = null;
		
		//add first edges to stacks
		for(Node node : nodeGraph) {
			for(Edge edge : node.edges) {
				if(edge.to.x != edge.from.x && edge.to.y != edge.from.y) {
					Deque<Edge> pathStack = new ArrayDeque<Edge>();
					pathStack.add(edge);
					paths.add(pathStack);
				}
			}
		}
		
		//TODO: what do we do if the fringe cannot be found?
		search: while (firstFringe == null) {
			for(int i = 0; i < paths.size(); i++) {
				Deque<Edge> pathStack = paths.get(i);
				//System.out.println("\nstart path");
				//for(Edge e : pathStack)
				//	System.out.println(e.from.x+" "+e.from.y+" "+e.from.entity+" : "+e.to.x+" "+e.to.y+" "+e.to.entity);
				///.println("end path");
				//peek at top of each stack.
				Edge top = pathStack.peek();
				
				visited.add(top);
				if(Math.abs(visited.size() - oldSize) < 1) {
					//System.out.println("delta 0");
					return null;
				}
				oldSize = visited.size();
				
				//check for fringe (fewer than 8 edges)
				if(top.to.entity == Entity.SPACE && top.to.edges.size() < 8) {
					//if fringe, return that stack.
					//System.out.println("fringe found.");
					firstFringe = pathStack;
					System.out.println("Target: " + top.to.x + ", " + top.to.y);
					break search;
				} else {
					//else, copy stack and push new edges onto tops of each new one
					paths.remove(i);
					if(top.to.entity != Entity.OBSTACLE) {
						//System.out.println(top.to.x+" "+top.to.y+" "+top.to.entity);
						for(Edge edge : top.to.edges) {
							if(edge.to.entity != Entity.OBSTACLE && edge.to.x != edge.from.x && edge.to.y != edge.from.y) {
								Deque<Edge> newPathStack = new ArrayDeque<Edge>();
								newPathStack.addAll(pathStack);
								newPathStack.add(edge);
								paths.add(newPathStack);
							}
						}
					}
				}
			}
		}
		
		return new ArrayList<Edge>(firstFringe);
	}
	
	private boolean areExitsFound() {
		// Exits can never be "unfound", so just cache our knowledge to avoid
		// unnecessary computation.
		if (!exitsFound) {
			exitsFound = isLeftExitFound() && isRightExitFound(); 
		}
		return exitsFound;
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

	private int getSolutionStep() {
		return getSolutionStepExpensive();
		
//		return getSolutionStepSingle();
	}

	private int getSolutionStepSingle() {
		if (solution == null && areExitsFound()) {
			solution = solver.solve(right, left);
			if (solution != null) {
				if (DEBUG) {
					System.out.println(solution);
					System.out.println("Solution size: " + solution.numTotalSteps());
					System.out.println("Solution diff: " + solution.getDiff());
				}
			}
		}
		// If solutionStep >= solution.length, the solution was invalid
		if(solution != null) {
			return solution.getNextStep();
		}
		return -1;
	}

	private int getSolutionStepExpensive() {
		if (areExitsFound()) {
			if (shouldNotRecomputeSolution()) {
				return solution.getNextStep();
			}
			if (!didExhaustiveCheck && isFullyExplored()) {
				didExhaustiveCheck = true;
				solution = solver.solve(right, left, Solver.MAX_CUTOFF_TIME,
						Solver.DEFAULT_MIN_DISTANCE, Solver.MAX_DISTANCE);
			} else {
				if (SID_DEBUG) {
					System.out.println(";;;;;");
					System.out.println(Arrays.deepToString(right));
					System.out.println("------");
					System.out.println(Arrays.deepToString(left));
				}
				solution = solver.solve(right, left);
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
		public String toString() { return move.toString(); }
	}
}
