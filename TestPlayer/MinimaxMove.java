package mnkgame;

import java.io.*;
import java.util.*;

public class MinimaxMove
{
    public int i;
    public int j;
    public int eval;

    public MinimaxMove(int i, int j, int eval) {
        this.i = i;
        this.j = j;
        this.eval = eval;
    }

    @Override
    public String toString() {
        return "(" + i + "," + j +") out: " + eval;
    }
}