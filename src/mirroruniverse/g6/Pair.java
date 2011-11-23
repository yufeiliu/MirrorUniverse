package mirroruniverse.g6;

public class Pair<T extends Comparable<T>, W> implements Comparable<Pair<T, W>> {

	private T front;
	private W back;
	
	public Pair(T aFront, W aBack) {
		this.front = aFront;
		this.back = aBack;
	}
	
	@SuppressWarnings("unused")
	public T getFront() { return front; }
	public W getBack() { return back; }
	
	//only compare on the front element
	public int compareTo(Pair<T, W> arg0) {
		return -1 * this.front.compareTo(arg0.front);
	}
	
}