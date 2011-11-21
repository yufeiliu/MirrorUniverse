package mirroruniverse.g5;


import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G5Player implements Player {
	
	//knowledge of whats at each map
	int[][] leftMapKnowledge;
	int[][] rightMapKnowledge;
	boolean didInitialize = false;
	boolean keepExploringLeft = true;
	boolean keepExploringRight = true;
	
	boolean leftMapExitFound = false;
	boolean rightMapExitFound = false;
	
	private int[] solution;
	private Solver solver;
	private int solutionStep;

	public G5Player() {
		solution = null;
		solver = new DFASolver();
	}
	
	public void initializeMapKnowledge(int[][] lMap, int[][] rMap) {
		
		//TODO: how the hell can we get dimensions of the maps? 
		leftMapKnowledge = new int[lMap.length][lMap[0].length];
		rightMapKnowledge = new int[rMap.length][rMap[0].length];
		
		//initialize the left map knowledge to all -1;
		//num of rows
		for(int i = 0; i < lMap.length; i++) {
			//System.out.println("rows: " + lMap.length);
			//num of cols
			for(int j = 0; j < lMap[0].length; j++) {
				//System.out.println("cols: " + lMap[0].length);
				leftMapKnowledge[i][j] = -1;
			}
		}
		
		//initalize the right map knowledge to all -1
		for(int i = 0; i < rMap.length; i++) {
			for(int j = 0; j < rMap[0].length; j++) {
				rightMapKnowledge[i][j] = -1;
			}
		}
		
		didInitialize = false;
	}
	
	//exploration strategy 
	public int[] Explore(int[][] leftMap, int[][] rightMap) {
		
		//stores which position a player should explore next
		//left map is index 0, right map is index 1
		int[] whichSpaceToMove = new int[2];
		
		//initialize explored arrays if empty
		if(!didInitialize)
			initializeMapKnowledge(leftMap, rightMap);
		
		//get the players current position
		//need to update this so that we can store accurate information of the exact x y location of a map
		//at the moment it refreshes and sees the boxes immediately adjacent to it and chooses which location to go
		//int[] currentPosition = myMap.getLocation();
		
		//check which squares havent been seen
		//to see the immediate adjacent square
		int[][] leftLocalView = new int[ 3 ][ 3 ];
		int intMidL = leftMap.length / 2;
		for ( int i = -1; i <= 1; i ++ )
		{
			for ( int j = -1; j <= 1; j ++ )
			{
				leftLocalView[ 1 + j ][ 1 + i ] = leftMap[ intMidL + j ][ intMidL + i ];
				//take note of whats on the adjacency squares
				//leftMapKnowledge[1+j][1+i] = leftMap[intMidL+j][intMidL+i];
			}
		}
		
		//find best spot to explore left map
		boolean lExplored = false;
		//traverse what spaces have been unexplored and chose best pick
		for(int i = 0; i < leftMapKnowledge.length; i++) {
			for(int j = 0; j < leftMapKnowledge[0].length; j++) {
				if(leftMapKnowledge[i][j] == 0) {
					lExplored = true;
					whichSpaceToMove[0] = MUMap.aintMToD[ j + 1 ][ i + 1 ];
				}
					
			}
		}
		
		//to see the immediate adjacent square
		int[][] rightLocalView = new int[ 3 ][ 3 ];
		int intMidR = rightMap.length / 2;
		for ( int i = -1; i <= 1; i ++ )
		{
			for ( int j = -1; j <= 1; j ++ )
			{
				rightLocalView[ 1 + j ][ 1 + i ] = rightMap[ intMidR + j ][ intMidR + i ];
				//take note of whats on the adjacency squares
				rightMapKnowledge[1+j][1+i] = rightMap[intMidR+j][intMidR+i];
			}
		}
		
		//find best spot to move right map
		boolean rExplored = false;
		//traverse what spaces have been unexplored and chose best pick
		for(int i = 0; i < rightMapKnowledge.length; i++) {
			for(int j = 0; j < rightMapKnowledge[0].length; j++) {
				if(rightMapKnowledge[i][j] == 0) {
					rExplored = true;
					whichSpaceToMove[1] = MUMap.aintMToD[ j + 1 ][ i + 1 ];
				}
					
			}
		}
		
		int rightUnknownSpaces = 0;
		int leftUnknownSpaces = 0;
		//check to see if we explored almost all unknowned squares
		for(int i = 0; i < leftMapKnowledge.length; i ++) {
			for(int j = 0; j < leftMapKnowledge.length; j++) {
				if(leftMapKnowledge[i][j] == -1)
					leftUnknownSpaces++;
			}
		}
		
		//if there is less than 20% left of unknown knowledge spaces than stop exploring
		//need to update this as this is not optimal
		if(leftUnknownSpaces <= (leftMap.length + leftMap[0].length) * .2)
			keepExploringLeft = false;
		
		for(int i = 0; i < rightMapKnowledge.length; i ++) {
			for(int j = 0; j < rightMapKnowledge.length; j++) {
				if(rightMapKnowledge[i][j] == -1) 
					rightUnknownSpaces++;
			}
		}
	
		if(rightUnknownSpaces <= (rightMap.length + rightMap[0].length) * .2)
			keepExploringRight = false;
		
		return whichSpaceToMove;
		
	}
	
	@Override
	public int lookAndMove(int[][] aintViewL, int[][] aintViewR) {
		int out = -1;
		
		int[] whichSpaceToMove = new int[2];
		
		//run the explore strategy first
		if(keepExploringRight || keepExploringLeft) {
			whichSpaceToMove = Explore(aintViewL, aintViewR);
			//return move for the left map
			return whichSpaceToMove[0]; //need to fix this logic
		}
		
		//TODO: add logic for re-recomputing solution upon uncovering more fogged area
		if (solution==null) {
			solution = solver.solve(leftMapKnowledge, rightMapKnowledge);
			solutionStep = 0;
		}
		
		return solution[solutionStep++];
	}

}
