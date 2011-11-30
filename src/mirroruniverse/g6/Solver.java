package mirroruniverse.g6;


public abstract class Solver {
	
	abstract Solution solve(int[][] firstMap, int[][] secondMap);
	
	// Solveo one map after a player has exited
	abstract Solution solve(int[][] map);

}
