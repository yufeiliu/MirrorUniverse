package mirroruniverse.g2.astar;

import java.util.LinkedList;
import java.util.List;

import mirroruniverse.g2.Config;
import mirroruniverse.g2.Map;
import mirroruniverse.g2.Position;
import mirroruniverse.g2.astar.AStar.Node;
import mirroruniverse.sim.MUMap;

public class BacktrackingAStar extends AStar<State> {

	private State goalState;
	private Node bestNode;
	private double steps;
	private double bestH;
	private Map leftMap;
	private Map rightMap;

	public BacktrackingAStar(Map leftMap, Map rightMap, Position leftTarget,
			Position rightTarget) {
		this.leftMap = leftMap;
		this.rightMap = rightMap;
		goalState = new State(leftTarget, rightTarget);

		// in case that we can't get to the target position at the same time
		// find the one with minimal h value (estimated distance to target position)
		// with minimal steps needed
		bestNode = null;
		steps = Double.MAX_VALUE;
		bestH = Double.MAX_VALUE;
	}

	@Override
	protected boolean isGoal(State state) {
		return goalState.equals(state);
	}

	@Override
	protected Double g(State from, State to) {
		return 1.0;
	}

	@Override
	protected Double h(State from) {
		double x1, x2, y1, y2, deltaX, deltaY, diagonal;

		x1 = from.posLeft.x;
		y1 = from.posLeft.y;
		x2 = goalState.posLeft.x;
		y2 = goalState.posLeft.y;
		deltaX = Math.abs(x1 - x2);
		deltaY = Math.abs(y1 - y2);
		diagonal = Math.max(deltaX, deltaY);
		// double orthogonal = Math.abs(deltaX - deltaY);
		double distanceLeft = diagonal;// + orthogonal;

		x1 = from.posRight.x;
		y1 = from.posRight.y;
		x2 = goalState.posRight.x;
		y2 = goalState.posRight.y;
		deltaX = Math.abs(x1 - x2);
		deltaY = Math.abs(y1 - y2);
		diagonal = Math.max(deltaX, deltaY);
		// orthogonal = Math.abs(deltaX - deltaY);
		double distanceRight = diagonal;// + orthogonal;

		return Math.max(distanceLeft, distanceRight);
	}

	@Override
	protected List<State> generateSuccessors(Node node) {
		List<State> successors = new LinkedList<State>();

		if (closedStates.contains(node.state))
			return successors;

		Position posLeft = node.state.posLeft;
		Position posRight = node.state.posRight;

		// if one of the players has reached the exit
		if (leftMap.isExit(posLeft) || rightMap.isExit(posRight)) {
			double h = node.f - node.g;
			if (h <= this.bestH) {
				this.bestH = h;
				if (node.f < this.steps)
					this.bestNode = node;
			}
			// do not expand it, return directly
			return successors;
		}

		if (Config.DEBUG)
			System.out.println("Expand:\n" + node.state);

		// now none of the player is on the exit

		for (int i = 1; i != MUMap.aintDToM.length; ++i) {
			int[] aintMove = MUMap.aintDToM[i];
			int deltaX = aintMove[0];
			int deltaY = aintMove[1];

			Position newPosLeft;
			newPosLeft = new Position(posLeft.y + deltaY, posLeft.x + deltaX);
			if (leftMap.isExit(newPosLeft))
				continue;
			if (!leftMap.isValid(newPosLeft)) {
				// if it's not valid, roll back
				newPosLeft.x -= deltaX;
				newPosLeft.y -= deltaY;
			}

			Position newPosRight;
			newPosRight = new Position(posRight.y + deltaY, posRight.x + deltaX);
			if (rightMap.isExit(newPosRight))
				continue;
			if (!rightMap.isValid(newPosRight)) {
				// if it's not valid, roll back
				newPosRight.x -= deltaX;
				newPosRight.y -= deltaY;
			}
			State newState = new State(newPosLeft, newPosRight);
			if (!newState.equals(node.state)
					&& (!closedStates.contains(newState)))
				successors.add(newState);
		}

		return successors;
	}

	@Override
	public List<State> compute(State start) {
		try {
			Node root = new Node();
			root.setState(start);

			fringe.offer(root);

			for (;;) {
				Node p = fringe.poll();

				if (p == null) {
					if (Config.DEBUG)
						System.out.println("No perfect path");
					if (this.bestNode != null)
						return this.constructSolution(bestNode);
					break;
				}

				State last = p.getState();

				if (isGoal(last)) {
					LinkedList<State> retPath = new LinkedList<State>();
					for (Node i = p; i != null; i = i.parent)
						retPath.addFirst(i.getState());
					closedStates.clear();
					return retPath;
				}

				expand(p);
				closedStates.add(p.state);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
