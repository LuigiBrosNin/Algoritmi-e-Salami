//import mnkgame.*;
// strategia
/* 

conviene lavorare in testplayer per la 

1 - prima di tutto si controlla che non ci siano mosse vincenti per noi
2 - poi si controllano le mosse vincenti avversarie (marca la prima casella trovata in quest'ordine dei casi)
3 - lanci alpha beta pruning, ma ottimizzato con alcune cose
    a - depth limitata (dinamica, basata su grandezza scacchiera)
    b - timeout se ci mette troppo ritorna il miglior caso che è riuscito a trovare
    c - range limitato di caselle da controllare (giocando ottimizzato si gioca sempre su caselle adiacenti quindi stonks)
    d - per il punto c trovare altri modi per ottimizzare o scegliere il migliore se non aumenta la complessità
    e - implementare euristica migliorata (controlla nome su pdf)
*/


public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}