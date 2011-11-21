package mirroruniverse.g6.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import mirroruniverse.g6.Utils;
import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

public class DFA<V, T> {
	
	// TODO - should use a different generic for the node value and for the
	// transitions...
	
	ArrayList<State<V, T>> goalStates;
	ArrayList<State<V, T>> states;
	State<V, T> startState;

	// TODO
	@SuppressWarnings("unchecked")
	public DFA(int[][] firstMap) {
		this();
		//Not an insurance company
		HashMap<String, State<Entity, Move>> allStates = new HashMap<String, State<Entity, Move>>(); 
		
		int x_cap = firstMap.length;
		int y_cap = firstMap[0].length;
		
		for (int x = 0; x < x_cap; x++) {
			for (int y = 0; y < y_cap; y++) {

				if (firstMap[x][y]==Utils.entitiesToShen(Entity.OBSTACLE)) continue;
				
				State<Entity, Move> node;
				if (allStates.containsKey(x+","+y)) {
					node = allStates.get(x+","+y);
				} else {
					node = new State<Entity, Move>(
							Utils.shenToEntities(firstMap[x][y]),
							firstMap[x][y]==Utils.entitiesToShen(Entity.EXIT));
					if (firstMap[x][y]== Utils.entitiesToShen(Entity.PLAYER)) {
						// This is janky
						startState = (State<V, T>) node;
					}
					allStates.put(x+","+y, node);
				}
				
				for (int dx = -1; dx <= 1; dx++) {
					
					for (int dy = -1; dy <= 1; dy++) {
						
						if (dx==0 && dy==0) continue;
						
						int new_x = x + dx, new_y = y + dy;
						
						//If out of bound, transition onto self
						if (new_x >= x_cap || new_x < 0 || new_y >= y_cap || new_y < 0 ||
								firstMap[x+dx][y+dy]==Utils.entitiesToShen(Entity.OBSTACLE)) {
							node.addTransition(new Transition<Entity, Move>(Utils.dxdyToMove(dx, dy),node,node));
						} else {
							State<Entity, Move> neighbor;
							if (allStates.containsKey((x+dx)+","+(y+dy))) {
								neighbor = allStates.get((x+dx)+","+(y+dy));
							} else {
								neighbor = new State<Entity, Move>(
										Utils.shenToEntities(
												firstMap[x+dx][y+dy]),
												firstMap[x+dx][y+dy] == Utils.entitiesToShen(Entity.EXIT)
										);
								if (firstMap[x+dx][y+dy]==Utils.entitiesToShen(Entity.PLAYER)) {
									startState = (State<V, T>) neighbor;
								}
								
								allStates.put((x+dx)+","+(y+dy), neighbor);
							}
							
							node.addTransition(new Transition<Entity, Move>(Utils.dxdyToMove(dx,dy), node, neighbor));
						}
					}
				}
				
			}
		}
		
	}
	
	public DFA() {
		goalStates = new ArrayList<State<V, T>>();
		states = new ArrayList<State<V, T>>();
	}
	
	public State<V, T> addStartState(State<V, T> s) {
		startState = s;
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
		
		for (State<Entity, Move> selfState : first.states) {
			for (State<Entity, Move> otherState : other.states) {
				// Don't accidentally step on an exit
				if ((selfState.isGoal() || !otherState.isGoal()) ||
						(!selfState.isGoal() || otherState.isGoal())) {
					continue;
				}
				String key = selfState.getValue() + ", " + otherState.getValue();
				// TODO - figure out what this should be if the value is ever
				// used?
				Entity e = selfState.getValue();
				State<Entity, Move> s = new State<Entity, Move>(
						selfState.getValue(),
						selfState.isGoal() && otherState.isGoal(), key);
				newStates.put(key, s);
				intersection.addState(s);
			}
		}
		
		HashMap<String, Transition<Entity, Move>> firstTransitionValues =
				new HashMap<String, Transition<Entity, Move>>();
		for (State<Entity, Move> selfState : first.states) {
			for (Transition<Entity, Move> t : selfState.getTransitions()) {
				firstTransitionValues.put(t.getId(), t);
			}
		}
		
		for (State<Entity, Move> otherState : other.states) {
			for (Transition<Entity, Move> t : otherState.getTransitions()) {
				Transition<Entity, Move> firstTransition =
						firstTransitionValues.get(t.getValue());
				if (firstTransition != null) {
					State<Entity, Move> start = newStates.get(
							firstTransition.getStart().getValue() + ", " +
							t.getStart().getValue());
					State<Entity, Move> end = newStates.get(
							firstTransition.getEnd().getValue() + ", " +
							t.getEnd().getValue());
					// These are null when exactly one of them was a goal state
					if (start != null && end != null) {
						start.addTransition(t.getValue(), end);
					}
				}
			}
		}
		return intersection;
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

}
