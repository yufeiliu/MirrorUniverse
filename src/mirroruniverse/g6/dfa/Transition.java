package mirroruniverse.g6.dfa;

public class Transition<V, T> {
	
	private State<V, T> end;
	private T value;
	private State<V, T> start;
	private String id;
	
	private static int idCounter = 0;
	
	public Transition(T value, State<V, T> start, State<V, T> end) {
		this.end = end;
		this.start = start;
		this.value = value;
		this.id = String.valueOf(idCounter++);
	}
	
	public T getValue() {
		return value;
	}
	
	public State<V, T> getStart() {
		return start;
	}

	public State<V, T> getEnd() {
		return end;
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return "Go " + value + " from (" + start + ") to (" + end + ")\n";
	}
}
