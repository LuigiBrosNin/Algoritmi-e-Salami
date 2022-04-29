

import java.util.ArrayList;

public class myTree<A> {
    public A val;
    public ArrayList<myTree> childs;

    public myTree(A input){
        this.val = input;
        this.childs = new ArrayList<myTree>(); 
    }

    public void addChild(A input){
        myTree<A> son=new myTree<>(input);
        childs.add(son);
    }

    public boolean isLeaf(){
        for (myTree child : childs) {
            if(child!=null){return false;}
        }
        return true;
    }

    @Override
    public String toString() {
        String output = "val: " + this.val + " // childrens: ";
        for (myTree child : childs) {
            output += child.val + ",";
        }
        return output;
    }
}
