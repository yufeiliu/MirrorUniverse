package mirroruniverse.g6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {

	public static final boolean DEBUG = false;
	public static final boolean SID_DEBUG = false;
	
	private static final int MAX_MAP_SIZE = 100;
	private static final int INTERNAL_MAP_SIZE = MAX_MAP_SIZE * 2;
	
	// Stores maps.
	private int[][] left = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	private int[][] right = new int[INTERNAL_MAP_SIZE][INTERNAL_MAP_SIZE];
	
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
		// TODO (Yufei or Hans) implement
		// add something to recompute if we're about to step on an exit, etc.
		return isFullyExplored();
	}
	
	private boolean isFullyExplored() {
		// TODO (Yufei or Hans) implement
		return false;
	}

	public int explore(int[][] leftView, int[][] rightView) {
//		return exploreRandom(leftView, rightView);
		int dir = 0;
		
		if (DEBUG) {
			System.out.println("=================");
			Utils.print2DArray(leftView);
			System.out.println();
			Utils.print2DArray(rightView);
		}
		
		// TODO - Use a set to avoid duplicates. I added this because there was
		// a TODO to avoid duplicates, but I'm not sure how it's possible to
		// get duplicates. Change this back to a list if it's not.
		Set<Pair<Integer, Integer>> possibilities =
				new HashSet<Pair<Integer, Integer>>();
		
		// TODO: if no direction exists that uncovers squares, go to direction
		// with most/least space
		
		// Iterate over directions
		for (int i = 1; i <= 8; i++) {
			addPossibleDir(leftView, rightView, possibilities, r1, r2, i);
		}
		
		if (DEBUG) {
			System.out.println("x1: " + x1 + " y1: " + y1 + " x2: " + x2 +
						" y2: " + y2);
		}
		
		ArrayList<Pair<Integer, Integer>> possibilitiesList =
				new ArrayList<Pair<Integer, Integer>>(possibilities);
		Collections.sort(possibilitiesList);
		
		dir = pickDirFromPossibilities(possibilitiesList);
		
		if (DEBUG) {
			System.out.println("Squares to be uncovered: " +
					possibilitiesList.get(0).getFront());
		}

		if (DEBUG) {
			System.out.println(Utils.shenToMove(dir));
		}

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
		if (!radiiDiscovered) {
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
		}
		
		updateKnowledge(left, x1, y1, leftView);
		updateKnowledge(right, x2, y2, rightView);

		int dir;
//		System.out.println("getting sol");
		dir = getSolutionStep();
//		System.out.println("got sol");
		if (dir < 0) {
			dir = explore(leftView, rightView);
		}

		
		int[] dirArray = MUMap.aintDToM[dir];
		int dx = dirArray[0];
		int dy = dirArray[1];
		
		updateCenters(leftView, rightView, r1, r2, dx, dy);

		if (SID_DEBUG) {
			System.out.println(Utils.shenToMove(dir));
		}
		
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
				// TODO - this sometimes has a negative or otherwise invalid index
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
			if (DEBUG) {
				System.out.println("Solution size: " + solution.numTotalSteps());
				System.out.println("Solution diff: " + solution.getDiff());
			}
			
			if (solution != null) {
				if (DEBUG) {
					System.out.println(solution);
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
			if (solution != null &&
					(solution.getDiff() == 0 || shouldRecomputeSolution())) {
				return solution.getNextStep();
			}
			solution = solver.solve(right, left);
			if (solution != null) {
				return solution.getNextStep();
			}
		}
		return -1;
	}

}
