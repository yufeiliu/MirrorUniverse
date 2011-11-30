package mirroruniverse.g6;


public abstract class Solver {
	
	public static final int MAX_DISTANCE = 10;
	public static final int MAX_CUTOFF_TIME = 10 * 1000;
	
	abstract Solution solve(int[][] firstMap, int[][] secondMap);
	
	// Solveo one map after a player has exited
	abstract Solution solve(int[][] map);

	abstract Solution solve(int[][] firstMap, int[][] secondMap, long cutoffTime,
			int minAttempts, int maxAttempts);

}
