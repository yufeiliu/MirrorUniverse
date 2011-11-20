package mirroruniverse.g5.dfa;

import java.util.ArrayList;

public class State<V> {
	
	private V value;
	private ArrayList<Transition<V>> transitions;
	private boolean goal;
	
	public State(V value, boolean goal) {
		this.value = value;
		this.goal = goal;
	}
	
	public void addTransition(Transition<V> trans) {
		transitions.add(trans);
	}
	
	public void addTransition(V value, State<V> end) {
		transitions.add(new Transition<V>(value, this, end));
	}
	
	public V getValue() {
		return value;
	}
	
	public boolean isGoal() {
		return goal;
	}

	public ArrayList<Transition<V>> getTransitions() {
		return transitions;
	}
	
}
