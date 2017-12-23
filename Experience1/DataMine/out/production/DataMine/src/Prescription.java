import java.util.ArrayList;
import java.util.List;

public class Prescription {
    private String name;
    private String Type;
    private String content;
    private int[] item =  new int[588];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Prescription(String name, String type, String content) {
        this.name = name;
        Type = type;
        this.content = content;
    }

    public int[] getItem() {
        return item;
    }

    public void setItem(int[] item) {
        this.item = item;
    }

    public List<String> getHerb()
    {
        List<String> herbs = new ArrayList<String>();
        String[] temp = this.content.split("；");
        for(int i = 0; i < temp.length; i++)
        {
           herbs.add(temp[i].split("：")[0]);
        }
        return herbs;
    }

}
