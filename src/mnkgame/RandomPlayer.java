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

package mnkgame;

import java.util.Random;

/**
 * not Totally random software player.
 */
public class RandomPlayer  implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;

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

	protected int minmax(MNKCell[] FC,MNKCell selected, int depth, boolean myturn, long start) {
		if (depth > (int)((B.M + B.N)/12)||(System.currentTimeMillis()-start)/5000.0 > TIMEOUT*(99.0/100.0)) { 
			//se la profondità supera 1/6 della grandezza complessiva della board oppure il tempo è scaduto non computare più
			return 0;
		}
		int bscore= -999999999;
		int score = 0;
		if (myturn) {
			for (MNKCell d : FC) { 
				if (d != selected) { //controllo d non sia la stessa cella che abbiamo già selezionato nella board interna
					if (B.markCell(d.i,d.j) == myWin) {//marco la cella e controllo se è uno stato finale, in caso ritorno
						B.unmarkCell();
						return 1;
					}
					if (FC.length >= depth+1) return 0;//controllo per caso base pareggio
					score = minmax(FC, selected, depth +1, !myturn,start);
					B.unmarkCell();
					bscore= max(score,bscore);
				}
			}
			return bscore;
		}
		else {
			bscore = 999999999;
			for (MNKCell d : FC) {
				if (d != selected) { //controllo d non sia la stessa cella che abbiamo già selezionato nella board interna
					if (B.markCell(d.i,d.j) == yourWin) {//marco la cella e controllo se è uno stato finale, in caso ritorno
						B.unmarkCell();
						return -1;
					}
					if (FC.length >= depth+1) return 0;//controllo per caso base pareggio
					score = minmax(FC, selected, depth +1, !myturn,start);
					B.unmarkCell();
					bscore= min(score,bscore);
				}
			}
			return bscore;
		}
	}
	/**
	 * Selects a position among those listed in the <code>FC</code> array.
   * <p>
   * 1 Selects a winning cell (if any) from <code>FC</code>, otherwise
   * 2 selects a cell (if any) that prevents the adversary to win 
   * with his next move. 
   * TO DO:
   * 3 - lanci alpha beta pruning, ma ottimizzato con alcune cose
    fatto a - depth limitata (dinamica, basata su grandezza scacchiera)
    fatto b - timeout se ci mette troppo gioca a caso (ricorsivo, passa timeout -1 per 1 secondo di scarto per chiudere tutte le ricorsioni)
    c - range limitato di caselle da controllare (giocando ottimizzato si gioca sempre su caselle adiacenti quindi stonks)
    d - per il punto c trovare altri modi maybe per ottimizzare scegliere il migliore se non aumenta la complessità
   * 
   * If all previous cases do not apply, selects
   * a random cell in <code>FC</code>.
	 * </p>
   */
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) { // FC = free cells, MC = marked cells
		long start = System.currentTimeMillis(); // prendo il tempo di inizio esecuzione funzione (per il timer)

		// Uncomment to chech the move timeout
		 
		try {
			Thread.sleep(1000*2*TIMEOUT);
		} 
		catch(Exception e) {
		}
		

		//salva l'ultima markedcell nella board interna
		if (MC.length > 0) {
			MNKCell c = MC[MC.length -1];
			B.markCell(c.i, c.j);
		}
		
		if (FC.length == 1) return FC[FC.length-1]; // ritorno immediatamente se non devo calcolare nulla (free cells = 1)

		// controllo per mossa finale vincente
		for(MNKCell d : FC) {
			// tempo limitato
			if((System.currentTimeMillis()-start)/5000.0 > TIMEOUT*(99.0/100.0)) { //controllo del timeout
				MNKCell c = FC[rand.nextInt(FC.length)];
				B.markCell(c.i,c.j);
				print("outcome 1");
				return c;
			} else if(B.markCell(d.i,d.j) == myWin) {
				print("outcome 2");
				return d;  
			} else {
				B.unmarkCell();
			}
		}
		int pos = rand.nextInt(FC.length); //serve per il prossimo for
		MNKCell c = FC[pos]; //preparo una cella casuale da ritornare in qualunque momento in caso di timeout o peggior caso
		B.markCell(c.i,c.j); //marco la cella casuale per calcoli interni e successivi
		
		for(int k = 0; k < FC.length; k++) {
      		if((System.currentTimeMillis()-start)/5000.0 > TIMEOUT*(99.0/100.0)) { //ritorna random se timeout
				print("outcome 3");
				return FC[pos];
			} else if(k != pos) {     
				MNKCell d = FC[k];
				if(B.markCell(d.i,d.j) == yourWin) {
					B.unmarkCell();        // undo adversary move
					B.unmarkCell();	       // undo my move (altrimenti vado a marcare il segno sbagliato penso)
					B.markCell(d.i,d.j);   // select his winning position
					print("outcome 4");
					return d;							 // return his winning position
				} else {
					B.unmarkCell();	       // undo adversary move to try a new one
				}	
			}	
		}
		//sium si fa il minmax non ottimizzato per un cazzo giusto per avere una base
		int score = -99999999,bscore = -99999999;
		MNKCell bcell = FC[rand.nextInt(FC.length)]; 
		//best choice, inizializzata per evitare che ritorni vuota se arriva al timeout senza riuscire a computare anche una sola cella
		for(MNKCell d : FC) {
			if((System.currentTimeMillis()-start)/5000.0 > TIMEOUT*(99.0/100.0)) { //controllo del timeout
				B.markCell(bcell.i,bcell.j);
				print("outcome 5");
				return bcell;//ritorna l'opzione migliore trovata fin'ora
			}
			B.markCell(d.i,d.j);
			score = minmax(FC, d , 0 , true, start);
			if (bscore < score) {
				bscore = score;
				bcell = d;
			}
			B.unmarkCell();
		}//finito il for ritorna
		print("outcome 6");
		return bcell;
		//il minmax è da testare
	}

	public String playerName() {
		return "SIUM";
	}
}
