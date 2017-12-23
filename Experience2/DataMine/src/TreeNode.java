import java.util.*;
import java.lang.*;

public class TreeNode {
    private int num;
    private String herb;
    private TreeNode parent = null;
    private List <TreeNode> childList =  new ArrayList<TreeNode>();
    private TreeNode nextNode;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getHerb() {
        return herb;
    }

    public void setHerb(String herb) {
        this.herb = herb;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildList() {
        return childList;
    }

    public void setChildList(List<TreeNode> childList) {
        this.childList = childList;
    }

    public TreeNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(TreeNode nextNode) {
        this.nextNode = nextNode;
    }

    public TreeNode() {

    }

    public List<TreeNode> getchild(){
        if (childList!=null){
            return childList;
        }
        return null;
    }

    public TreeNode findChild(String name)
    {
        List<TreeNode> children = this.getchild();
        if(children != null)
        {
            for (TreeNode child:children) {
                if (child.getHerb().equals(name))
                {
                    return child;
                }
            }
        }

        return null;
    }

    public void addChild(TreeNode n)
    {
        this.childList.add(n);
    }

}
