package mirroruniverse.g6.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import mirroruniverse.g6.G6Player;
import mirroruniverse.g6.Solution;
import mirroruniverse.g6.Utils;
import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

public class DFA {
	
	private ArrayList<State> goalStates;
	private ArrayList<State> states;
	private Map<String, State> stateMap;
	private State startState;
	private static final int THRESHOLD_PRODUCT_DIST = 100;
	private static final boolean ENABLE_FALSIFICATION = true;
	private static final int THRESHOLD_DIST = 20;

	
	public DFA() {
		goalStates = new ArrayList<State>();
		states = new ArrayList<State>();
		stateMap = new HashMap<String, State>();
	}
	
	public DFA(int[][] map) {
		this();
		HashMap<String, State> allStates = new HashMap<String, State>();
		HashSet<String> blockedStates = new HashSet<String>();
		
		int xCap = map.length;
		int yCap = map[0].length;
		
		addStates(map, allStates, blockedStates, xCap, yCap);

		for (String k : allStates.keySet()) {
			State node = allStates.get(k);
			if (node != null) {
				int[] xy = recoverKey(k);
				addTransitions(map, allStates, blockedStates, xy[0], xy[1], node);
			}
		}
	}

	private void addStates(int[][] map,
			HashMap<String, State> allStates, HashSet<String> blockedStates,
			int xCap, int yCap) {
		for (int x = 0; x < xCap; x++) {
			for (int y = 0; y < yCap; y++) {
				addStateFromEntity(map, allStates, blockedStates,
						x, y);
			}
		}
	}

	private void addStateFromEntity(int[][] map,
			HashMap<String, State> allStates, HashSet<String> blockedStates,
			int x, int y) {
		Entity entity = Utils.shenToEntities(map[x][y]);
		boolean isStart = (entity == Entity.PLAYER);
		boolean isGoal = (entity == Entity.EXIT);
		boolean isUnknown = (entity == Entity.UNKNOWN);
		boolean isBlocked = (entity == Entity.OBSTACLE);
		
		if (isBlocked) {
			blockedStates.add(makeKey(x, y));
			return;
		}
		
		if (isUnknown) {
			return;
		}

		State node = new State(entity, isGoal);
		allStates.put(makeKey(x, y), node);
		
		if (isStart) {
			addStartState(node);
		} else {
			addState(node);
		}
	}

	private void addTransitions(int[][] map, HashMap<String, State> allStates,
			HashSet<String> blockedStates, int x, int y, State node) {
		for (byte dx = -1; dx <= 1; dx++) {
			for (byte dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) {
					continue;
				}
				
				String key = makeKey(x + dy, y + dx);
				Move move = Utils.dxdyToMove(dx, dy);
				State end = allStates.get(key);
				if (end != null) {
					node.addTransition(move, end);
				} else if (blockedStates.contains(key)) {
					node.addTransition(move, node);
				}
				
			}
		}
	}
	
	private String makeKey(int x, int y) {
		return x + "," + y;
	}


	private static int[] recoverKey(String k) {
		String[] split = k.split(",");
		int[] xy = new int[2];
		xy[0] = Integer.parseInt(split[0]);
		xy[1] = Integer.parseInt(split[1]);
		return xy;
	}
	
	public void addStartState(State s) {
		startState = s;
		addState(s);
	}
	
	public void addState(State s) {
		if (s.isGoal()) {
			goalStates.add(s);
		}
		states.add(s);
		stateMap.put(s.getId(), s);
	}
	
	public State getState(String k) {
		return stateMap.get(k);
	}
	
	public static boolean isPartialGoal(DFA first, DFA other, State selfState, State otherState) {
		return selfState.isBanned() || otherState.isBanned() ||
				(selfState.isGoal() && !otherState.isGoal() &&
						selfState != first.startState) ||
				(!selfState.isGoal() && otherState.isGoal() &&
							otherState != other.startState);
	}
	
	/*
	 * Optimized shortest path algorithm for
	 * the intersection of two DFAs.
	 * 
	 * TODO - cap exploration
	 */
	public static Solution findShortestPathBFS(DFA first, DFA other, int offset) {
		
		ArrayList<Move> firstSol = first.findShortestPath();
		ArrayList<Move> otherSol = other.findShortestPath();
		
		int firstDist = firstSol.size(); 
		int otherDist = otherSol.size();
		int productDist = firstDist * otherDist;
		boolean isFake = false;
		if (ENABLE_FALSIFICATION) {
			if (productDist > THRESHOLD_PRODUCT_DIST ||
					firstDist > THRESHOLD_DIST ||
					otherDist > THRESHOLD_DIST) {
				isFake = true;
				falsifyExit(first, firstSol,
						(int) Math.min(firstDist, Math.sqrt(THRESHOLD_PRODUCT_DIST)));
				falsifyExit(other, otherSol, 
						(int) Math.min(otherDist, Math.sqrt(THRESHOLD_PRODUCT_DIST)));
			}
		}
		
		// States to evaluate. They should return in priority order. However,
		// since we're doing a BFS, priority order is just the order in which
		// we insert things into the queue.
		Queue<State> openSet = new LinkedList<State>();
		
		// Used to backtrack.
		HashMap<State, Transition> cameFrom = new HashMap<State, Transition>();
		
		// Maps a State ID to the pair of states it contains. The keys represent
		// states that have been examined.
		HashSet<String> states = new HashSet<String>();
		
		String key = makeKey(first.startState, other.startState);
		
		openSet.add(new State(
				null,
				first.startState.isGoal() && other.startState.isGoal(),
				key));
		states.add(key);
		
		while (!openSet.isEmpty()) {
			
			if (firstDist == 1) {
				@SuppressWarnings("unused")
				boolean ig = first.startState.isGoal();
			}
			
			State current = openSet.poll();
			if (current.isGoal()) {
				ArrayList<Move> steps = recoverPath(current, cameFrom);
				return new Solution(steps, offset, isFake);
			}
			// Iterate through different moves.
			for (int i = 0; i < 8; i++) {
				String[] pairKeys = current.getId().split(";");
				Transition firstTrans = first.getState(pairKeys[0]).getTransitions()[i];
				Transition otherTrans = other.getState(pairKeys[1]).getTransitions()[i];
				
				if (firstTrans != null && otherTrans != null) {
					State firstDest = firstTrans.end;
					State otherDest = otherTrans.end;
					String destKey = makeKey(firstDest, otherDest);			
					
					if (states.contains(destKey) ||
							isPartialGoal(first, other, firstDest, otherDest)) {
						continue;
					}
					
					State next = new State(
							null,
							firstDest.isGoal() && otherDest.isGoal(),
							destKey);
					cameFrom.put(next, new Transition(
							/* firstTrans val is the same as otherTrans val. */
							firstTrans.value,
							current,
							next));
					states.add(destKey);
					openSet.add(next);
				}
			}
		}
		return null;
	}
	
	private static void falsifyExit(DFA dfa, ArrayList<Move> sol,
			int targetDist) {
		
		State current = dfa.getStartState();
		for (int i = 0; i < sol.size() - 1 && i < targetDist; i++) {
			int transitionIndex = Utils.moveToShen(sol.get(i)) - 1;
			current = current.getTransitions()[transitionIndex].getEnd();
		}
		for (State gs : dfa.goalStates) {
			gs.setGoal(false);
			gs.setBanned(true);
		}
		dfa.goalStates.clear();
		current.setGoal(true);
		dfa.goalStates.add(current);
	}

	public ArrayList<Move> findShortestPath() {
		HashMap<State, Transition> used = new HashMap<State, Transition>();
		Queue<State> q = new LinkedList<State>();
		
		// This indicates that no transition points to the startState.
		used.put(startState, null);
		
		q.add(startState);
		while(!q.isEmpty()) {
			State currentState = q.poll();
			if (currentState.isGoal()) {
				return recoverPath(currentState, used);
			}
			for (Transition t : currentState.getTransitions()) {
				// This happens after an intersection, where the transition
				// would go to a state with one exit.
				if (t == null) {
					continue;
				}
				// We never want to repeat states in our shortest path. This
				// includes avoiding self-transitions.
				State nextState = t.getEnd();
				if (!used.containsKey(nextState)) {
					used.put(nextState, t);
					q.add(nextState);
				}
			}
		}
		return null;
	}
	
	protected static ArrayList<Move> recoverPath(State currentState,
			HashMap<State, Transition> used) {
		ArrayList<Move> path = new ArrayList<Move>();
		ArrayList<Transition> transPath; 
		if (G6Player.SID_DEBUG_VERBOSE) {
			transPath = new ArrayList<Transition>();
		}
		Transition trans = used.get(currentState);
		while (trans != null) {
			path.add(trans.getValue());
			currentState = trans.getStart();
			if (G6Player.SID_DEBUG_VERBOSE) {
				transPath.add(trans);
			}
			trans = used.get(currentState);
		}
		Collections.reverse(path);
		if (G6Player.SID_DEBUG_VERBOSE) {
			Collections.reverse(transPath);
			System.out.println(transPath);
		}
		return path;
	}

	/*
	 * Creates a DFA with new states, one for every pair of states in the two
	 * original DFAs. A state is a goal state in the new DFA if and only if 
	 * both states in the pair were goal states.
	 * 
	 * There is a transition from state (A, B) to (C, D) on symbol x if and
	 * only if there was a transition on x from A to C and a transition x from
	 * C to D.
	 * 
	 * The result is a DFA that accepts the intersection of the two original
	 * DFA.
	 */
	public static DFA intersect(DFA first, DFA other) {
		DFA intersection = new DFA();
		HashMap<String, State> newStates = new HashMap<String, State>();
		
		long startTime = 0, stateEndTime, transEndTime;
		
		if (G6Player.SID_DEBUG) {
			 startTime = System.currentTimeMillis();
		}
		
		addIntersectionStates(first, other, intersection, newStates);
		if (G6Player.DEBUG) {
			stateEndTime = System.currentTimeMillis();
		}
		
		addIntersectionTransitions(first, other, newStates);
		if (G6Player.DEBUG) {
			transEndTime = System.currentTimeMillis();
		}
		
		if (G6Player.DEBUG) {
			System.out.println("Intersection: " + (stateEndTime - startTime));
			System.out.println("Transitions: " + (transEndTime - stateEndTime));
		}
		
		return intersection;
	}

	private static void addIntersectionStates(DFA first, DFA other,
			DFA intersection, HashMap<String, State> newStates) {
		for (State selfState : first.states) {
			for (State otherState : other.states) {
				// Don't accidentally step on an exit - these states cannot
				// be part of our solution
				
				// TODO - handle cases where these aren't real exits; need
				// states to have an isExit field. Currently, we might miss
				// non-perfect solutions.
				if ((selfState.isGoal() && !otherState.isGoal() &&
						selfState != first.startState) ||
							(!selfState.isGoal() && otherState.isGoal() &&
							otherState != other.startState)) {
					continue;
				}
				
				String key = makeKey(selfState, otherState);
				State s = new State(
						/* Entity isn't meaningful. */ null,
						selfState.isGoal() && otherState.isGoal(),
						key);
				newStates.put(key, s);
				
				// Add the state to the DFA
				if (selfState == first.startState && otherState == other.startState) {
					intersection.addStartState(s);
				} else {
					intersection.addState(s);
				}
			}
		}
	}

	private static void addIntersectionTransitions(DFA first, DFA other,
			HashMap<String, State> newStates) {
		for (State selfState : first.states) {
			Transition[] selfTransArray = selfState.getTransitions();
			for (State otherState : other.states) {
				String startKey = makeKey(selfState, otherState);
				State source = newStates.get(startKey);
				// null if it's an exit for one of them
				if (source == null) {
					continue;
				}
				Transition[] otherTransArray = otherState.getTransitions();
				for (int i = 0; i < State.NUM_DIRECTIONS; i++) {
					Transition selfTrans = selfTransArray[i]; 
					Transition otherTrans = otherTransArray[i];
					
					// Just for tests.
					if (selfTrans == null || otherTrans == null)
						continue;
					
					String endKey = makeKey(selfTrans.getEnd(), otherTrans.getEnd());
					State dest = newStates.get(endKey);
					// dest is null if it would have one exit
					if (dest != null) {
						// trans value is the same for both transitions.
						source.addTransition(selfTrans.getValue(), dest);
					}
				}
			}
		}
	}

	private static String makeKey(State selfState, State otherState) {
		return selfState.getId() + ";" + otherState.getId();
//		String key = selfState.getValue() + selfState.getId() + "; " +
//				otherState.getValue() + otherState.getId();
//		return key;
	}
	
	/*
	 * Returns a list of DFAs with the goals shifted one back.
	 */
	public DFA shiftGoals() {
		DFA other = new DFA();
		HashMap<State, State> copiedStates = new HashMap<State, State>();

		// Copy states. Nothing is set a goal state.
		for (State s : states) {
			boolean isStart = (s == startState);
			State newS = new State(s.getValue(), false);
			copiedStates.put(s, newS);
			if (isStart) {
				other.addStartState(newS);
			} else {
				other.addState(newS);
			}
		}
		
		// Copy transitions and make appropriate states goal states.
		for (State s : states) {
			State newS = copiedStates.get(s);
			for (Transition t : s.getTransitions()) {
				if (t == null) {
					continue;
				}
				State dest = copiedStates.get(t.getEnd());
				newS.addTransition(t.getValue(), dest);
				// TODO: we really want to check !s.isExit, not s.isGoal
				if (!s.isGoal() && t.getEnd().isGoal() && !newS.isGoal()) {
					newS.setGoal(true);
					other.goalStates.add(newS);
				}
			}
		}
		return other;
	}
	
	public String toString() {
		String s = "";
		s += "=====START STATE=====\n";
		s += startState + "\n";
		s += "\n=====GOAL STATES=====\n";
		for (State state : goalStates) {
			s += state + "\n";
		}
		s += "\n=====ALL STATES=====\n";
		for (State state : states) {
			s += state + "\n";
			for (Transition t : state.getTransitions()) {
				s += "\t" + t;
			}
		}
		return s;
	}
	
	public State getStartState() {
		return startState;
	}

}
