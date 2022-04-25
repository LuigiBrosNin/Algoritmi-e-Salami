/*
 *  Copyright (C) 2021 Pietro Di Lena
 *  
 *  This file is part of the MNKGame v2.0 software developed for the
 *  students of the course "Algoritmi e Strutture di Dati" first 
 *  cycle degree/bachelor in Computer Science, University of Bologna
 *  A.Y. 2020-2021.
 *
 *  MNKGame is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This  is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this file.  If not, see <https://www.gnu.org/licenses/>.
 */



import java.util.LinkedList;
import java.util.Random;

/**
 * not Totally random software player.
*/
public class RandomPlayer  implements MNKPlayer {
	// default privates
	private Random rand;
	// board ausiliaria per effettuare calcoli
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;

	// added privates
	private static myTree<MNKBoard> node;
	private MNKCellState myCell;
    private MNKCellState enemyCell;    
	private static int M;
    private static int N;
    private static int K;
    private static int DEPTH;

	/**
   * Default empty constructor
   */
	public RandomPlayer() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand    = new Random(System.currentTimeMillis()); 

		B       = new MNKBoard(M,N,K); // board interna
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; //ci aiutano a capire quando uno dei due player ha vinto
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1; // utilissimo per controlli finali e anche non
		TIMEOUT = timeout_in_secs;	
		
		myCell = first ? MNKCellState.P1 : MNKCellState.P2;
        enemyCell = first ? MNKCellState.P2 : MNKCellState.P1;
        DEPTH=M*N;
		// il numero di nodi e foglie da calcolare massimo sono (M*N-1) * (M*N-2) ... , che in casi troppo grandi non riuscirà mai a fare in 10 secondi
		// quindi riduciamo la depth fino ad un numero accettabile e somewhat calcolabile per risparmiare calcoli
		int nodes = (int)Double.POSITIVE_INFINITY;
        while(nodes>999999999){
			DEPTH--;
			nodes = 0;
			for (int i = 1; i < DEPTH; i++) {
				nodes = nodes * (M*N - i);
			}
		}
		
		RandomPlayer.M = M;
        RandomPlayer.N = N;
        RandomPlayer.K = K;

		// Uncomment to chech the initialization timeout
		/* 
		try {
			Thread.sleep(1000*2*TIMEOUT);
		} 
		catch(Exception e) {
		}
		*/
	}

	public int min(int a,int b) {
		if (a>b) {
			return b;
		}
		else return a;
	}

	public int max(int a,int b) {
		if (a>b) {
			return a;
		}
		else return b;
	}

	public void print(String a) {
		System.out.println(a);
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) { // FC = free cells, MC = marked cells
		long start = System.currentTimeMillis(); // prendo il tempo di inizio esecuzione funzione (per il timer)
		// Uncomment to chech the move timeout
		/* 
		try {
			Thread.sleep(1000*2*TIMEOUT);
		} 
		catch(Exception e) {
		}
		*/

		//salva l'ultima markedcell nella board interna
		if (MC.length > 0) { // salva l'ultima markedcell nella game board interna
			MNKCell c = MC[MC.length -1];
			B.markCell(c.i, c.j);
		}
		else {
			// se è la prima mossa in assoluto, per evitare calcoli inutili prende la casella centrale
			B.markCell((int)(B.M/2), (int)(B.N/2));
			//iniziare a generare l'albero prima di ritornare

			return new MNKCell((int)(B.M/2), (int)(B.N/2), myCell);
		}
		
		if (FC.length == 1) return FC[0]; // ritorno immediatamente se non devo calcolare nulla (free cells = 1)

		return new MNKCell((int)(B.M/2), (int)(B.N/2), myCell);
	}

	public String playerName() {
		return "AAAAAH";
	}
}