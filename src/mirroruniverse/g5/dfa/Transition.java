package mirroruniverse.g5.dfa;

public class Transition<V> {
	
	private State<V> end;
	private V value;
	private State<V> start;
	
	public Transition(V value, State<V> start, State<V> end) {
		this.end = end;
		this.start = start;
		this.value = value;
	}
	
	public V getValue() {
		return value;
	}
	
	public State<V> getStart() {
		return start;
	}

	public State<V> getEnd() {
		return end;
	}
	
}
