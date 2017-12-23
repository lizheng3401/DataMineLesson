package KNN;

import com.csvreader.CsvReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KnnNode
{
    private int ID;
    private double distance;
    private int type;

    public KnnNode(int ID, double distance, int type) {
        this.ID = ID;
        this.distance = distance;
        this.type = type;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

public class Knn {

    public static List<List<String>> init(String path)
    {
        List<List<String>> sources = new ArrayList<>();

        ArrayList<String[]> csvFileList = null;
        String[] header = null;
        try {
            csvFileList = new ArrayList<>();
            String csvFilePath = path;
            CsvReader reader = new CsvReader(csvFilePath, ',', Charset.forName("GBK"));
            reader.readHeaders();
            header = reader.getHeaders();
            while (reader.readRecord()) {
                csvFileList.add(reader.getValues());
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        for (String[] strings:csvFileList) {
            List<String> temp = new ArrayList<>();
            for (int i = 0; i < strings.length; i++) {
                temp.add(strings[i]);
            }
            sources.add(temp);
        }

        return sources;
    }

    public static List<List<Double>> getSources(String path)
    {
        List<List<String>> data = init(path);
        List<List<Double>> sources = new ArrayList<>();
        List<String> tempS = null;
        for (int i = 0; i < data.size(); i++) {
            tempS = data.get(i);
            List<Double> tempI = new ArrayList<>();
            for (String s:tempS) {
                Double a = Integer.parseInt(s)*1.0;
                tempI.add(a);
            }
            sources.add(tempI);
            tempS.clear();
        }

        return sources;
    }

    public static List<Integer> getRandom(int num, int max)
    {
        List<Integer> random = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            int temp = (int)(Math.random() * max);
            if(!random.contains(temp))
            {
                random.add(temp);
            }
        }
        return random;
    }

    public static double work(List<Double> source,List<Double> test)
    {
        double dis = 0.0;
        for (int i = 0; i < source.size(); i++) {
            dis += (source.get(i) - test.get(i)) * (source.get(i) - test.get(i));
        }

        return dis;
    }
    public static List<KnnNode> getOrderedKnnNodes(List<KnnNode> knnNodes, int k)
    {
        List<KnnNode> re = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            int position = 0;
            double min = 1000;
            for (int j = 0; j < knnNodes.size(); j++) {
                double temp = knnNodes.get(j).getDistance();
                if(temp == 0.0)
                {
                    position = j;
                    break;
                }
                if(temp < min)
                {
                    min = temp;
                    position = j;
                }
            }
            KnnNode ks = knnNodes.get(position);
            re.add(new KnnNode(ks.getID(),ks.getDistance(),ks.getType()));
            knnNodes.get(position).setDistance(1000);
        }

        return re;

    }

    public static int getType(List<KnnNode> orderedKnnNodes)
    {
        if(orderedKnnNodes.get(0).getDistance() == 0.0)
        {
            return orderedKnnNodes.get(0).getType();
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < orderedKnnNodes.size(); i++) {
            KnnNode kn = orderedKnnNodes.get(i);
            if(!map.containsKey(kn.getType()))
            {
                map.put(kn.getType(), 1);
            }
            else
            {
                int temp = map.get(kn.getType()) + 1;
                map.put(kn.getType(), temp);
            }
        }
        int re = -1;
        for (Integer i: map.keySet()) {
            if(map.get(i) > re)
            {
                re = map.get(i);
            }
        }
        return re;
    }

    public static int knn(List<List<Double>> sources, List<Double> test, int k)
    {
        List<KnnNode> knnNodes = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            List<Double> source = sources.get(i);
            double s = source.get(source.size() - 1);
            int type =  (int)s;
            KnnNode kn = new KnnNode(i,work(source, test), type);
            knnNodes.add(kn);
        }

        List<KnnNode> orderedKnnNodes = getOrderedKnnNodes(knnNodes, k);

        int result = getType(orderedKnnNodes);
        return result;
    }

    public static void main(String args[])
    {
        String path = "D:\\Daily\\DataMine\\src\\cate.csv";
        int k = 7;
        int right = 0,error = 0,total = 0;

        List<List<Double>> sources = getSources(path);
        for (List<Double> source: sources) {
            int testRe = knn(sources, source, k);
            double s = source.get(source.size() - 1);
            int realRe = (int)s;
            if(testRe == realRe)
            {
                right++;
            }
            else
            {
                error++;
            }
        }

        total = sources.size();
        System.out.println("right: "+right);
        System.out.println("error: "+error);
        System.out.println("Total: "+total);
        System.out.println("Right Percent: "+(right*1.0/total*100)+"%");

        List<Double> testData = new ArrayList<>();
        for (double d:sources.get(0)) {
            testData.add(d);
        }
        for (int i = 0, j = 5; i < testData.size(); i++) {
            if(testData.get(i) == 0.0)
            {
                testData.set(i, 1.0);
                j++;
            }
            if(j == 5)
            {
                break;
            }
        }
        System.out.print("enter: ");
        for (double d:testData) {
            System.out.print(d+",");
        }
        System.out.println();
        if(1.0*knn(sources, testData, k) == testData.get(testData.size() - 1))
        {
            System.out.println("right");
        }
        else
        {
            System.out.println("false");
        }


    }
}
