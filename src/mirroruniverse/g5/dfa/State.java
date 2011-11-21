package mirroruniverse.g5.dfa;

import java.util.ArrayList;

public class State<V, T> {
	
	private V value;
	private ArrayList<Transition<V, T>> transitions;
	private boolean goal;
	private String id;
	
	private static int idCounter = 0; 
	
	public State(V value, boolean goal) {
		this.value = value;
		this.goal = goal;
		this.id = String.valueOf(idCounter++);
	}
	
	public State(V value, boolean goal, String id) {
		this.value = value;
		this.goal = goal;
		this.id = id;
	}

	public void addTransition(Transition<V, T> trans) {
		transitions.add(trans);
	}
	
	public void addTransition(T value, State<V, T> end) {
		transitions.add(new Transition<V, T>(value, this, end));
	}
	
	// NOTE - this value is not meaningful for intersected DFAs
	public V getValue() {
		return value;
	}
	
	public boolean isGoal() {
		return goal;
	}
	
	public String getId() {
		return this.id;
	}

	public ArrayList<Transition<V, T>> getTransitions() {
		return transitions;
	}
	
}
