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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

/**
 * not Totally random software player.
 */
public class RandomPlayer implements MNKPlayer {
	// default privates
	private Random rand;
	// board ausiliaria per effettuare calcoli
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;

	// added privates
	private long start;// starting time of selectCell
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
		rand = new Random(System.currentTimeMillis());

		B = new MNKBoard(M, N, K); // board interna
		myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2; // ci aiutano a capire quando uno dei due player ha
																	// vinto
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1; // utilissimo per controlli finali e anche non
		TIMEOUT = timeout_in_secs;

		myCell = first ? MNKCellState.P1 : MNKCellState.P2;
		yourCell = first ? MNKCellState.P2 : MNKCellState.P1;
		DEPTH = M * N;
		while (Math.pow((M * N), DEPTH) > 1000000000) {
			DEPTH--;
		}
		// il numero di nodi e foglie da calcolare massimo sono ((M*N)-1) * ((M*N)-2)
		// ... , che in casi troppo grandi non riuscirà mai a fare in 10 secondi

		RandomPlayer.M = M;
		RandomPlayer.N = N;
		RandomPlayer.K = K;

		// Uncomment to chech the initialization timeout
		/*
		 * try {
		 * Thread.sleep(1000*2*TIMEOUT);
		 * }
		 * catch(Exception e) {
		 * }
		 */
	}

	private MinmaxMove min(MinmaxMove a, MinmaxMove b) {
		if (a.eval < b.eval) {
			return a;
		} else
			return b;
	}

	private MinmaxMove max(MinmaxMove a, MinmaxMove b) {
		if (a.eval > b.eval) {
			return a;
		} else
			return b;
	}

	private void print(String a) {
		System.out.println(a);
	}

	private long currentTime() {
		return System.currentTimeMillis();
	}

	// funzione di controllo di minaccia amica/nemica orizzontale
	private int horizontalCheck(myTree<MNKBoard> t) {
		int myValue = 0, yourValue = 0, myMenace = 1, yourMenace = 1;
		int checkScore;

		for (int i = 0; i < M; i += 1) {
			myMenace = 1;
			yourMenace = 1;
			for(int j=0; j<N; j+=1) {
				if(t.val.cellState(i, j) == MNKCellState.P1
				|| (t.val.cellState(i, j+1) == MNKCellState.P1 && j+1<N)
				 ) {
					myValue = myValue + myMenace;
					myMenace = myMenace * 10;
					yourMenace = 1;
				}
				if(t.val.cellState(i, j) == MNKCellState.P2
				|| (t.val.cellState(i, j+1) == MNKCellState.P2 && j+1<N)) {
					yourValue = yourValue + yourMenace;
					yourMenace = yourMenace * 10;
					myMenace = 1;
				} else {
					myMenace = 1;
					yourMenace = 1;
				}
			}
		}
		checkScore = myValue - yourValue;
		return checkScore;
	}

	// funzione di controllo di minaccia amica/nemica verticale
	private int verticalCheck(myTree<MNKBoard> t) {
		int myValue = 0, yourValue = 0, myMenace = 1, yourMenace = 1;
		int checkScore;

		for (int i = 0; i < M; i += 1) {
			myMenace = 1;
			yourMenace = 1;
			for(int j=0; j<N; j+=1) {
				if(t.val.cellState(j, i) == MNKCellState.P1
				|| (t.val.cellState(j+1, i) == MNKCellState.P1 && j+1<M)) {
					myValue = myValue + myMenace;
					myMenace = myMenace * 10;
					yourMenace = 1;
				}
				if(t.val.cellState(j, i) == MNKCellState.P2
				|| (t.val.cellState(j+1, i) == MNKCellState.P2 && j+1<M)) {
					yourValue = yourValue + yourMenace;
					yourMenace = yourMenace * 10;
					myMenace = 1;
				} else {
					myMenace = 1;
					yourMenace = 1;
				}
			}
		}
		checkScore = myValue - yourValue;
		return checkScore;
	}

	// funzione di controllo di minaccia amica/nemica diagonale con offset
	// orizzontale
	private int DiagonalCheckHorizontalOffset(myTree<MNKBoard> t, boolean isRight) {
		int myValue = 0, yourValue = 0, myMenace = 1, yourMenace = 1;
		int y;
		int checkScore;

		for(int i=0; i<N; i+=1) {
			myMenace = 1; yourMenace = 1;
			if(Math.min(N-i, M) >= K){
				for(int j=0; j<Math.min(N-i, M); j+=1){
					if (isRight) {
						y = j+i;
						if(t.val.cellState(j, y) == MNKCellState.P1
						|| (t.val.cellState(j+1, y+1) == MNKCellState.P1 && j+1<M && y+1<N)) {
						myValue = myValue + myMenace;
						myMenace = myMenace * 10;
						yourMenace = 1;
						}
						else if(t.val.cellState(j, y) == MNKCellState.P2
						|| (t.val.cellState(j+1, y+1) == MNKCellState.P2 && j+1<M && y+1<N)) {
							yourValue = yourValue + yourMenace;
							yourMenace = yourMenace * 10;
							myMenace = 1;
						}
						else {
							myMenace = 1;
							yourMenace = 1;
						}
					}
					else {
						y = N-1-j-i;
						if(t.val.cellState(j, y) == MNKCellState.P1
						|| (t.val.cellState(j+1, y-1) == MNKCellState.P1 && j+1<M && y-1>N)) {
						myValue = myValue + myMenace;
						myMenace = myMenace * 10;
						yourMenace = 1;
						}
						else if(t.val.cellState(j, y) == MNKCellState.P2
						|| (t.val.cellState(j+1, y-1) == MNKCellState.P2 && j+1<M && y-1>N)) {
							yourValue = yourValue + yourMenace;
							yourMenace = yourMenace * 10;
							myMenace = 1;
						}
						else {
							myMenace = 1;
							yourMenace = 1;
						}
					}
				}
			}
		}
		checkScore = myValue - yourValue;
		return checkScore;
	}

	// funzione di controllo di minaccia amica/nemica diagonale con offset verticale
	private int DiagonalCheckVerticalOffset(myTree<MNKBoard> t, boolean isRight) {
		int myValue = 0, yourValue = 0, myMenace = 1, yourMenace = 1;
		int x,y;
		int checkScore;

		for(int i=0; i<M; i+=1) {
			myMenace = 1; yourMenace = 1;
			if(Math.min(M-i, N) >= K){
				for(int j=0; j<Math.min(M-i, N); j+=1){
					if (isRight) {
						x = j+i;
						if(t.val.cellState(x, j) == MNKCellState.P1
						|| (t.val.cellState(x+1, j+1) == MNKCellState.P1 && x+1<M && j+1<N)) {
						myValue = myValue + myMenace;
						myMenace = myMenace * 10;
						yourMenace = 1;
						}
						else if(t.val.cellState(x, j) == MNKCellState.P2
						|| (t.val.cellState(x+1, j+1) == MNKCellState.P2 && x+1<M && j+1<N)) {
						yourValue = yourValue + yourMenace;
						yourMenace = yourMenace * 10;
						myMenace = 1;
						}
						else {
							myMenace = 1;
							yourMenace = 1;
						}
					}
					else {
						x = j+i;
						y = N-1-j;
						{
							if(t.val.cellState(x, y) == MNKCellState.P1
							|| (t.val.cellState(x+1, y-1) == MNKCellState.P1 && x+1<M && y>N)) {
							myValue = myValue + myMenace;
							myMenace = myMenace * 10;
							yourMenace = 1;
							}
							else if(t.val.cellState(x, y) == MNKCellState.P2
							|| t.val.cellState(x+1, y-1) == MNKCellState.P2 && x+1<M && y>N) {
							yourValue = yourValue + yourMenace;
							yourMenace = yourMenace * 10;
							myMenace = 1;
							}
							else {
								myMenace = 1;
								yourMenace = 1;
							}
						}
					}

				}
			}
		}
		checkScore = myValue - yourValue;
		return checkScore;
	}

	/*
	 * costo
	 * O(6*M*N)
	 */
	private int evaluate(myTree<MNKBoard> t, boolean myTurn) {
		int cellscore = 0;

		// controllo orizzontale
		cellscore += horizontalCheck(t);
		// controllo verticale
		cellscore += verticalCheck(t);

		if (M >= K && N >= K) {
			// controllo diagonale dx, offset orizzontale
			cellscore += DiagonalCheckHorizontalOffset(t, true);

			// controllo diagonale dx, offset verticale
			cellscore += DiagonalCheckVerticalOffset(t, true);

			// controllo diagonale sx, offset orizzontale
			cellscore += DiagonalCheckHorizontalOffset(t, false);

			// controllo diagonale sx, offset verticale
			cellscore += DiagonalCheckVerticalOffset(t, false);

		}

		if (myTurn)
			return (cellscore);
		else
			return (-cellscore);
	}

	private int getChilds(myTree<MNKBoard> tree, MNKCell[] MC){
		HashSet<MNKCell> list = new HashSet<MNKCell>();
		int pos=0;
		for (MNKCell c : MC) {
			//aggiungo le caselle esistenti
			// angles
			if (c.i != M-1 && c.j != N-1) list.add(new MNKCell(c.i-1, c.j+1)); //dwn r
			if (c.i != 0 && c.j != 0)     list.add(new MNKCell(c.i+1, c.j+1)); // up r
			if (c.i != M-1 && c.j != 0)   list.add(new MNKCell(c.i-1, c.j-1)); //dwn l
			if (c.i != 0 && c.j != N-1)   list.add(new MNKCell(c.i+1, c.j-1)); // up l
			// arrows
			if (c.i != 0)                 list.add(new MNKCell(c.i+1, c.j)); // up
			if (c.i != M-1)               list.add(new MNKCell(c.i-1, c.j)); // dwn
			if (c.j != 0)                 list.add(new MNKCell(c.i, c.j-1)); // l
			if (c.j != N-1)               list.add(new MNKCell(c.i, c.j+1)); // r
		}
		//TOFINISH

		return -1;
	}


	// true indica il turno del bot mentre false quello dell'avversario
	private MinmaxMove abPruning(myTree<MNKBoard> tree, boolean myTurn, int alpha, int beta, int depth) {
		MNKCell[] MCs = tree.val.getMarkedCells();
		MNKCell lastCell = MCs[MCs.length - 1];
		MinmaxMove cell = new MinmaxMove(-1, -1, 0);

		Boolean isGameOver = tree.val.gameState != MNKGameState.OPEN;

		if (depth == 0 || (currentTime() - start) / 9000.0 > TIMEOUT * (90.0 / 100.0) || isGameOver) { // TODO: casi base
			cell.eval = evaluate(tree, myTurn);
			cell.i = lastCell.i;
			cell.j = lastCell.j;
		} else if (myTurn) {
			cell.eval = (int) Double.NEGATIVE_INFINITY;
			for (myTree<MNKBoard> c : tree.childs) {
				cell = max(cell, abPruning(c, false, alpha, beta, depth - 1));
				alpha = Math.max(cell.eval, alpha);
				if (beta <= alpha)
					break;
			}
		} else {
			cell.eval = (int) Double.POSITIVE_INFINITY;
			for (myTree<MNKBoard> c : tree.childs) {
				cell = min(cell, abPruning(c, true, alpha, beta, depth - 1));
				beta = Math.min(cell.eval, beta);
				if (beta <= alpha)
					break;
			}
		}
		// TODO: update tree ?
		return cell;
	}

	/**
	 * 
	 * @param FC free cells array
	 * @return cell that makes us win the game in this move, otherwise returns an
	 *         invalid enemycell
	 */
	private MNKCell selectWinningCell(MNKCell[] FC) {

		// Check whether there is single move win
		for (MNKCell d : FC) {
			if (B.markCell(d.i, d.j) == myWin) {
				return d;
			} else {
				B.unmarkCell();
			}
		}
		// se nessuna cella è vincente, ritorna una cella invalida (nemica per il
		// controllo validità)
		return new MNKCell(-1, -1, yourCell);
	}

	/**
	 * 
	 * @param FC free cells array
	 * @return cell that makes us lose the game in the next move, otherwise returns
	 *         an invalid enemycell
	 */
	private MNKCell preventLoss(MNKCell[] FC) {

		B.markCell(FC[1].i, FC[1].j);
		MNKCell c = FC[0]; // random move

		// mark the random position and check if it's the winning position
		if (B.markCell(c.i, c.j) == yourWin) {
			// sincronizzo la board interna e ritorno
			B.unmarkCell();
			B.unmarkCell();
			B.markCell(c.i, c.i);
			return c;
		}

		// sincronizzo la board per il for
		B.unmarkCell();
		B.unmarkCell();
		B.markCell(c.i, c.i);

		for (MNKCell d : FC) {
			if (B.markCell(d.i, d.j) == yourWin) {
				B.unmarkCell(); // undo adversary move
				B.unmarkCell(); // undo my move
				B.markCell(d.i, d.j); // select his winning position
				return d; // return his winning position
			} else {
				B.unmarkCell(); // undo adversary move to try a new one
			}
		}
		// se nessuna cella è vincente, ritorna una cella invalida (nemica per il
		// controllo validità)
		return new MNKCell(-1, -1, yourCell);
	}

	/**
	 * 
	 * simple copying function for creating new boards for us to save in the
	 * gameTree
	 * 
	 * @param toCopy la board da copiare <code> B </code> che passeremo quasi sempre
	 *               in questo contesto
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

	// cerca la mossa nell'albero di gioco e aggiorna l'albero interno
	private void updateNode(MNKCell move){
	   	boolean moved = false;
	   for (int i = 0; i < RandomPlayer.node.childs.size(); i++) {
			MNKCell[] CMC = ((MNKBoard)(node.childs.get(i).val)).getMarkedCells();
			MNKCell CLM = CMC[CMC.length-1];
			if (CLM.i == move.i && CLM.j == move.j) {
				RandomPlayer.node = RandomPlayer.node.childs.get(i);
				moved = true;
				break;
			}
		}
		// se non esiste il nodo che cerco, lo creo e assegno
		if (!moved) {
			RandomPlayer.node = new myTree<MNKBoard>(copyBoard(B));
		}
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		start = currentTime(); // prendo il tempo di inizio esecuzione funzione (per il timer)
		// Uncomment to check the move timeout
		/*
		 * try {
		 * Thread.sleep(1000*2*TIMEOUT);
		 * }
		 * catch(Exception e) {
		 * }
		 */

		// salva l'ultima markedcell nella board interna
		MNKCell c;
		if (MC.length > 0) {
			c = MC[MC.length - 1];
			B.markCell(c.i, c.j);
		} else {
			// se è la prima mossa in assoluto, per evitare calcoli inutili prende la
			// casella centrale
			B.markCell((int) (B.M / 2), (int) (B.N / 2));
			// iniziamo a generare l'albero prima di ritornare per usare il tempo a
			// disposizione
			RandomPlayer.node = new myTree<MNKBoard>(copyBoard(B)); // creo l'albero interno

			abPruning(RandomPlayer.node, true, (int) Double.NEGATIVE_INFINITY, (int) Double.POSITIVE_INFINITY,
					RandomPlayer.DEPTH);

			return new MNKCell((int) (B.M / 2), (int) (B.N / 2), myCell);
		}
		// seconda mossa in assoluto, dobbiamo creare il gametree e iniziare a calcolare
		if (MC.length == 1) {
			RandomPlayer.node = new myTree<MNKBoard>(copyBoard(B));

			MinmaxMove bestmove;

			// TODO: emergency cell

			bestmove = abPruning(RandomPlayer.node, true, (int) Double.NEGATIVE_INFINITY,(int) Double.POSITIVE_INFINITY, RandomPlayer.DEPTH);

			MNKCell ret = new MNKCell(bestmove.i, bestmove.j);
			
			B.markCell(ret.i, ret.j);

			updateNode(ret);

			return ret;
		}

		if (FC.length == 1)
			return FC[0]; // ritorno immediatamente se non devo calcolare nulla (free cells = 1)

		// mi sposto nel nodo dell'albero interessato
		updateNode(c);

		MNKCell ret;

		// cerco una casella vincente
		ret = selectWinningCell(FC);

		// se non trovo una casella vincente ne cerco una perdente
		if (ret.state == yourCell)
			ret = preventLoss(FC);

		// se ho trovato una cella valida, la ritorno
		if (ret.state != yourCell)
			return ret;

		// inizio a calcolare la mossa migliore
		MinmaxMove bestmove;

		// TODO: emergency cell

		bestmove = abPruning(RandomPlayer.node, true, (int) Double.NEGATIVE_INFINITY,(int) Double.POSITIVE_INFINITY, RandomPlayer.DEPTH);
		
		ret = new MNKCell(bestmove.i, bestmove.j);
			
		B.markCell(ret.i, ret.j);

		updateNode(ret);

		return ret;
	}

	public String playerName() {
		return "AAAAAH";
	}
}