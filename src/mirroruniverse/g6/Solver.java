package mirroruniverse.g6;


public abstract class Solver {
	
	public static final int MAX_DISTANCE = 20;
	public static final int MAX_CUTOFF_TIME = 60 * 1000;
	
	public static final int DEFAULT_MIN_DISTANCE = 0;
	public static final int DEFAULT_MAX_DISTANCE = 3;
	public static final long DEFAULT_CUTOFF_TIME = 10 * 1000;
	
	abstract Solution solve(int[][] firstMap, int[][] secondMap);
	
	// Solveo one map after a player has exited
	abstract Solution solve(int[][] map);

	abstract Solution solve(int[][] firstMap, int[][] secondMap, long cutoffTime,
			int minAttempts, int maxAttempts);

}
