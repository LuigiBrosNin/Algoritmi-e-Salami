package mnkgame;

import java.util.HashSet;
import java.util.Random;

public class TestPlayer implements MNKPlayer
{
    private Random rand;
    private MNKBoard B;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private MNKCellState myCell;
    private MNKCellState yourCell;
    private int TIMEOUT;

    private static int M;
    private static int N;
    private static int K;
    private static int DEPTH;
    private static myTree<MNKBoard> node;



    public TestPlayer() {
        
    }


	private void print(String a) {
		System.out.println(a);
	}

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        this.M = M;
        this.N = N;
        this.K = K;

        rand = new Random(System.currentTimeMillis()); 
        B = new MNKBoard(M,N,K);
        myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        myCell = first ? MNKCellState.P1 : MNKCellState.P2;
        yourCell = first ? MNKCellState.P2 : MNKCellState.P1;
        DEPTH=10;
        while(Math.pow((M*N),DEPTH)>1000000000){DEPTH--;}
        TIMEOUT = timeout_in_secs;
    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

        long start = System.currentTimeMillis();
        // partiamo per primi
        if (MC.length == 0) {
            B.markCell(M/2, N/2); // se partiamo per primi, sappiamo giÃ  la nostra prima mossa

            // ci mettiamo avanti, e proviamo a generare un albero di gioco
            this.node = new myTree<MNKBoard>(cloneBoard(B));
            ALPHABETA(node, MC, false, DEPTH, -(int)Double.POSITIVE_INFINITY, (int)Double.POSITIVE_INFINITY, 0, null);
            return new MNKCell(M/2, N/2, myCell);
        }
        // partiamo per secondi
        else if (MC.length == 1) {
            MNKCell LM = MC[MC.length-1]; // ultima mossa dell'avversario
            B.markCell(LM.i, LM.j); // salviamo la mossa dell'avversario

            MNKCell emergencyCell = getEmergencyCell(LM, FC);

            this.node = new myTree<MNKBoard>(cloneBoard(B));
            MinmaxMove MM = ALPHABETA(node, MC, false, DEPTH, -(int)Double.POSITIVE_INFINITY, (int)Double.POSITIVE_INFINITY, start, emergencyCell);

            // cerca la nostra mossa nell'albero di gioco e aggiorna this.node
            for (int i = 0; i < this.node.childs.size(); i++) {
                MNKCell[] CMC = ((MNKBoard)(node.childs.get(i).val)).getMarkedCells();
                MNKCell CLM = CMC[CMC.length-1];

                if (CLM.i == MM.i && CLM.j == MM.j) {
                    this.node = node.childs.get(i);
                    break;
                }
            }

            B.markCell(MM.i, MM.j); // salviamo la nostra mossa

            return new MNKCell(MM.i, MM.j, myCell);
        }
        // partita in corso
        else {            
            MNKCell LM = MC[MC.length-1]; // ultima mossa dell'avversario
            B.markCell(LM.i, LM.j); // salviamo la mossa dell'avversario

            MNKCell emergencyCell = getEmergencyCell(LM, FC);

            // cerca l'ultima mossa dell'avversario nell'albero di gioco che abbiamo e aggiorna this.node
            // se non esiste, bisogna rigenerare l'albero di gioco
            boolean found_node = false;
            for (int i = 0; i < this.node.childs.size(); i++) {
                MNKCell[] CMC = ((MNKBoard)(this.node.childs.get(i).val)).getMarkedCells();
                MNKCell CLM = CMC[CMC.length-1];

                if (CLM.i == LM.i && CLM.j == LM.j) {
                    this.node = node.childs.get(i);
                    found_node = true;
                    break;
                }
            }
            if (!found_node) {
                this.node = new myTree<MNKBoard>(cloneBoard(B));
            }

            MinmaxMove MM = ALPHABETA(this.node, MC, false, DEPTH, -(int)Double.POSITIVE_INFINITY, (int)Double.POSITIVE_INFINITY, start, emergencyCell);
            // cerca la nostra mossa nell'albero di gioco e aggiorna this.node
            for (int i = 0; i < this.node.childs.size(); i++) {
                MNKCell[] CMC = ((MNKBoard)(node.childs.get(i).val)).getMarkedCells();
                MNKCell CLM = CMC[CMC.length-1];

                if (CLM.i == MM.i && CLM.j == MM.j) {
                    this.node = this.node.childs.get(i);
                    break;
                }
            }

            B.markCell(MM.i, MM.j); // salviamo la nostra mossa

            return new MNKCell(MM.i, MM.j, myCell);
        }
    }

    /*
     * O(N*M)
     */

    public static int genChilds(myTree<MNKBoard> T, MNKCell[] MCs) {
        HashSet<String> adiacenze = new HashSet<String>();
        for (MNKCell MC : MCs) {
            if (MC.i == M-1) {
                if (MC.j == N-1) {
                    adiacenze.add((M-1) + "-" + (N-2)); // l
                    adiacenze.add((M-2) + "-" + (N-1)); // t
                    adiacenze.add((M-2) + "-" + (N-2)); // tl
                } else {
                    switch (MC.j) {
                        case 0:
                            adiacenze.add((M-1) + "-" + (MC.j+1)); // r
                            adiacenze.add((M-2) + "-" + (MC.j)); // t
                            adiacenze.add((M-2) + "-" + (MC.j+1)); // tr
                            break;
                        default:
                            adiacenze.add((M-1) + "-" + (MC.j+1)); // r
                            adiacenze.add((M-1) + "-" + (MC.j-1)); // l
                            adiacenze.add((M-2) + "-" + (MC.j)); // t
                            adiacenze.add((M-2) + "-" + (MC.j+1)); // tr
                            adiacenze.add((M-2) + "-" + (MC.j-1)); // tl
                            break;
                    }
                }
            } else {
                switch (MC.i) {
                    case 0:
                        if (MC.j == N-1) {
                            adiacenze.add((MC.i) + "-" + (N-2)); // l
                            adiacenze.add((MC.i+1) + "-" + (N-1)); // b
                            adiacenze.add((MC.i+1) + "-" + (N-2)); // bl
                        } else {
                            switch(MC.j) {
                                case 0:
                                    adiacenze.add((MC.i) + "-" + (MC.j+1)); // r
                                    adiacenze.add((MC.i+1) + "-" + (MC.j)); // b
                                    adiacenze.add((MC.i+1) + "-" + (MC.j+1)); // br
                                    break;
                                default:
                                    adiacenze.add((MC.i) + "-" + (MC.j-1)); // l
                                    adiacenze.add((MC.i) + "-" + (MC.j+1)); // r
                                    adiacenze.add((MC.i+1) + "-" + (MC.j)); // b
                                    adiacenze.add((MC.i+1) + "-" + (MC.j+1)); // br
                                    adiacenze.add((MC.i+1) + "-" + (MC.j-1)); // bl
                                    break;
                            }
                        }
                        break;
                    default:
                        if (MC.j == N-1) {
                            adiacenze.add((MC.i) + "-" + (N-2)); // l
                            adiacenze.add((MC.i+1) + "-" + (N-1)); // b
                            adiacenze.add((MC.i+1) + "-" + (N-2)); // bl
                            adiacenze.add((MC.i-1) + "-" + (N-1)); // t
                            adiacenze.add((MC.i-1) + "-" + (N-2)); // tl
                        } else {
                            switch (MC.j) {
                                case 0:
                                    adiacenze.add((MC.i) + "-" + (MC.j+1)); // r
                                    adiacenze.add((MC.i+1) + "-" + (MC.j)); // b
                                    adiacenze.add((MC.i+1) + "-" + (MC.j+1)); // br
                                    adiacenze.add((MC.i-1) + "-" + (MC.j)); // t
                                    adiacenze.add((MC.i-1) + "-" + (MC.j+1)); // tr
                                    break;
                                default:
                                    adiacenze.add((MC.i) + "-" + (MC.j+1)); // r
                                    adiacenze.add((MC.i) + "-" + (MC.j-1)); // l
                                    adiacenze.add((MC.i+1) + "-" + (MC.j)); // b
                                    adiacenze.add((MC.i+1) + "-" + (MC.j+1)); // br
                                    adiacenze.add((MC.i+1) + "-" + (MC.j-1)); // bl
                                    adiacenze.add((MC.i-1) + "-" + (MC.j)); // t
                                    adiacenze.add((MC.i-1) + "-" + (MC.j+1)); // tr
                                    adiacenze.add((MC.i-1) + "-" + (MC.j-1)); // tl
                                    break;
                            }
                        }
                        break;
                }
            }
        }

        String[] coord = new String[2];
        int i, j;

        int figli_generati = 0;
        for (String adiacenza : adiacenze) {
            coord = adiacenza.split("-");
            i = Integer.parseInt(coord[0]);
            j = Integer.parseInt(coord[1]);

            if (T.val.cellState(i, j) == MNKCellState.FREE) {
                figli_generati++;
                MNKBoard childB = cloneBoard(T.val);
                childB.markCell(i, j);
                T.addChild(childB);
            }
        }

        return figli_generati;
    }


    private MNKCell getEmergencyCell(MNKCell LM, MNKCell[] FC) {
        MNKCell emergencyCell = FC[rand.nextInt(FC.length)];

        for (MNKCell d : FC) {
            if (
                d.i == LM.i && d.j == LM.j+1 ||
                d.i == LM.i && d.j == LM.j-1 ||
                d.i == LM.i+1 && d.j == LM.j+1 ||
                d.i == LM.i+1 && d.j == LM.j-1 ||
                d.i == LM.i-1 && d.j == LM.j+1 ||
                d.i == LM.i-1 && d.j == LM.j-1
            ) {
                emergencyCell = d;
            }
        }

        return emergencyCell;
    }

    private MinmaxMove ALPHABETA(myTree<MNKBoard> T, MNKCell[] MC, boolean mynode, int depth, int a, int b, long start, MNKCell emergencyCell) {
        if (start != 0 && (System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)) {
            return new MinmaxMove(emergencyCell.i, emergencyCell.j, 0);
        }

        boolean gameover = T.val.gameState() != MNKGameState.OPEN;
        int childsCount = 1;
        if (!gameover)
            childsCount = genChilds(T,MC);
        if (depth == 0 || gameover || childsCount == 0) {
            print(Integer.toString(depth));
            MNKCell[] MCs = T.val.getMarkedCells();
            MNKCell c = MCs[MCs.length-1];

            if (gameover) {
                if (T.val.gameState() == myWin) {
                    return new MinmaxMove(c.i, c.j, (int)Double.POSITIVE_INFINITY);
                    
                }
                else if (T.val.gameState() == yourWin) {
                    return new MinmaxMove(c.i, c.j, -(int)Double.POSITIVE_INFINITY);
                    
                }
                else {
                    return new MinmaxMove(c.i, c.j, 0);
                    
                }
            }

            int out = depth*evaluate(T.val, mynode);
            return new MinmaxMove(c.i, c.j, out);
        }
        else if (mynode) {
            int eval = (int)Double.POSITIVE_INFINITY;
            //genChilds(T, MC);
            int l=-1, j=-1;
            for (int i=0; i<T.childs.size();i++) {
                MinmaxMove out = ALPHABETA((myTree)T.childs.get(i), MC, false, depth-1, a, b, start, emergencyCell);
                if (out.eval <= eval || l==-1) {
                    eval = out.eval;
                    MNKCell[] MCs = ((MNKBoard)T.childs.get(i).val).getMarkedCells();
					MNKCell c = MCs[MCs.length-1];
                    l=c.i;
                    j=c.j;
                }
                b = Math.min(eval, b);
                if (b <= a) { // a cutoff
                    break;
                }
            }
            return new MinmaxMove(l, j, eval);
        }
        else {
            int eval = -(int)Double.POSITIVE_INFINITY;
            //genChilds(T, MC);
            int l=-1, j=-1;
            for (int i=0; i<T.childs.size();i++) {
                MinmaxMove out = ALPHABETA((myTree)T.childs.get(i), MC, true, depth-1, a, b, start, emergencyCell);
                if (out.eval >= eval || l==-1) {
                     eval = out.eval;
                    MNKCell[] MCs = ((MNKBoard)T.childs.get(i).val).getMarkedCells();
					MNKCell c = MCs[MCs.length-1];
                    l=c.i;
                    j=c.j;
                }

                a = Math.max(eval, a);
                if (b <= a) { // b cutoff
                    break;
                }
            }
            return new MinmaxMove(l, j, eval);
        }
    }

    /*
     * O(6*M*N)
     */
    private static int evaluate(MNKBoard B, boolean myNode){
        int myLine=1, myScore=0, yourLine=1, yourScore=0;

        int inverter;
        if (!myNode){
            inverter=1;
        }
        else{
            inverter=-1;
        }
        
        //conteggio orizzontale
        for (int i=0; i<M; i++){
            myLine=1;yourLine=1;
            for(int j=0;j<N;j++){
                if(B.cellState(i,j)==MNKCellState.P1){
                    myScore+=myLine;
                    myLine=myLine*10;
                    yourLine=1;
                }
                else if(B.cellState(i,j)==MNKCellState.P2){
                    yourScore+=yourLine;
                    yourLine=yourLine*10;
                    myLine=1;
                }
                else{
                    myLine=1;
                    yourLine=1;
                }
            }
        }

        //conteggio verticale
        for (int i=0; i<N; i++){
            myLine=1;yourLine=1;
            for(int j=0;j<M;j++){
                if(B.cellState(j,i)==MNKCellState.P1){
                    myScore+=myLine;
                    myLine=myLine*10;
                    yourLine=1;
                }
                else if(B.cellState(j,i)==MNKCellState.P2){
                    yourScore+=yourLine;
                    yourLine=yourLine*10;
                    myLine=1;
                }
                else{
                    myLine=1;
                    yourLine=1;
                }
            }
        }        
                

       if(M>=K && N>=K){
          
            //conteggio diagonale verso destra con offset orizzontale
            for (int i=0; i<N; i++){
                myLine=1;yourLine=1;
                if(Math.min(N-i,M)>=K){
                    for(int j=0; j<Math.min(N-i,M); j++){
                        if(B.cellState(j,j+i)==MNKCellState.P1){
                            myScore+=myLine;
                            myLine=myLine*10;
                            yourLine=1;
                        }
                        else if(B.cellState(j,j+i)==MNKCellState.P2){
                            yourScore+=yourLine;
                            yourLine=yourLine*10;
                            myLine=1;
                        }
                        else{
                            myLine=1;
                            yourLine=1;
                        }
                    }
                }
            }
            
            //conteggio diagonale verso destra con offset verticale
            for (int i=1; i<M; i++){
                myLine=1;yourLine=1;
                if(Math.min(M-i,N)>=K){
                    for(int j=0; j<Math.min(M-i,N); j++){
                        if(B.cellState(j+i,j)==MNKCellState.P1){
                            myScore+=myLine;
                            myLine=myLine*10;
                            yourLine=1;
                        }
                        else if(B.cellState(j+i,j)==MNKCellState.P2){
                            yourScore+=yourLine;
                            yourLine=yourLine*10;
                            myLine=1;
                        }
                        else{
                            myLine=1;
                            yourLine=1;
                        }
                    }
                }
            }
            
            //conteggio diagonale verso sinistra con offset orizzontale
            for (int i=0; i<N; i++){
                myLine=1;yourLine=1;
                if(Math.min(N-i,M)>=K){
                    for(int j=0; j<Math.min(N-i,M); j++){
                        // j-i
                        if(B.cellState(j,N-1-j-i)==MNKCellState.P1){
                            myScore+=myLine;
                            myLine=myLine*10;
                            yourLine=1;
                        }
                        else if(B.cellState(j,N-1-j-i)==MNKCellState.P2){
                            yourScore+=yourLine;
                            yourLine=yourLine*10;
                            myLine=1;
                        }
                        else{
                            myLine=1;
                            yourLine=1;
                        }
                    }
                }
            }
            
            //conteggio diagonale verso sinistra con offset verticale
            for (int i=1; i<M; i++){
                myLine=1;yourLine=1;
                if(Math.min(M-i,N)>=K){
                    for(int j=0; j<Math.min(M-i,N); j++){
                        // j-i
                        if(B.cellState(j+i,N-1-j)==MNKCellState.P1){
                            myScore+=myLine;
                            myLine=myLine*10;
                            yourLine=1;
                        }
                        else if(B.cellState(j+i,N-1-j)==MNKCellState.P2){
                            yourScore+=yourLine;
                            yourLine=yourLine*10;
                            myLine=1;
                        }
                        else{
                            myLine=1;
                            yourLine=1;
                        }
                    }
                }
            }
        }

        return inverter*(myScore-yourScore);   
    }

    /*
     * O(N*M)
     */
    private static MNKBoard cloneBoard(MNKBoard original) {
        MNKCell[] MCs = original.getMarkedCells();
        MNKBoard copy = new MNKBoard(original.M, original.N, original.K);
        for (MNKCell MC : MCs) {
            copy.markCell(MC.i, MC.j);
        }
        return copy;
    }

    public String playerName() {
        return "Alexa, this is so sad, play mnkgamesitos++";
    }
}
