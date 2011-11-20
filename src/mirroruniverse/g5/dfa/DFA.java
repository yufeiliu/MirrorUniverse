package mirroruniverse.g5.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class DFA<V> {
	
	ArrayList<State<V>> goalStates;
	ArrayList<State<V>> states;
	State<V> startState;

	// TODO
	public DFA(int[][] firstMap) {
		
	}
	
	private State<V> makeNewState(V value, boolean isGoal) {
		State<V> s = new State<V>(value, isGoal);
		if (isGoal) {
			goalStates.add(s);
		}
		states.add(s);
		return s;
	}
	
	// TODO
	public ArrayList<V> findShortestPath() {
		HashMap<State<V>, State<V>> used = new HashMap<State<V>, State<V>>();
		Queue<State<V>> q = new LinkedList<State<V>>();
		q.add(startState);
		State<V> currentState;
		while(!q.isEmpty()) {
			currentState = q.poll();
			if (currentState.isGoal()) {
				return recoverPath(currentState, used);
			}
			for (Transition<V> t : currentState.getTransitions()) {
				State<V> nextState = t.getEnd();
				if (used.get(nextState) != null) {
					q.add(nextState);
				}
			}
		}
		return null;
	}
	
	private ArrayList<V> recoverPath(State<V> currentState,
			HashMap<State<V>, State<V>> used) {
		ArrayList<V> path = new ArrayList<V>();
		while (currentState != null) {
			path.add(currentState.getValue());
			currentState = used.get(currentState);
		}
		Collections.reverse(path);
		return path;
	}

	// TODO
	public DFA<V> intersect(DFA<V> other) {
		return null;
	}
	
	public boolean hasNonEmptyLanguage() {
		return !goalStates.isEmpty();
	}
	
	/*
	 * Returns a list of DFAs with the goals shifted one back.
	 */
	public ArrayList<DFA<V>> shiftGoals() {
		return null;
	}

}
