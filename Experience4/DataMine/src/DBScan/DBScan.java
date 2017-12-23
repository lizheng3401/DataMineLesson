package DBScan;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

class DBNode{

    private int ID;
    private Boolean visited = false;
    private Boolean noised = false;
    private String name = null;
    private double[] attrs = null;
    private Boolean centerPoint = false;
    private List<DBNode> children = new ArrayList<>();
    private Boolean isOrdered = false;
    public Boolean getVisited() {
        return visited;
    }

    public void setVisited(Boolean visited) {
        this.visited = visited;
    }

    public Boolean getNoised() {
        return noised;
    }

    public void setNoised(Boolean noised) {
        this.noised = noised;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[] getAttrs() {
        return attrs;
    }

    public void setAttrs(double[] attrs) {
        this.attrs = attrs;
    }

    public Boolean getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(Boolean centerPoint) {
        this.centerPoint = centerPoint;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public List<DBNode> getChildren() {
        return children;
    }

    public void setChildren(List<DBNode> children) {
        this.children = children;
    }

    public Boolean getOrdered() {
        return isOrdered;
    }

    public void setOrdered(Boolean ordered) {
        isOrdered = ordered;
    }
}
public class DBScan {

    static int MINPTS;
    static double EPS;

    public static ArrayList<String[]> csvRead(String path)
    {
//        读入数据源文件
        ArrayList<String[]> csvFileList = null;
        try {
            csvFileList = new ArrayList<>();
            String csvFilePath = path;
            CsvReader reader = new CsvReader(csvFilePath, ',', Charset.forName("GBK"));
            reader.readHeaders();
            while (reader.readRecord()) {
                csvFileList.add(reader.getValues());
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return csvFileList;
    }

    public static void csvWriter(String path,String csvFilePath)
    {
//        将读入的数据格式化后存入csv文件中
        Map<String, Integer> attr_x = new HashMap<>();
        Map<String, Integer> attr_w = new HashMap<>();
        Map<String, Integer> attr_g = new HashMap<>();
        List<String> ls = new ArrayList<>();
        List<String> ls_x = new ArrayList<>();
        List<String> ls_w = new ArrayList<>();
        List<String> ls_g = new ArrayList<>();

        ArrayList<String[]> csvFileList = csvRead(path);

        int x = 0, w = 0, g = 0;
        for (int row = 0, i = 0; row < csvFileList.size(); row++, i++) {
            String[] item = csvFileList.get(row);
            String[] xing = item[1].split("、");
            String[] wei = item[2].split("、");
            String[] guijing = item[3].split("、");

            for (int j = 0; j < xing.length; j++) {
                if(!attr_x.containsKey(xing[j]))
                {
                    attr_x.put(xing[j], x);
                    x++;
                    ls_x.add(xing[j]);
                }
            }

            for (int j = 0; j < wei.length; j++) {
                if(!attr_w.containsKey(wei[j]))
                {
                    attr_w.put(wei[j], w);
                    w++;
                    ls_w.add(wei[j]);
                }
            }

            for (int j = 0; j < guijing.length; j++) {
                if(!attr_g.containsKey(guijing[j]))
                {
                    attr_g.put(guijing[j], g);
                    g++;
                    ls_g.add(guijing[j]);
                }
            }
        }

        ls.addAll(ls_x);
        ls.addAll(ls_w);
        ls.addAll(ls_g);

        try {
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("GBK"));
            int length = ls.size()+ 1;
            String[] csvheader = new String[length];
            csvheader[0] = "药名";

            for (int i = 0; i < length - 1; i++) {
                csvheader[i+1] = ls.get(i);
            }

            csvWriter.writeRecord(csvheader);

            for (int i = 0; i < csvFileList.size(); i++) {
                String[] csvContent = new String[length];
                String[] strings = csvFileList.get(i);
                String[] xing = strings[1].split("、");
                String[] wei = strings[2].split("、");
                String[] guijing = strings[3].split("、");
                for (int j = 0; j < csvContent.length; j++) {
                    csvContent[j] = "0";
                }
                csvContent[0] = strings[0];
                for (int j = 0; j < xing.length; j++) {
                    csvContent[attr_x.get(xing[j])+1] = "1";
                }
                for (int j = 0; j < wei.length; j++) {
                    csvContent[attr_w.get(wei[j]) + x + 1] = "1";
                }
                for (int j = 0; j < guijing.length; j++) {
                    csvContent[attr_g.get(guijing[j])+ w + x + 1] = "1";
                }
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<DBNode> init(String path)
    {
//        将数据从格式化的csv文件中读出
        ArrayList<String[]> csvFileList =  csvRead(path);
        List<DBNode> sources = new ArrayList<>();
        for (int row = 0; row < csvFileList.size(); row++) {
            DBNode node = new DBNode();
            String[] item = csvFileList.get(row);
            double[] attrs = new double[item.length - 1];
            for (int i = 1; i < item.length; i++) {
                attrs[i-1] = Integer.parseInt(item[i])*1.0;
            }
            node.setID(row);
            node.setName(item[0]);
            node.setAttrs(attrs);
            sources.add(node);
        }

        return sources;
    }

    public static double getDistance(double[] a, double[] b)
    {
//        计算两个节点之间的距离
        double dis = 0.0;
        for (int i = 0; i < a.length; i++) {
            dis += (a[i] - b[i])*(a[i] - b[i]);
        }

        dis = Math.sqrt(dis);
        return dis;
    }
    public static List<DBNode> getNearNode(DBNode node, List<DBNode> sources)
    {
//        获取它的临近节点即半径为EPS范围内的所有点
        List<DBNode> nearNode = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            DBNode temp = sources.get(i);
            double length = getDistance(temp.getAttrs(), node.getAttrs());
            if(length <= EPS)
            {
                nearNode.add(temp);
            }
        }

        return nearNode;
    }
    public static List<DBNode> DBSCAN(List<DBNode> sources)
    {
//        DBSCAN主算法

        List<DBNode> clusters = new ArrayList<>();
//      遍历所有节点
        for (DBNode node: sources) {
//            若节点已访问，则直接进入下一个点
            if(node.getVisited())
            {
                continue;
            }
//            获取它的临近节点
            List<DBNode> nearNodes = getNearNode(node, sources);
//            如果它的临近节点树木小于MINPTS，则判定为噪声点，若是，则为核心对象
            if(nearNodes.size() < MINPTS)
            {
                node.setNoised(true);
            }
            else
            {
//                新建一个簇
                DBNode cluster = new DBNode();

                for (int j = 0; j < nearNodes.size(); j++) {
                    DBNode nearNode = nearNodes.get(j);
                    if(!nearNode.getVisited())
                    {
                        nearNode.setVisited(true);
//                        如果该节点的邻域内至少有MINPTS个对象，把这些对象添加至对象集合中
                        List<DBNode> subNearNodes = getNearNode(nearNode, sources);
                        if(subNearNodes.size() >= MINPTS)
                        {
                            for (DBNode temp: subNearNodes) {
                                if(!nearNodes.contains(temp))
                                {
                                    nearNodes.add(temp);
                                }
                            }
                        }
//                        如果该节点还不属于任何节点则把他加入该簇中
                        if(!nearNode.getOrdered())
                        {
                            nearNode.setOrdered(true);
                            cluster.getChildren().add(nearNode);
                            if(nearNode.getNoised())
                            {
                                nearNode.setNoised(false);
                            }
                        }
                    }

                }
//                将该簇加入簇集中
                clusters.add(cluster);
            }
        }

        return clusters;
    }
    public static void main(String[] args)
    {
        String input = "D:\\Daily\\数据挖掘\\实验四\\DataMine\\src\\DBScan\\input.CSV";
        String output = "D:\\Daily\\数据挖掘\\实验四\\DataMine\\src\\DBScan\\output.CSV";
        csvWriter(input,output);
        List<DBNode> sources = init(output);
        MINPTS = 2;
        EPS = 1;
//        获得聚类后的簇集
        List<DBNode> clusters = DBSCAN(sources);
//        遍历打印结果
        int b = 0;
        for (int i = 0; i < clusters.size(); i++) {
            DBNode cluster = clusters.get(i);
            int a = cluster.getChildren().size();
            b += a;
            System.out.print("分组"+(i+1)+"\t共计（"+a + "）个\t\t[");
            for (DBNode n: cluster.getChildren()) {
                System.out.print(n.getName()+",");
            }
            System.out.print("]\n");
        }
//        打印参数配置
        System.out.println("总数："+b+" 共 "+clusters.size()+" 组");
        System.out.println("噪声共 "+(sources.size() - b)+" 个");
        System.out.println("参数配置：MinPts = "+MINPTS+" Eps = "+EPS);
        System.out.println();
    }

}
