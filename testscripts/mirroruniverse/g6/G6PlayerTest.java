package mirroruniverse.g6;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class G6PlayerTest {

	@Test
	public void testExplore() {
		G6Player tester = new G6Player();
		//example left view
		int[][] leftView = { {0,0,0},
						   {1,0,0},
						   {0,0,1}};
		
		//example right view
		int[][] rightView = {{1,1,1},
							{0,0,1},
							{1,0,1}};
		
		assertNotNull("explore result:", tester.explore(leftView, rightView));
		
	}


	@Test
	public void testLookAndMove() {
		G6Player tester = new G6Player();
		//example left view
		int[][] leftView = { {0,0,0},
						   {1,0,0},
						   {0,0,1}};
		
		//example right view
		int[][] rightView = {{1,1,1},
							{0,0,1},
							{1,0,1}};
		assertNotNull("look and move result:", tester.lookAndMove(leftView, rightView));
	}

}
