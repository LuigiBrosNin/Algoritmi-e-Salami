

import java.util.LinkedList;
import java.util.Random;

/**
 * not Totally random software player.
*/
public class SIUMPlayer  implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;

	/**
   * Default empty constructor
   */
	public SIUMPlayer() {
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
/*
ritorna true se all'interno dell'array di celle trova una cella con le stesse coordinate presenti nel secondo parametro
*/
	public boolean contains(MNKCell[] Array, int[][] C) {
		for (MNKCell mnkcell : Array) {
			for (int k = 0; k < C.length; k++) {
				if (mnkcell.i == C[k][0] && mnkcell.j == C[k][1]) {
					return true;
				}
			}
		}
		return false;
	}

	public int[][] initAdMatrix(int[][] C, MNKCell d){
		C[0][0] = d.i-1;//implementazione orrenda che non abbiamo avuto tempo di scrivere decentemente
		C[0][1] = d.j-1;//anche per la mancanza di tempo non abbiamo potuto aggiungere il controllo
						//che si accerta che le celle adiacenti esistano in modo da diminuire i controlli in contains()
		C[1][0] = d.i-1;
		C[1][1] = d.j; 
		
		C[2][0] = d.i-1;
		C[2][1] = d.j+1;
		
		C[3][0] = d.i;
		C[3][1] = d.j-1;
		
		C[4][0] = d.i;
		C[4][1] = d.j+1;
		
		C[5][0] = d.i+1;
		C[5][1] = d.j-1;
		
		C[6][0] = d.i+1;
		C[6][1] = d.j;
		
		C[7][0] = d.i+1;
		C[7][1] = d.j+1;
		return C;
	}

/*
ritorna una linkedlist con tutte le celle che sono libere ed adiacenti a una cella marcata :) 
*/
	public LinkedList<MNKCell> AdiacentCells() {
			
		MNKCell[] freeCells = B.getFreeCells();
		MNKCell[] MCell = B.getMarkedCells();
		LinkedList<MNKCell> RCell=new LinkedList<MNKCell>();
		int[][] CheckCell = new int[8][2];
		for (MNKCell d : freeCells) {//Ciclo che si ferma quando non ci sono più celle libere
			CheckCell = initAdMatrix(CheckCell, d); 
			if(contains( MCell, CheckCell )) { //Controllo se la cella CheckCell è in MCell
				RCell.add(d);//se lo è la aggiungo alla lista da ritornare
			}
		}
		return RCell;
	}

	protected int minmax(MNKCell[] FC, int depth, boolean myturn, long start, int a, int b) { //ottimizzato con l'alphabeta
		if (depth > (int)((B.M * B.N)/3)||(System.currentTimeMillis()-start)/9000.0 > TIMEOUT*(99.0/100.0)) { 
			//se la profondità supera 1/3 della grandezza complessiva della board oppure il tempo è scaduto non computare più
			return 0;
		}
		int bscore= -999999999;
		int score = 0;
		if (myturn) {
			for (MNKCell d : FC) { 
				if (B.markCell(d.i,d.j) == myWin) {//marco la cella e controllo se è uno stato finale, in caso ritorno
					B.unmarkCell();
					return 1;
				}
				if (FC.length <= depth+1){
					B.unmarkCell();
					return 0;//controllo per caso base pareggio
				}
				score = score + minmax(B.getFreeCells(), depth +1, !myturn,start,a,b);
				B.unmarkCell();
				bscore = max(score,bscore);
				b = min(b,score);
				if (b<=a) break; // a cutoff
			}
			return bscore;
		}
		else {
			bscore = 999999999;
			for (MNKCell d : FC) {
				if (B.markCell(d.i,d.j) == yourWin) {//marco la cella e controllo se è uno stato finale, in caso ritorno
					B.unmarkCell();
					return -1;
				}
				if (FC.length <= depth+1){
					B.unmarkCell();
					return 0;//controllo per caso base pareggio
				} 
				score = score + minmax(B.getFreeCells(), depth +1, !myturn,start,a,b);
				B.unmarkCell();
				bscore = min(score,bscore);
				a = max(a,score);
				if (b<=a) break; // b cutoff
			}
			return bscore;
		}
	}
	/*



   */
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
			for (MNKCell q : FC) { // se è la prima mossa in assoluto, per evitare calcoli inutili prende la casella centrale
				if (q.i ==(int)(B.M/2) && q.j ==(int)(B.N/2)) {
					B.markCell(q.i, q.j);
					return q;
				}
			}
		}
		
		if (FC.length == 1) return FC[FC.length-1]; // ritorno immediatamente se non devo calcolare nulla (free cells = 1)

		// controllo per mossa finale vincente
		for(MNKCell d : FC) {
			// tempo limitato
			if((System.currentTimeMillis()-start)/9000.0 > TIMEOUT*(99.0/100.0)) { //controllo del timeout
				MNKCell c = FC[rand.nextInt(FC.length)];
				B.markCell(c.i,c.j);
				return c;
			} else if(B.markCell(d.i,d.j) == myWin) {
				return d;  
			} else {
				B.unmarkCell();
			}
		}
		int pos = rand.nextInt(FC.length); //serve per il prossimo for
		MNKCell c = FC[pos]; //preparo una cella casuale da ritornare in qualunque momento in caso di timeout o peggior caso
		B.markCell(c.i,c.j); //marco la cella casuale per calcoli interni e successivi
		
		for(int k = 0; k < FC.length; k++) {
      		if((System.currentTimeMillis()-start)/9000.0 > TIMEOUT*(99.0/100.0)) { //ritorna random se timeout
				return FC[pos];
			} else if(k != pos) {     
				MNKCell d = FC[k];
				if(B.markCell(d.i,d.j) == yourWin) {
					B.unmarkCell();        // undo adversary move
					B.unmarkCell();	       // undo my move (altrimenti vado a marcare il segno sbagliato penso)
					B.markCell(d.i,d.j);   // select his winning position
					return d;							 // return his winning position
				} else {
					B.unmarkCell();	       // undo adversary move to try a new one
				}	
			}	
		}
		B.unmarkCell(); //tolgo la cella casuale marcata prima
		int score = -99999999,bscore = -99999999;
		MNKCell bcell = c; 
		//best choice, inizializzata per evitare che ritorni vuota se arriva al timeout senza riuscire a computare anche una sola cella
		LinkedList<MNKCell> list=AdiacentCells();
		for(MNKCell d : list) {
			if((System.currentTimeMillis()-start)/9000.0 > TIMEOUT*(99.0/100.0)) { //controllo del timeout
				B.markCell(bcell.i,bcell.j);
				return bcell;//ritorna l'opzione migliore trovata fin'ora
			}
			B.markCell(d.i,d.j);
			score = minmax( B.getFreeCells() , 0 , false, start,0,99999999);
			if (bscore < score) {
				bscore = score;
				bcell = d;
			}
			B.unmarkCell();
		}//finito il for ritorna
		B.markCell(bcell.i,bcell.j);
		return bcell;
	}

	public String playerName() {
		return "SIUM";
	}
}
