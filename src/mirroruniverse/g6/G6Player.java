package mirroruniverse.g6;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {
	
	//knowledge of whats at each map
	private int[][] left = new int[200][200];
	private int[][] right = new int[200][200];
	
	
	
	private int x1 = -999;
	private int y1;
	private int x2;
	private int y2;
	
	
	private int r1 = -1;
	private int r2 = -1;
	

	private int[] solution;
	private Solver solver;
	private int solutionStep;

	public G6Player() {
		for (int i = 0; i < 200; i++) {
			for (int j = 0; j < 200; j++) {
				left[i][j]=-1;
				right[i][j]=-1;
			}
		}
		
		solution = null;
		solver = new DFASolver();
	}

	//exploration strategy 
	public int explore(int[][] leftView, int[][] rightView) {
		if (r1 == -1 || r2 == -1) {
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
		}
		if(x1 == -999) {
			x1 = r1;
			y1 = r1;
			x2 = r2;
			y2 = r2;
			
			left[x1][y1]=0;
			right[x2][y2]=0;
		}
		
		updateKnowledge(left, x1, y1, leftView);
		updateKnowledge(right, x2, y2, rightView);
		
		
		int dir = 0;
		
		List<Pair<Integer, Integer>> possibilities = new ArrayList<Pair<Integer, Integer>>(); 
		
		//TODO: if no direction exists that uncovers squares, go to direction with most/least space
		//Loop over directions
		for (int i = 1; i <= 8; i++) {
			int[] curDir = MUMap.aintDToM[i];
			int dx = curDir[0];
			int dy = curDir[1];
			int newx1 = Math.max(x1+dx, 0);
			int newy1 = Math.max(y1+dy, 0);
			int newx2 = Math.max(x2+dx, 0);
			int newy2 = Math.max(y2+dy, 0);
			
			if (leftView[newx1][newy1] == Utils.entitiesToShen(Entity.SPACE) && 
					rightView[newx2][newy2] == Utils.entitiesToShen(Entity.SPACE)) {
				//TODO: remove duplicates
				possibilities.add(new Pair<Integer, Integer>(
						squaresUncovered(newx1, newy1, r1, left) + 
						squaresUncovered(newx2, newy2, r2, right)
						, i));
			}
		}
		
		Collections.sort(possibilities);
		dir = possibilities.get(0).getBack();
		
		int[] dirArray = MUMap.aintDToM[dir];
		x1 += dirArray[0];
		x2 += dirArray[0];
		y1 += dirArray[1];
		y2 += dirArray[1];
		
		return dir;
	}
	
	@Override
	public int lookAndMove(int[][] leftView, int[][] rightView) {
		//TODO: add logic for re-recomputing solution upon uncovering more fogged area
		
		if (solution == null && switchPhase(left, right)) {
			solution = solver.solve(left, right);
			solutionStep = 0;
		}
		if(solution != null) {
			return solution[solutionStep++];
		}
		return explore(leftView, rightView);
	}
	
	private void updateKnowledge(int[][] knowledge, int x, int y, int[][] view) {
		for (int i = (view.length - 1)/2; i < view.length; i++) {
			for (int j = (view[0].length-1)/2; j < view[0].length; j++) {
				knowledge[i- (view.length - 1)/2][j - (view[0].length-1)/2] = view[i][j];
			}
		}
	}
	
	private int squaresUncovered(int newX, int newY, int r, int[][] knowledge) {
		int counter = 0;
		
		for (int i = Math.max(newX - r, 0); i <= Math.min(newX + r, knowledge[0].length-1); i++) {
			for (int j = Math.max(newY - r, 0); j <= Math.min(newY + r, knowledge.length-1); j++) {
				counter += (knowledge[j][i] == -1 ? 1 : 0);
			}
		}
		
		return counter;
	}
	
	private boolean switchPhase(int[][] left, int[][] right) {
		//check for exit in both
		return isExitFoundIn(left) && isExitFoundIn(right);
	}
	
	private boolean isExitFoundIn(int[][] knowledge) {
		for(int i = 0; i < knowledge.length; i++) {
			for(int j = 0; j < knowledge[0].length; j++) {
				if(knowledge[i][j] == Utils.entitiesToShen(Entity.EXIT)) {
					return true;
				}
			}
		}
		return false;
	}

}
