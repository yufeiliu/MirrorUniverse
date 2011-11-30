package mirroruniverse.g6;


public abstract class Solver {
	
	public static final int MAX_DISTANCE = 10;
	
	abstract Solution solve(int[][] firstMap, int[][] secondMap);
	
	// Solveo one map after a player has exited
	abstract Solution solve(int[][] map);

	abstract Solution solve(int[][] firstMap, int[][] secondMap, int distance);

}
