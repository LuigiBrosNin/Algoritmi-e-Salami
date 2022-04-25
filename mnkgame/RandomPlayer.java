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
    private MNKCellState yourCell;    
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
        yourCell = first ? MNKCellState.P2 : MNKCellState.P1;
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

	private int min(int a,int b) {
		if (a>b) {
			return b;
		}
		else return a;
	}

	private int max(int a,int b) {
		if (a>b) {
			return a;
		}
		else return b;
	}

	private void print(String a) {
		System.out.println(a);
	}

	private long currentTime(){
		return System.currentTimeMillis();
	}

	// funzione da scrivere, implementazione temporanea
	private int evaluate(myTree<MNKBoard> t, int depth) {
		return 0;
	}

	// true indica il turno del bot mentre false quello dell'avversario
	private MinmaxMove abPruning(myTree<MNKBoard> tree, bool myTurn, int alpha, int beta, int depth) {
		int eval;
		if(depth == 0 || tree.isLeaf())
			eval = evaluate(tree.val, depth);
		else if(myTurn) {
			eval = (int)Double.NEGATIVE_INFINITY;
			for(myTree<MNKBoard> c : tree.childs){
				eval = max(eval, abPruning(c, false, alpha, beta, depth-1));
				alpha = max(eval, alpha);
				if (beta <= alpha)
					break;
			}
		}
		else {
			eval = (int)Double.POSITIVE_INFINITY;
			for (myTree<MNKBoard> c : tree.childs) {
				eval = min(eval, abPruning(c, true, alpha, beta, depth-1));
				beta = min (eval, beta);
				if (beta <= alpha)
					break;
			}
		}
		tree.val = eval;
		return eval;
	}
	/**
	 * 
	 * @param FC free cells array
	 * @return cell that makes us win the game in this move, otherwise returns an invalid enemycell
	 */
	private MNKCell selectWinningCell (MNKCell[] FC){
				
		// Check whether there is single move win 
		for(MNKCell d : FC) {
			if(B.markCell(d.i,d.j) == myWin) {
				return d;
			} else {
				B.unmarkCell();
			}
		}
		// se nessuna cella è vincente, ritorna una cella invalida (nemica per il controllo validità)
		return new MNKCell(-1,-1, yourCell);
	}

	/**
	 * 
	 * @param FC free cells array
	 * @return cell that makes us lose the game in the next move, otherwise returns an invalid enemycell
	 */
	private MNKCell preventLoss (MNKCell[] FC){

		B.markCell(FC[1].i, FC[1].j);
		MNKCell c = FC[0]; // random move

		// mark the random position and check if it's the winning position	
		if(B.markCell(c.i,c.j) == yourWin){
			// sincronizzo la board interna e ritorno
			B.unmarkCell();
			B.unmarkCell();
			B.markCell(c.i, c.i);
			return c; 
		}

		//sincronizzo la board per il for
		B.unmarkCell();
		B.unmarkCell();
		B.markCell(c.i, c.i);

		for(MNKCell d : FC) {
			if (B.markCell(d.i, d.j) == yourWin) {
				B.unmarkCell(); // undo adversary move
				B.unmarkCell(); // undo my move
				B.markCell(d.i, d.j); // select his winning position
				return d; // return his winning position
			} else {
				B.unmarkCell(); // undo adversary move to try a new one
			}
		}
		// se nessuna cella è vincente, ritorna una cella invalida (nemica per il controllo validità)
		return new MNKCell(-1,-1, yourCell);
	}

	/**
	 * 
	 * simple copying function for creating new boards for us to save in the gameTree
	 * 
	 * @param toCopy la board da copiare <code> B </code> che passeremo quasi sempre in questo contesto
	 * @return una copia della board passata per parametro
	 */
    private MNKBoard copyBoard(MNKBoard toCopy) {
	   MNKCell[] MCs = toCopy.getMarkedCells();
	   MNKBoard inCopy = new MNKBoard(toCopy.M, toCopy.N, toCopy.K);
	   for (MNKCell MC : MCs) {
		   inCopy.markCell(MC.i, MC.j);
	   }
	   return inCopy;
   }

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		long start = currentTime(); // prendo il tempo di inizio esecuzione funzione (per il timer)
		// Uncomment to check the move timeout
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
			//iniziamo a generare l'albero prima di ritornare per usare il tempo a disposizione
			RandomPlayer.node = new myTree<MNKBoard>(copyBoard(B)); // creo l'albero interno

			abPruning(RandomPlayer.node, true, (int)Double.NEGATIVE_INFINITY, (int)Double.POSITIVE_INFINITY, RandomPlayer.DEPTH);

			return new MNKCell((int)(B.M/2), (int)(B.N/2), myCell);
		}
		
		if (FC.length == 1) return FC[0]; // ritorno immediatamente se non devo calcolare nulla (free cells = 1)

		MNKCell c;

		// cerco una casella vincente
		c = selectWinningCell(FC);
		
		// se non trovo una casella vincente ne cerco una perdente
		if (c.state == yourCell) c = preventLoss(FC);

		// se ho trovato una cella valida, la ritorno
		if (c.state != yourCell) return c;

		//inizio a calcolare la mossa migliore


		//return temporaneo invalido
		return new MNKCell(-1,-1, yourCell);
	}

	public String playerName() {
		return "AAAAAH";
	}
}