package kmeans;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


class knode{
    private String name = null;
    private int ID = -1;
    private double[] attrs = null;
    private knode type = null;
    private List<knode> children = new ArrayList<>();
    private Boolean isCenterPoint = false;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public double[] getAttrs() {
        return attrs;
    }

    public void setAttrs(double[] attrs) {
        this.attrs = attrs;
    }

    public knode getType() {
        return type;
    }

    public void setType(knode type) {
        this.type = type;
    }

    public List<knode> getChildren() {
        return children;
    }

    public void setChildren(List<knode> children) {
        this.children = children;
    }

    public Boolean getCenterPoint() {
        return isCenterPoint;
    }

    public void setCenterPoint(Boolean centerPoint) {
        isCenterPoint = centerPoint;
    }

    public knode() {

    }
}

public class Kmeans {

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

    public static List<knode> init(String path)
    {
//        读取处理后的数据，并格式化后返回
        List<knode> sources = new ArrayList<>();

        ArrayList<String[]> csvFileList =  csvRead(path);

        for (int row = 0; row < csvFileList.size(); row++) {
            String[] temp = csvFileList.get(row);
            double[] attrs = new double[temp.length - 1];

            knode node = new knode();
            node.setID(row);
            node.setName(temp[0]);
            for (int i = 1; i < temp.length; i++) {
                attrs[i-1] = Integer.parseInt(temp[i])*1.0;
            }
            node.setAttrs(attrs);
            sources.add(node);
        }

        return sources;
    }

    public static List<Integer> getRandom(int k, int max)
    {
//        获得k个不大于数据长度的随机数
        List<Integer> re = new ArrayList<>();
        for (int i = 0; i < k; ) {
            int tmp = (int)(Math.random() * max);
            if(!re.contains(tmp))
            {
                re.add(tmp);
                i++;
            }
        }

        return re;
    }

    public static double getDistance(double[] a, double[] b)
    {
//        求取两个节点之间的距离
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += (a[i]-b[i]) * (a[i]-b[i]);
        }

        return result;
    }

    public static void getCate(knode node, List<knode> tmp)
    {
//        将该节点和其他k个中心点之间的距离比较，并将其花费为其中一个簇中
        double min = 1000.0;

        for (knode type: tmp) {

            double temp = getDistance(node.getAttrs(), type.getAttrs());
            if( min > temp)
            {
                min = temp;
                node.setType(type);
            }
        }

    }
    public static List<knode> kmean(List<knode> tmp, List<knode> sources)
    {
//        获取每个簇的孩子节点
        List<knode> ktmp = new ArrayList<>();

        for (knode node: tmp) {
            knode a = new knode();
            a.setID(100);
            a.setAttrs(node.getAttrs());
            a.setCenterPoint(true);
            ktmp.add(a);
        }

        for (knode node: sources) {
            getCate(node, ktmp);
            for (knode type: ktmp) {
                if(node.getType().equals(type))
                {
                    type.getChildren().add(node);
                    break;
                }
            }
        }
//        打印出每个簇的孩子节点
        for (int i = 0; i < ktmp.size(); i++) {
            knode k = ktmp.get(i);
            System.out.print("分组"+i+": \t[");
            for (knode node: k.getChildren()) {
                System.out.print(node.getID()+",");
            }
            System.out.print("]\n");
        }
        return ktmp;
    }

    public static double[] getAverage(knode node)
    {
//      获得簇中的所有节点的平均值
        double[] all = new double[node.getAttrs().length];
        for (int i = 0; i < all.length; i++) {
            all[i] = 0.0;
        }
        for (knode child: node.getChildren()) {
            double[] a = child.getAttrs();
            for (int i = 0; i < a.length; i++) {
                all[i] += a[i];
            }
        }
        int num = node.getChildren().size();
        for (int i = 0; i < all.length; i++) {
            all[i] = all[i] / num;
        }

        return all;
    }

    public static List<knode> getCenterPoint( List<knode> tempRe )
    {
//        获取新一轮的中心点
        List<knode> tmp = new ArrayList<>();

        for ( knode typeNode: tempRe ) {
            double[] centerNode = getAverage(typeNode);
            knode node = new knode();
            node.setID(100);
            node.setAttrs(centerNode);
            node.setCenterPoint(true);
            tmp.add(node);
        }

        return tmp;
    }

    public static void showCenterPoint(List<knode> newCneterPoint)
    {
        System.out.println("*****新的中心点*****");
        for (knode node:newCneterPoint) {
            System.out.print("[");
            double[] b = node.getAttrs();
            for (int i = 0; i < b.length; i++) {
                System.out.print(b[i]+",");
            }
            System.out.print("]");
            System.out.println();
        }
    }


    public static Boolean isEnd(List<knode> newCenterPoint, List<knode> centerPoint)
    {
//        计算两个中心点之间的误差，等于0则结束迭代，否则进入下一次迭代
        double error = 0.0;
        for (int i = 0; i < newCenterPoint.size(); i++) {
            double[] newNode = newCenterPoint.get(i).getAttrs();
            double[] node = centerPoint.get(i).getAttrs();
            error += getDistance(newNode, node);
        }
        System.out.println("误差: "+ error);
        if(error == 0)
        {
            return true;
        }else{
            return false;
        }
    }
    public static void showResult(List<knode> tempRe)
    {
//        打印最后的结果
        for (int i = 0; i < tempRe.size(); i++) {
            knode node = tempRe.get(i);
            System.out.print("class "+ (i+1) + "（共计 "+ node.getChildren().size() + " 个）" +": \t[" );
            for (knode n: node.getChildren()) {
                System.out.print(n.getName()+",");
            }
            System.out.print("]");
            System.out.println();
        }
    }
    public static void main(String args[])
    {
//        数据预处理
        String input = "D:\\Daily\\DataMine\\src\\kmeans\\input.CSV";
        String output = "D:\\Daily\\DataMine\\src\\kmeans\\output.CSV";
        csvWriter(input,output);
//        设置k值
        int k = 10;
        List<knode> sources = init(output);
//        获取初次的随机中心点
        List<Integer> rand = getRandom(k, sources.size());
        List<knode> centerPoint = new ArrayList<>();

        for (int id: rand) {
            knode n = new knode();
            n.setID(100);
            n.setAttrs(sources.get(id).getAttrs());
            n.setCenterPoint(true);
            centerPoint.add(n);
        }
//        开始迭代
        for(int i = 1 ; ; i++)
        {
//            获取本次聚类结果
            List<knode> tempRe = kmean(centerPoint, sources);
//            获取新的中心点
            List<knode> newCenterPoint = getCenterPoint(tempRe);
            System.out.println("迭代次数 = " + i);
//            判断两次中心点是否移动，若未移动，结束迭代，否则进入下一次迭代
            if(isEnd(newCenterPoint, tempRe))
            {
                System.out.println("k值 = " + k);
                showResult(tempRe);
                break;
            }
            centerPoint = newCenterPoint;
        }
    }
}
