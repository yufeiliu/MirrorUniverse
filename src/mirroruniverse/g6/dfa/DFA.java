package mirroruniverse.g6.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import mirroruniverse.g6.Utils;
import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

public class DFA<V, T> {
	
	ArrayList<State<V, T>> goalStates;
	ArrayList<State<V, T>> states;
	State<V, T> startState;

	public DFA(int[][] map) {
		this();
		HashMap<String, State<Entity, Move>> allStates =
				new HashMap<String, State<Entity, Move>>(); 
		int xCap = map.length;
		int yCap = map[0].length;
		
		addStates(map, allStates, xCap, yCap);

		for (int x = 0; x < xCap; x++) {
			for (int y = 0; y < yCap; y++) {
				if (allStates.containsKey(makeKey(x, y))) {
					State<Entity, Move> node = allStates.get(makeKey(x, y));
					addTransitions(map, allStates, x, y, node);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addStates(int[][] map,
			HashMap<String, State<Entity, Move>> allStates, int xCap, int yCap) {
		for (int x = 0; x < xCap; x++) {
			for (int y = 0; y < yCap; y++) {
				if (map[x][y] == Utils.entitiesToShen(Entity.OBSTACLE))
					continue;
				
				Entity entity = Utils.shenToEntities(map[x][y]);
				boolean isStart = (entity == Entity.PLAYER);
				boolean isGoal = (entity == Entity.EXIT);
				boolean isKnown = (entity != Entity.UNKNOWN);
				if (isKnown) {
					State<Entity, Move> node = new State<Entity, Move>(entity, isGoal);
					allStates.put(makeKey(x, y), node);
					if (isStart) {
						startState = (State<V, T>) node;
					}
					if (node.isGoal()) {
						goalStates.add((State<V, T>) node);
					}
					states.add((State<V, T>) node);
				}
			}
		}
	}

	private void addTransitions(int[][] map,
			HashMap<String, State<Entity, Move>> allStates,
			int x, int y, State<Entity, Move> node) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) {
					continue;
				}
				String key = makeKey(x+dy, y+dx);
				if (!allStates.containsKey(key)) {
					node.addTransition(Utils.dxdyToMove(dx, dy), node);
				} else {
					State<Entity, Move> neighbor = allStates.get(key);
					node.addTransition(Utils.dxdyToMove(dx, dy), neighbor);
				}
			}
		}
	}
	
	private String makeKey(int x, int y) {
		return x + "," + y;
	}

	public DFA() {
		goalStates = new ArrayList<State<V, T>>();
		states = new ArrayList<State<V, T>>();
	}
	
	public State<V, T> addStartState(State<V, T> s) {
		startState = s;
		addState(s);
		return s;
	}
	
	public State<V, T> addState(State<V, T> s) {
		if (s.isGoal()) {
			goalStates.add(s);
		}
		states.add(s);
		return s;
	}
	
	public ArrayList<T> findShortestPath() {
		HashMap<State<V, T>, Transition<V, T>> used =
				new HashMap<State<V, T>, Transition<V, T>>();
		Queue<State<V, T>> q = new LinkedList<State<V, T>>();
		used.put(startState, null);
		q.add(startState);
		State<V, T> currentState;
		while(!q.isEmpty()) {
			currentState = q.poll();
			if (currentState.isGoal()) {
				return recoverPath(currentState, used);
			}
			for (Transition<V, T> t : currentState.getTransitions()) {				
				// self-transitions should not be part of shortest path
				if(t.getStart().equals(t.getEnd())) {
					continue;
				}
				State<V, T> nextState = t.getEnd();
				if (!used.containsKey(nextState)) {
					used.put(nextState, t);
					q.add(nextState);
				}
			}
		}
		return null;
	}
	
	protected ArrayList<T> recoverPath(State<V, T> currentState,
			HashMap<State<V, T>, Transition<V, T>> used) {
		ArrayList<T> path = new ArrayList<T>();
		Transition<V, T> trans = used.get(currentState);
		while (trans != null) {
			path.add(trans.getValue());
			currentState = trans.getStart();
			trans = used.get(currentState);
		}
		Collections.reverse(path);
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
	public static DFA<Entity, Move> intersect(
			DFA<Entity, Move> first, DFA<Entity, Move> other) {
		DFA<Entity, Move> intersection = new DFA<Entity, Move>();
		HashMap<String, State<Entity, Move>> newStates =
				new HashMap<String, State<Entity, Move>>();
		addIntersectionStates(first, other, intersection, newStates);
		addIntersectionTransitions(first, other, newStates);
		return intersection;
	}

	private static void addIntersectionStates(DFA<Entity, Move> first,
			DFA<Entity, Move> other, DFA<Entity, Move> intersection,
			HashMap<String, State<Entity, Move>> newStates) {
		for (State<Entity, Move> selfState : first.states) {
			for (State<Entity, Move> otherState : other.states) {
				// Don't accidentally step on an exit - these states cannot
				// be part of our solution
				if ((selfState.isGoal() && !otherState.isGoal()) ||
						(!selfState.isGoal() && otherState.isGoal())) {
					continue;
				}
				String key = makeKey(selfState, otherState);
				// This value 
				Entity e = selfState.getValue();
				State<Entity, Move> s = new State<Entity, Move>(
						e,
						selfState.isGoal() && otherState.isGoal(), key);
				newStates.put(key, s);
				
				// Add the state to the DFA
				if (selfState == first.startState &&
						otherState == other.startState) {
					intersection.addStartState(s);
				} else {
					intersection.addState(s);
				}
			}
		}
	}

	private static void addIntersectionTransitions(DFA<Entity, Move> first,
			DFA<Entity, Move> other,
			HashMap<String, State<Entity, Move>> newStates) {
		for (State<Entity, Move> selfState : first.states) {
			for (State<Entity, Move> otherState : other.states) {
				String startKey = makeKey(selfState, otherState);
				State<Entity, Move> source = newStates.get(startKey);
				// null if it's an exit for one of them
				if (source == null) {
					continue;
				}
				for (Transition<Entity, Move> selfTrans :
							selfState.getTransitions()) {
					for (Transition<Entity, Move> otherTrans :
							otherState.getTransitions()) {
						Move m = selfTrans.getValue();
						if (otherTrans.getValue() != m) {
							continue;
						};
						String endKey = makeKey(
								selfTrans.getEnd(), otherTrans.getEnd());
						State<Entity, Move> dest = newStates.get(endKey);
						// dest is null if it would have one exit
						if (dest != null) {
							source.addTransition(m, dest);
						}
					}
				}
			}
		}
	}

	private static String makeKey(State<Entity, Move> selfState,
			State<Entity, Move> otherState) {
		String key = selfState.getValue() + selfState.getId() + "; " +
				otherState.getValue() + otherState.getId();
		return key;
	}
	
	public boolean hasNonEmptyLanguage() {
		return !goalStates.isEmpty();
	}
	
	/*
	 * Returns a list of DFAs with the goals shifted one back.
	 */
	public ArrayList<DFA<V, T>> shiftGoals() {
		// TODO
		return null;
	}
	
	public String toString() {
		String s = "";
		s += "=====START STATE=====\n";
		s += startState + "\n";
		s += "\n=====GOAL STATES=====\n";
		for (State<V, T> state : goalStates) {
			s += state + "\n";
		}
		s += "\n=====ALL STATES=====\n";
		for (State<V, T> state : states) {
			s += state + "\n";
			for (Transition<V, T> t : state.getTransitions()) {
				s += "\t" + t;
			}
		}
		return s;
	}

}
