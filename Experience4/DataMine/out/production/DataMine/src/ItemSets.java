import java.util.List;

public class ItemSets {
    List<String> herbItems = null;
    int times = 0;

    public ItemSets(List<String> herbItems, int times) {
        this.herbItems = herbItems;
        this.times = times;
    }

    public List<String> getHerbItems() {
        return herbItems;
    }

    public void setHerbItems(List<String> herbItems) {
        this.herbItems = herbItems;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public ItemSets() {

    }

    public void showHerbItems()

    {
        for (String s:herbItems
             ) {
            System.out.print(s+",");
        }
    }
}
