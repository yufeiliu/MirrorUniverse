package mirroruniverse.g5;

import mirroruniverse.g5.Utils.Move;

public class RegexSolver extends Solver {

	@Override
	Move[] solveInternal(int[][] firstMap, int[][] secondMap) {
		String firstRegex = dfaToRegex((mapToDfa(firstMap)));
		String secondRegex = dfaToRegex((mapToDfa(secondMap)));
		String solution = findShortestIntersection(firstRegex, secondRegex);
		return stringToMoves(solution);
	}

	// TODO(sid)
	private Move[] stringToMoves(String solution) {
		return null;
	}

	// TODO(sid)
	private String findShortestIntersection(String firstRegex,
			String secondRegex) {
		return null;
	}

	// TODO(yufei), if necessary
	private String dfaToRegex(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	// TODO(yufei)
	private Object mapToDfa(int[][] firstMap) {
		return null;
	}

}
