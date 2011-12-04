
package mirroruniverse.g2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import mirroruniverse.sim.MUMap;

public class Explorer {
	Map leftMap;
	Map rightMap;
	LinkedList<Position> leftOpenList = new LinkedList<Position>();
	LinkedList<Position> rightOpenList = new LinkedList<Position>();
	public int r = -1;
	public boolean allExplored = false;
	Backtracker backtrack;

	public Explorer(Map leftMap, Map rightMap) {
		this.leftMap = leftMap;
		this.rightMap = rightMap;
	}

	public int getMove(int[][] aintViewL, int[][] aintViewR) {
		if (r == -1) {
			r = aintViewL.length / 2;
		}
		int d = nextBestSearch();
		//System.out.println(d);
		return d;
	}

	public int nextBestSearch() {
		int d = -1;
		int bestCount = 0;
		int leftCount = 0;
		int rightCount = 0;
		for (int i = 0; i <= 8; i++) {
			int[] diff = MUMap.aintDToM[i];
			leftCount = countNewSpacesOpened(diff, leftMap, leftMap.playerPos);
			rightCount = countNewSpacesOpened(diff, rightMap, rightMap.playerPos);
			if (leftCount + rightCount > bestCount) {
				bestCount = leftCount + rightCount;
				d = i;
			}
		}
		if (bestCount != 0) {
			int[] diff = MUMap.aintDToM[d];
			if (leftCount > 1) {
				leftOpenList.push(new Position(leftMap.playerPos.y+diff[1], leftMap.playerPos.x+diff[0]));
			}
			if (rightCount > 1) {
				rightOpenList.push(new Position(rightMap.playerPos.y+diff[1], rightMap.playerPos.x+diff[0]));
			}
			return d;
		}
		while (backtrack == null || !backtrack.pathFound()) {
			//System.out.println("Before generateBackTrack");
			generateBackTrack();
			//System.out.println("After generateBackTrack");
		}
		//System.out.println("Before getMove");
		if (backtrack.pathFound())
			d = backtrack.getMove();
		else
			d = this.randomness();
		//System.out.println("After getMove");
		//System.out.println("backtrack generated moves");
		return d;
	}
	
	public Position getFrontier(LinkedList<Position> list, Map myMap) {
		Iterator<Position> itr = list.iterator();
		while (itr.hasNext()) {
			Position p = itr.next();
			if (myMap.map[p.y][p.x] != Map.Tile.UNKNOWN.getValue()) {
				itr.remove();
			} else {
				return p;
			}
		}
		return null;
	}
	
	public Position closestUnknown(Map myMap, Position current) {
		myMap.map[current.y][current.x] = Map.Tile.MARKED.getValue();
		//System.out.println(current + " is marked");
		Random r = new Random();
		int[] directions = {1, 2, 3, 4, 5, 6, 7, 8};
		for (int i = 0; i < 7; i++) {
			int slot1 = r.nextInt(8);
			int slot2 = r.nextInt(8);
			int temp = directions[slot1];
			directions[slot1] = directions[slot2];
			directions[slot2] = temp;
		}
		for (int i = 0; i < directions.length; i++) {
			int j = directions[i];
			int[] diff = MUMap.aintDToM[j];
			if (current.y + diff[1] >= myMap.map.length || current.y + diff[1] < 0) {
				continue;
			}
			if (current.x + diff[0] >= myMap.map.length || current.x + diff[0] < 0) {
				continue;
			}
			if (myMap.map[current.y + diff[1]][current.x + diff[0]] == Map.Tile.UNKNOWN.getValue()) {
				return current;
			}
			if (myMap.map[current.y + diff[1]][current.x + diff[0]] == Map.Tile.EMPTY.getValue()) {
				//System.out.println("looking at " + new Position(current.y + diff[1], current.x + diff[0]));
				Position newPos = closestUnknown(myMap, new Position(current.y + diff[1], current.x + diff[0]));
				//System.out.println("returned " + newPos);
				if (newPos != null) {
					System.out.println("****************** " + newPos);
					return newPos;
				}
			}
		}
		return null;
	}
	
/*	public void generateBackTrack() {
		Map newLeft = new Map("left", leftMap);
		Map newRight = new Map("right", rightMap);
		Position leftPos = closestUnknown(newLeft, leftMap.playerPos);
		Position rightPos = closestUnknown(newRight, rightMap.playerPos);
		//System.out.println("Returned " + leftPos);
		//System.out.println("Returned " + rightPos);
		backtrack = new Backtracker(leftMap, rightMap, leftPos, rightPos);
	}*/
	

	public void generateBackTrack() {
		Position leftPos = leftOpenList.pop();
		Position rightPos = rightOpenList.pop();
		//System.out.println("Start Backtracker++");
		backtrack = new Backtracker(leftMap, rightMap, leftPos, rightPos);
		//System.out.println("End Backtracker++");
	}

	public int countNewSpacesOpened(int[] diff, Map myMap, Position pos) {
		int ret = 0;
		Position newPos = pos.newPosFromOffset(diff[1], diff[0]);
		if (myMap.map[newPos.y][newPos.x] == Map.Tile.EMPTY.getValue()) {
			for (int i = -r; i <= r; i++) {
				for (int j = -r; j <= r; j++) {
					if (myMap.map[newPos.y + i][newPos.x + j] == Map.Tile.UNKNOWN.getValue()) {
						ret++;
					}
				}
			}
		}
		return ret;
	}
	
	public int countSpacesOpen(Map myMap, Position pos) {
		int ret = 0;
		if (myMap.map[pos.y][pos.x] == Map.Tile.EMPTY.getValue()) {
			for (int i = -((r / 2) + 1); i <= (r / 2) + 1; i++) {
				for (int j = -((r / 2) + 1); j <= (r / 2) + 1; j++) {
					if (myMap.map[pos.y + i][pos.x + j] == Map.Tile.UNKNOWN.getValue()) {
						ret++;
					}
				}
			}
		}
		return ret;
	}

	public int randomness() {
		Random rdmTemp = new Random();
		int nextX = rdmTemp.nextInt(3);
		int nextY = rdmTemp.nextInt(3);
		//System.out.println("RANDOM");
		//System.exit(0);
		return MUMap.aintMToD[nextX][nextY];
	}

}