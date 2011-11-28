package mirroruniverse.g6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {

	private static final boolean DEBUG = false;
	
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
		/*
		int exit = Utils.entitiesToShen(Entity.EXIT);
		int dir = 0;
		int dx, dy;
		int leftMidY = leftView.length / 2;
		int leftMidX = leftView[0].length / 2;
		int rightMidY = rightView.length / 2;
		int rightMidX = rightView[0].length / 2;
		// Random, but don't step on exit.
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
		*/
		
		//If haven't set sight radii yet, set them
		if (r1==-1 || r2==-2) {
			r1 = (leftView.length-1) / 2;
			r1 = (rightView.length-1) / 2;
		}
		
		int dir = 0;
		
		System.out.println("=================");
		Utils.print2DArray(leftView);
		System.out.println();
		Utils.print2DArray(rightView);
		
		List<Pair<Integer, Integer>> possibilities = new ArrayList<Pair<Integer, Integer>>(); 
		
		int leftRelative = leftView.length / 2;
		int rightRelative = rightView.length / 2;
		
		//TODO: if no direction exists that uncovers squares, go to direction with most/least space
		//Loop over directions
		for (int i = 1; i <= 8; i++) {
			int[] curDir = MUMap.aintDToM[i];
			int dx = curDir[0];
			int dy = curDir[1];
			int newx1 = Math.min(Math.max(x1+dx, 0), MAX_MAP_SIZE - 1);
			int newy1 = Math.min(Math.max(y1+dy, 0), MAX_MAP_SIZE - 1);
			int newx2 = Math.min(Math.max(x2+dx, 0), MAX_MAP_SIZE - 1);
			int newy2 = Math.min(Math.max(y2+dy, 0), MAX_MAP_SIZE - 1);
			
			if (leftView[leftRelative + dy][leftRelative + dx] == Utils.entitiesToShen(Entity.SPACE) || 
					rightView[rightRelative + dy][rightRelative + dx] == Utils.entitiesToShen(Entity.SPACE)) {
				//TODO: remove duplicates
				possibilities.add(new Pair<Integer, Integer>(
						squaresUncovered(newx1, newy1, r1, left) + 
						squaresUncovered(newx2, newy2, r2, right), i));
			}
		}
		
		System.out.println("========");
		System.out.println("x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2);
		
		Collections.sort(possibilities);
		dir = possibilities.get(0).getBack();
		System.out.println("Squares to be uncovered: " + possibilities.get(0).getFront());
		
		int[] dirArray = MUMap.aintDToM[dir];
		int dx = dirArray[0];
		int dy = dirArray[1];
		
		System.out.println("Choose dir: (" + dirArray[0] + ", " + dirArray[1] + ")");
		
		if (leftView[leftRelative + dy][leftRelative + dx] == Utils.entitiesToShen(Entity.SPACE)) {
			x1 += dx;
			y1 += dy;
		}
		
		if (rightView[rightRelative + dy][rightRelative + dx] == Utils.entitiesToShen(Entity.SPACE)) {
			x2 += dx;
			y2 += dy;
		}
		
		return dir;
	}
	
	@Override
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
				knowledge[leftX + i][botY + j] = view[i][j];
			}
		}
	}
	
	private int squaresUncovered(int newX, int newY, int r, int[][] knowledge) {
		int counter = 0;
		
		for (int i = Math.max(newX - r, 0); i <= Math.min(newX + r, knowledge[0].length-1); i++) {
			for (int j = Math.max(newY - r, 0); j <= Math.min(newY + r, knowledge.length-1); j++) {
				counter += (knowledge[i][j] == Utils.entitiesToShen(Entity.UNKNOWN) ? 1 : 0);
			}
		}
		
		return counter;
	}
	
	private boolean areExitsFound() {
		// Exits can never be "unfound", so just cache our knowledge to avoid
		// unnecessary computation.
		if (!exitsFound) {
			exitsFound = isExitSeen(left) && isExitSeen(right);
		}
		return exitsFound;
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
			solution = solver.solve(left, right);
			
			System.out.println("Solution size: " + solution.length);
			
			solutionStep = 0;
			if (solution != null) {
				if (DEBUG) {
					System.out.println("solution");
					for (int i : solution) {
						System.out.println(Utils.shenToMove(i));
					}
				}
			}
		}
		if(solution != null) {
			return solution[solutionStep++];
		}
		
		return -1;
	}

}
