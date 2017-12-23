import java.util.HashMap;
import java.util.Map;

public class IDTreeNode {
    private String herb;
    private Map<String, IDTreeNode> demo = new HashMap<>();

    public String getHerb() {
        return herb;
    }

    public void setHerb(String herb) {
        this.herb = herb;
    }

    public Map<String, IDTreeNode> getDemo() {
        return demo;
    }

    public void setDemo(Map<String, IDTreeNode> demo) {
        this.demo = demo;
    }

    public IDTreeNode() {

    }
}
