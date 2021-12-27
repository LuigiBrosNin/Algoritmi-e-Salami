//import mnkgame.*;
// strategia
/* 

conviene lavorare in testplayer per la 

1 - prima di tutto si controlla che non ci siano mosse vincenti per noi
2 - poi si controllano le mosse vincenti avversarie (marca la prima casella trovata in quest'ordine dei casi)
3 - lanci alpha beta pruning, ma ottimizzato con alcune cose
    a - depth limitata (dinamica, basata su grandezza scacchiera)
    b - timeout se ci mette troppo gioca a caso (ricorsivo, passa timeout -1 per 1 secondo di scarto per chiudere tutte le ricorsioni)
    c - range limitato di caselle da controllare (giocando ottimizzato si gioca sempre su caselle adiacenti quindi stonks)
    d - per il punto c trovare altri modi maybe per ottimizzare scegliere il migliore se non aumenta la complessità

    <3 betto <3

*/


public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}
