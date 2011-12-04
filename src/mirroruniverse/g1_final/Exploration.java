package mirroruniverse.g1_final;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mirroruniverse.sim.MUMap;
import mirroruniverse.g1_final.Info;


public class Exploration {
	
	private int[][] lArrPossiblyConnecting;
	private int[][] rArrPossiblyConnecting;
	private int globalX, globalY;
	
	
	private ArrayList<Coord> lALPossiblyConnecting, rALPossiblyConnecting;
	
	boolean leftFinished, rightFinished;
	
	Coord target;
	
	public Exploration(){
		lArrPossiblyConnecting = new int[Info.GlobalViewL.length][Info.GlobalViewL.length];
		rArrPossiblyConnecting = new int[Info.GlobalViewL.length][Info.GlobalViewL.length];
		
		lALPossiblyConnecting = new ArrayList<Coord>();
		rALPossiblyConnecting = new ArrayList<Coord>();
		
		leftFinished = false;
		rightFinished = false;
		
		target = null;
	}
	
	/*
	 * Implements a random Move.Making sure 
	 * that a 0 move is not returned. 
	 */
	
	public static boolean isMoveLegal(int direction){
		boolean retValue =  true;
		int lastXMove = MUMap.aintDToM [direction][0];
		int lastYMove = MUMap.aintDToM [direction][1];
		
		if (!Mirrim.seeLeftExit && Info.LocalViewR [Info.LocalViewR.length / 2 + lastYMove][Info.LocalViewR.length / 2 + lastXMove] == MapData.EXIT)
			retValue = false;
		if (!Mirrim.seeRightExit && Info.LocalViewL[Info.LocalViewL.length / 2 + lastYMove][Info.LocalViewL.length / 2 + lastXMove]== MapData.EXIT)
			retValue = false;
		
		System.out.println("Returning value " + retValue);
		return retValue;
	}
	
	
	
	public static boolean notWall( int intDeltaX,int intDeltaY){
		if (Info.LocalViewL[ Info.LocalViewL.length/ 2 + intDeltaY ][ Info.LocalViewL.length / 2 + intDeltaX ] == 1 
				|| (intDeltaY == 0 && intDeltaX == 0) || Info.LocalViewR[Info.LocalViewL.length / 2 + intDeltaY][Info.LocalViewL.length / 2 + intDeltaX] ==1)
			return false;
		
		else return true;
	}
	
	public static int randomMove (){
		
		Random rdmTemp = new Random();
		int d=0;
		int nextX =0 ,nextY = 0;
				
		do{
			nextX = rdmTemp.nextInt(3);
			nextY = rdmTemp.nextInt(3);
	
			d = MUMap.aintMToD[nextX][nextY];
		} while (d==0 && isMoveLegal(d) );
		
			System.out.println("Next move is :" + MUMap.aintDToM[d][0] + " "
					+ MUMap.aintDToM[d][1]);
		return d;

	}
	
	/*
	 * Look at view from each player and determine which are the newly explored squares
	 * If any of those new squares are 0, add them to list 
	 * Remove from list any squares not on the outside of our view radius (incl player square)
	 */
	public void updatePossibleConnects(int[][] lLocalView, int[][] rLocalView){
		int index = -1;
		if(!leftFinished){
			//removing inner cells from list
			for(int i = 1; i < lLocalView.length - 1; i++){
				for(int j = 1; j < lLocalView.length - 1; j++){
					if((index = lALPossiblyConnecting.indexOf(new Coord(j+Info.getCurrLX() - lLocalView.length/2, i+Info.getCurrLY() - lLocalView.length/2, 'l'))) != -1){
						lArrPossiblyConnecting[j+Info.getCurrLY() - lLocalView.length/2][i+Info.getCurrLX() - lLocalView.length/2] = 0;
						if(lALPossiblyConnecting.get(index).equals(target))
							target = null;
						lALPossiblyConnecting.remove(index);
					}
				}
			}
			//adding appropriate outer cells to list
			for(int i = 0; i < lLocalView.length; i += lLocalView.length - 1){
				for(int j = 0; j < lLocalView.length; j += lLocalView.length - 1){
					if(Info.GlobalViewL[j+Info.getCurrLY() - lLocalView.length/2][i+Info.getCurrLX() - lLocalView.length/2] == 4 && lLocalView[j][i] == 0){
						lArrPossiblyConnecting[j+Info.getCurrLY() - lLocalView.length/2][i+Info.getCurrLX() - lLocalView.length/2] = 1;
						lALPossiblyConnecting.add(new Coord(i+Info.getCurrLX() - lLocalView.length/2, j+Info.getCurrLY() - lLocalView.length/2, 'l'));
					}
				}
			}
			leftFinished = lALPossiblyConnecting.isEmpty();
		}
		if(!rightFinished){
			for(int i = 1; i < rLocalView.length - 1; i++){
				for(int j = 1; j < rLocalView.length - 1; j++){
					if((index = rALPossiblyConnecting.indexOf(new Coord(j+Info.getCurrRX() - rLocalView.length/2, i+Info.getCurrRY() - rLocalView.length/2, 'r'))) != -1){
						rArrPossiblyConnecting[j+Info.getCurrRY() - rLocalView.length/2][i+Info.getCurrRX() - rLocalView.length/2] = 1;
						if(rALPossiblyConnecting.get(index).equals(target))
							target = null;
						rALPossiblyConnecting.remove(index);
					}
				}
			}
			for(int i = 0; i < rLocalView.length; i += rLocalView.length - 1){
				for(int j = 0; j < rLocalView.length; j += rLocalView.length - 1){
					if(Info.GlobalViewR[j+Info.getCurrRY() - rLocalView.length/2][i+Info.getCurrRX() - rLocalView.length/2] == 4 && rLocalView[j][i] == 0){
						rArrPossiblyConnecting[j+Info.getCurrRY() - rLocalView.length/2][i+Info.getCurrRX() - rLocalView.length/2] = 1;
						rALPossiblyConnecting.add(new Coord(i+Info.getCurrRX() - rLocalView.length/2, j+Info.getCurrRY() - rLocalView.length/2, 'r'));
					}
				}
			}
			rightFinished = rALPossiblyConnecting.isEmpty();
		}
	}
	

	 /*
	 * If any new squares were added, pick one of them and make it the target
	 * Otherwise, pick one square from possiblyConnecting list and set it as target
	 * While target remains in list and not at target, use astar to move towards target
	 */
	public int explore(int[][] lLocalView, int[][] rLocalView, int lastDirection){
		//TODO make more intelligent (both able to move, etc)
		//if target is no longer possibly connecting, generate new target
		if(target != null){
			if(!leftFinished){
				if(target.getX() == Info.currLX && target.getY() == Info.currLY)
					lALPossiblyConnecting.remove(target);
			}
			else{
				if(target.getX() == Info.currRX && target.getY() == Info.currRY)
					rALPossiblyConnecting.remove(target);
			}
		}
		
		if(target == null || !(lALPossiblyConnecting.contains(target) || rALPossiblyConnecting.contains(target))){
			Collections.sort(lALPossiblyConnecting);
			Collections.sort(rALPossiblyConnecting);
			//TODO use this code for next time, it tries to pick the min move for any player
			/*if(!leftFinished && !rightFinished){
				if(lALPossiblyConnecting.get(0).compareTo(rALPossiblyConnecting.get(0)) < 0)
					target = lALPossiblyConnecting.get(0);
				else
					target = rALPossiblyConnecting.get(0);
			}
			else if(!leftFinished)
				target = lALPossiblyConnecting.get(0);
			else
				target = rALPossiblyConnecting.get(0);*/
		}
		Collections.sort(lALPossiblyConnecting);
		Collections.sort(rALPossiblyConnecting);
		int[][] mymap;
		if(!leftFinished){
			mymap = Info.GlobalViewL;
			target = lALPossiblyConnecting.get(0);
			globalX = Info.getCurrLX();
			globalY = Info.getCurrLY();
		}
		else{
			mymap = Info.GlobalViewR;
			target = rALPossiblyConnecting.get(0);
			globalX = Info.getCurrRX();
			globalY = Info.getCurrRY();
		}
		int i = 0;
		
		Node_Single path;
		AStar_Single s = new AStar_Single(Info.currLX, Info.currLY, target.x, target.y, mymap);
		int mynext = s.findPath().getActionPath().get(0);
		int[] nextstep = MUMap.aintDToM[mynext];
		
//		if(!leftFinished) {
//			while(Info.GlobalViewL[globalY + nextstep[0]][globalX + nextstep[1]] == 2) {
//				i++;
//				target = lALPossiblyConnecting.get(i);
//				mynext = s.findPath().getActionPath().get(0);
//				nextstep = MUMap.aintDToM[mynext];
//			}
//		}
//		//TODO astar to target
//		else {
//			while(Info.GlobalViewR[globalY + nextstep[0]][globalX + nextstep[1]] == 2) {
//				i++;
//				target = rALPossiblyConnecting.get(i);
//				mynext = s.findPath().getActionPath().get(0);
//				nextstep = MUMap.aintDToM[mynext];
//			}
//	}
		return mynext;
//		int directionToMove = 0;
////		
//		System.out.println((globalX - path.getX1()) + ", y " + (globalY - path.getY1()));
//		for(int b = 0; b<MUMap.aintDToM.length; b++) {
//			if(globalX - path.getX1() == MUMap.aintDToM[b][0] && globalY - path.getY1()  == MUMap.aintDToM[b][1])
//					directionToMove = b;
//		}
//			
//		
//		return directionToMove;
	}

	public boolean isLeftFinished() {
		return leftFinished;
	}

	public boolean isRightFinished() {
		return rightFinished;
	}
	
	
	
	/*
	 * TODO
	 * Add more functions for a more intelligent exploration strategy. 
	 * They can take any parameters from any of the classes written by us.
	 * HOWEVER, IT IS IMPORTANT THAT THEY RETURN AN INTEGER.
	 * 
	 * The function may or may not be static - its not important.
	 */
}

class Coord implements Comparable<Coord>{
	
	int x,y, globalX, globalY;
	char side;
	
	public Coord(int x, int y, char side){
		this.y = y;
		this.x = x;
		this.side = side;
	}
	
	//TODO should side be included in the equals?
	public boolean equals(Coord c){
		return y == c.getY() && x == c.getX();
	}
	
	

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public char getSide(){
		return side;
	}

	@Override
	public int compareTo(Coord c) {
		if(this.equals(c))
			return 0;
		if(side == 'l'){
			globalX = Info.getCurrLX();
			globalY = Info.getCurrLY();
		}
		else{
			globalX = Info.getCurrRX();
			globalY = Info.getCurrRY();
		}
		if(Math.sqrt(Math.pow(x - (globalX), 2) + Math.pow(y - (globalY), 2)) 
				< Math.sqrt(Math.pow(c.getX() - (globalX), 2) + Math.pow(c.getY() - (globalY), 2))){
			return -1;
		}
		else{
			return 1;
		}
	}
	
}
