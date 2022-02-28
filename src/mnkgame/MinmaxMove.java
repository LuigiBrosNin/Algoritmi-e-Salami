package mnkgame;

public class MinmaxMove
{
    public int i;
    public int j;
    public int eval;

    public MinmaxMove(int i, int j, int eval) {
        this.i = i;
        this.j = j;
        this.eval = eval;
    }

    @Override
    public String toString() {
        return "(" + i + "," + j +") out: " + eval;
    }
}