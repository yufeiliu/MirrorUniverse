package mirroruniverse.g6;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {

	public static final boolean DEBUG = false;
	
	private static final int MAX_MAP_SIZE = 100;
	private static final int INTERNAL_MAP_SIZE = MAX_MAP_SIZE * 2;
	
	// Stores maps.
	private int[][] left = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	private int[][] right = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	
	private ArrayList<Node> nodeGraphLeft = new ArrayList<Node>();
	private ArrayList<Node> nodeGraphRight = new ArrayList<Node>();
	
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
	
	private boolean radiiDiscovered;

	/*
	 * Array of moves for the solution. null if unsolved.
	 */
	private int[] solution;
	
	/*
	 * Solver used to provide the solution. Can be swapped out to use
	 * a different method if necessary.
	 */
	private Solver solver;
	
	/*
	 * Step of the solution which the player currently is on.
	 */
	private int solutionStep;

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

	public int explore(int[][] leftView, int[][] rightView) {
		if (!radiiDiscovered) {
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
		}
		
		if (nodeGraphLeft.size() == 0) {
			
			
		}
		
		int dir = 0;
		
		
		return dir;
	}

	private int pickDirFromPossibilities(
			ArrayList<Pair<Integer, Integer>> possibilitiesList) {
		int dir;
		// Choose randomly among dirs of equal score. This avoids infinite
		// loops.
		ArrayList<Integer> goodDirs = new ArrayList<Integer>();
		int maxScore = -1;
		for (Pair<Integer, Integer> p : possibilitiesList) {
			if (maxScore <= p.getFront()) {
				maxScore = p.getFront();
				goodDirs.add(p.getBack());
			}
		}
		dir = goodDirs.get((int) (Math.random() * goodDirs.size()));
		return dir;
	}

	private void addPossibleDir(int[][] leftView, int[][] rightView,
			Set<Pair<Integer, Integer>> possibilities, int leftMid,
			int rightMid, int i) {
		int[] curDir = MUMap.aintDToM[i];
		int dx = curDir[0];
		int dy = curDir[1];
		int space = Utils.entitiesToShen(Entity.SPACE);
		int newx1 = x1;
		int newy1 = y1;
		int newx2 = x2;
		int newy2 = y2;
		boolean leftUnblocked =
				(leftView[leftMid + dy][leftMid + dx] ==  space);
		boolean rightUnblocked = (rightView[rightMid + dy][rightMid + dx] ==  space);
//			rightUnblocked = leftUnblocked;
		
		// Only update newx and newy for unblocked directions
		if (leftUnblocked) {
			newx1 = Math.min(Math.max(x1+dy, 0), INTERNAL_MAP_SIZE - 1);
			newy1 = Math.min(Math.max(y1+dx, 0), INTERNAL_MAP_SIZE - 1);
		}
		if (rightUnblocked) {
			newx2 = Math.min(Math.max(x2+dy, 0), INTERNAL_MAP_SIZE - 1);
			newy2 = Math.min(Math.max(y2+dx, 0), INTERNAL_MAP_SIZE - 1);
		}
		
		// Don't make a move that will move neither player. If we don't
		// perform this check, we could perform a useless move if everything
		// ties for 0
		if (leftUnblocked || rightUnblocked) {
			int toUncover = 0;
			if (leftUnblocked && !isLeftExitFound()) {
				toUncover += squaresUncovered(newx1, newy1, r1, left);
			}
			if (rightUnblocked && !isRightExitFound()) {
				toUncover += squaresUncovered(newx2, newy2, r2, right);
			}
			possibilities.add(new Pair<Integer, Integer>(toUncover, i));
		}
	}

	private void updateCenters(int[][] leftView, int[][] rightView,
			int leftMid, int rightMid, int dx, int dy) {
		if (leftView[leftMid + dy][leftMid + dx] ==
				Utils.entitiesToShen(Entity.SPACE)) {
			x1 += dy;
			y1 += dx;
		}
		if (rightView[rightMid + dy][rightMid + dx] ==
				Utils.entitiesToShen(Entity.SPACE)) {
			x2 += dy;
			y2 += dx;
		}
	}

	// Random, but don't step on exit.
	private int exploreRandom(int[][] leftView, int[][] rightView) {
		int exit = Utils.entitiesToShen(Entity.EXIT);
		int dir = 0;
		int dx, dy;
		int leftMidY = leftView.length / 2;
		int leftMidX = leftView[0].length / 2;
		int rightMidY = rightView.length / 2;
		int rightMidX = rightView[0].length / 2;
		do {
			int[] deltas = { -1, 0, 1 };
			dx = deltas[(int) (Math.random() * 3)];
			dy = deltas[(int) (Math.random() * 3)];
		} while(leftView[leftMidY + dy][leftMidX + dx] == exit ||
				rightView[rightMidY + dy][rightMidX + dx] == exit);
		dir = Utils.moveToShen(Utils.dxdyToMove(dx, dy));
		if (DEBUG) {
			System.out.println(dir);
		}
		return dir;
	}
	
	//@Override
	public int lookAndMove(int[][] leftView, int[][] rightView) {
		updateKnowledge(left, x1, y1, leftView);
		updateKnowledge(right, x2, y2, rightView);
		
		
		int dir;		
		dir = getSolutionStep();
		if (dir > 0) {
			return dir;
		}
		dir = explore(leftView, rightView);
		return dir;
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
				// TODO - I think if this is ever false, there's a bug in the code
				// and this is false for the identical maps	
				if (leftX + i < knowledge.length && botY + j < knowledge[0].length) {
					knowledge[leftX + i][botY + j] = view[i][j];
				}
			}
		}
	}
	
	private int squaresUncovered(int newX, int newY, int r, int[][] knowledge) {
		int counter = 0;
		for (int i = Math.max(newX - r, 0); i <= Math.min(newX + r, knowledge[0].length-1); i++) {
			for (int j = Math.max(newY - r, 0); j <= Math.min(newY + r, knowledge.length-1); j++) {
				if (DEBUG) {
					System.out.println(i + ", " + j + "\t" +
							Utils.shenToEntities(knowledge[i][j]));
				}
				if (knowledge[i][j] == Utils.entitiesToShen(Entity.UNKNOWN)) {
					counter++;
				}
			}
		}
		
		if (DEBUG) {
			System.out.println(r);
			System.out.println(counter);
		}
		
		return counter;
	}
	
	private ArrayList<Edge> getFringe(ArrayList<Node> nodeGraph) {
		HashSet<Deque<Edge>> paths = new HashSet<Deque<Edge>>();
		
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
		while(firstFringe != null) {
			for(Deque<Edge> pathStack : paths) {
				//peek at top of each stack.
				Edge top = pathStack.peek();
				
				//check for fringe (fewer than 8 edges)
				if(top.to.entity == Entity.SPACE && top.to.edges.size() < 8) {
					//if fringe, return that stack.
					firstFringe = pathStack;
				} else {
					//else, copy stack and push new edges onto tops of each new one
					for(Edge edge : top.to.edges) {
						if(edge.to.x != edge.from.x && edge.to.y != edge.from.y) {
							Deque<Edge> newPathStack = new ArrayDeque<Edge>();
							newPathStack.addAll(pathStack);
							newPathStack.add(edge);
							paths.add(newPathStack);
						}
					}
				}
			}
		}
		
		ArrayList<Edge> edgeList = new ArrayList<Edge>(firstFringe);
		
		return edgeList;
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
		// TODO - continuously update solution with new knowledge.
		// native approach below is too slow (or moves us away from exit); at
		// the least, we can avoid recomputing if we've found a 0 solution or
		// see the entire map.
		/*
		if (areExitsFound(left, right)) {
			solution = solver.solve(right, left);
			if (solution != null) {
				return solution[0];
			}
		}
		*/
		
		if (solution == null && areExitsFound()) {
			solution = solver.solve(right, left);
			if (DEBUG) {
				System.out.println("Solution size: " + solution.length);
			}
			
			solutionStep = 0;
			if (solution != null) {
				if (DEBUG) {
					System.out.println("Solution: ");
					for (int i : solution) {
						System.out.print(Utils.shenToMove(i) + "\t");
					}
					System.out.println("");
				}
			}
		}
		// If solutionStep >= solution.length, the solution was invalid
		if(solution != null  && solutionStep < solution.length) {
			return solution[solutionStep++];
		} else if (solution != null && solutionStep >= solution.length) {
			System.err.println("Invalid solution provided.");
		}
		
		return -1;
	}

	private class Node {
		public Entity entity;
		public int x;
		public int y;
		public HashSet<Edge> edges;
	}
	
	private class Edge {
		public Node from;
		public Node to;
		public Move move;
	}
}
