package mirroruniverse.g6;


public abstract class Solver {
	
	public static final int MAX_DISTANCE = 100;
	public static final int MAX_CUTOFF_TIME = 20 * 1000;
	
	protected static final int DEFAULT_MIN_ATTEMPTS = 0;
	protected static final int DEFAULT_MAX_ATTEMPTS = 10;
	protected static final long DEFAULT_CUTOFF_TIME = 10 * 1000;
	
	abstract Solution solve(int[][] firstMap, int[][] secondMap);
	
	// Solveo one map after a player has exited
	abstract Solution solve(int[][] map);

	abstract Solution solve(int[][] firstMap, int[][] secondMap, long cutoffTime,
			int minAttempts, int maxAttempts);

}
