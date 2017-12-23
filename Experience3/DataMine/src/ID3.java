import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;


class Init{

    static Map<String, Integer> mapType = new HashMap<>();
    static Map<String, Integer> mapHerb = new HashMap<>();

    public static ArrayList<String[]> csvRead(String path)
    {
//        读取csv文件的内容并返回
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
    public static List<Prescription> getPrescription(String path)
    {
//        将CSV文件的数据格式化为Prescription（药方）集合
        ArrayList<String[]> csvFileList = csvRead(path);
        /**
         * 获取每个草药的药方、药名、类型
         *
         * */
        String content,name,type;
        List<Prescription> prescriptions = new ArrayList<>();
        for (int row = 0, i = 0; row < csvFileList.size(); row++, i++) {
            name = csvFileList.get(row)[0];
            type = csvFileList.get(row)[1];
            content = csvFileList.get(row)[2];
            prescriptions.add(new Prescription(name,type,content));
        }

        return prescriptions;
    }
    public static void init(String path)
    {
//        将数据格式化后写入csv文件中
        int type = 0;
        int h = 0;
        List<Prescription> prescriptions = getPrescription(path);
        List<String> demo = new ArrayList<>();
//       遍历统计草药种类和分类种类
        for (Prescription p:prescriptions) {
            List<String> herbs = p.getHerb();
            if(!mapType.containsKey(p.getType()))
            {
                mapType.put(p.getType(), type);
                type++;
            }
            Iterator it = herbs.iterator();
            while(it.hasNext())
            {
                String herb = it.next().toString();
                if(!mapHerb.containsKey(herb))
                {
                    mapHerb.put(herb, h);
                    demo.add(herb);
                    h++;
                }
            }
        }

//    格式化数据并写入csv文件
        String csvFilePath = "D://Daily/DataMine/src/cate.csv";
        try {
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("GBK"));

            String[] csvheader = new String[mapHerb.size() + 1];
            csvheader[mapHerb.size()] = "类型";

            int i = 0;
            for (String s:demo) {
                csvheader[i] = s;
                i++;
            }
            csvWriter.writeRecord(csvheader);
            int length = mapHerb.size() + 1;
            for (int k = 0; k < prescriptions.size(); k++) {
                String[] csvContent = new String[length];
                Prescription p = prescriptions.get(k);
                for (int j = 0; j < csvContent.length; j++) {
                    csvContent[j] = "0";
                }
                csvContent[length - 1] = mapType.get(p.getType())+"";
                for (String he:p.getHerb()) {
                    csvContent[mapHerb.get(he)] = "1";
                }
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

public class ID3 {

    public static Boolean isTheSame(List<List<String>> sources)
    {
//        判断传入的数据是否为同一个类型，是返回true，否返回false
        String key = sources.get(0).get(sources.get(0).size() - 1);
        for (List<String> ls: sources) {
            if(!ls.get(ls.size() - 1).equals(key))
            {
                return false;
            }
        }
        return  true;
    }

    public static List<String> getValue(List<List<String>> sources, int position)
    {
//        获得传入数据的指定属性的值的种类
        List<String> values = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            if(!values.contains(sources.get(i).get(position)))
            {
                values.add(sources.get(i).get(position));
            }
        }

        return values;
    }


    public static double getGain(List<List<String>> sources, int position)
    {
//        获取对应属性的gain值，
        double re = 0;
        int last = sources.get(0).size() - 1;
//    获取到该数据集的类型的信息熵
        List<String> typeValues = getValue(sources, last);
        for (String value: typeValues) {
            int num = 0;
            for (List<String> temp: sources) {
                if(temp.get(last).equals(value))
                {
                    num++;
                }
            }

            re += -(1.0*num/sources.size())*Math.log(1.0*num/sources.size())/Math.log(2);
        }
//    获取属性的不同值的信息熵
        List<String> attrValues = getValue(sources, position);

        for (String s: attrValues) {
            List<List<String>> temp = new ArrayList<>();

            for (List<String> ls: sources) {
                if(ls.get(position).equals(s))
                {
                    temp.add(ls);
                }
            }

            List<String> subTypeValues = getValue(temp, last);

            for (String v:subTypeValues) {
                int num = 0;
                for (List<String> sub: temp) {
                    if (sub.get(last).equals(v))
                    {
                        num++;
                    }
                }

                re += -1.0*temp.size()/sources.size()*(-(1.0*num/temp.size())*Math.log(1.0*num/temp.size())/Math.log(2));
            }
        }
//  返回最终的信息熵
        return re;
    }

    public static int selectMaxAttribute(List<List<String>> sources, List<String> attributes)
    {
//        选择剩余未分类属性中最大信息熵的属性
        double maxGain = -1.0;
        int position = 0;

        for (int i = 0; i < attributes.size() - 1; i++) {
            double temp = getGain(sources, i);
            if(temp > maxGain)
            {
                maxGain = temp;
                position = i;
            }
        }
        return position;
    }


    public static Map<String, List<List<String>>> getSubSources(List<List<String>> sources,int selectedAttr)
    {
//        根据分类属性划分数据的子集
        Map<String, List<List<String>>> subSources = new HashMap<>();
        List<String> values = getValue(sources, selectedAttr);
//      根据属性值划分数据集
        for (String s: values) {
            List<List<String>> temp = new ArrayList<>();
            for (List<String> item: sources) {
                if(item.get(selectedAttr).equals(s))
                {
                    temp.add(item);
                }
            }
            subSources.put(s, temp);
        }
//     将该属性对应值从数据集中移除
        for (String s: subSources.keySet()) {
            List<List<String>> a = subSources.get(s);
            for (List<String> c: a) {
                c.remove(selectedAttr);
            }
        }
        return subSources;
    }

    public static IDTreeNode DecisionTree(List<List<String>> sources, List<String> attributes)
    {
//        生成决策树
        IDTreeNode node = new IDTreeNode();
//        如果数据集均属于统一个类型，返回它即为叶子节点
        if(isTheSame(sources))
        {
            node.setHerb(sources.get(0).get(sources.get(0).size() - 1));
            return node;
        }

        if(attributes.size() == 0)
        {
            return node;
        }
        //选择分类属性
        int selectedAttr = selectMaxAttribute(sources, attributes);
//        设置节点名称
        node.setHerb(attributes.get(selectedAttr));
//        把该属性从属性list中删除
        attributes.remove(selectedAttr);
//         根据被选属性值划分数据集
        Map<String, List<List<String>>> subSources = getSubSources(sources, selectedAttr);
//          递归处理剩余属性
        for (String k:subSources.keySet()) {
            Map<String, IDTreeNode> child = node.getDemo();
            child.put(k,DecisionTree(subSources.get(k), attributes));
            node.setDemo(child);
        }
//最后返回节点。
        return node;

    }

    public static String testRe(IDTreeNode node, List<String> sources, List<String> attrs)
    {
//        遍历决策树，返回分类结果
//        把传入的01字符串转化为中药名称
        List<String> re = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            String temp =sources.get(i);
            if(temp.equals("1"))
            {
                re.add(attrs.get(i));
            }
        }
//        若该节点为叶子节点，则返回其节点名称即为分类结果
        if(node.getDemo().size() == 0)
        {
            return node.getHerb();
        }
        else
        {
//            否则判断该输入中药是否含该节点名称
            if(re.contains(node.getHerb()))
            {
//                若含则返回左子树
                return testRe(node.getDemo().get("1"), sources, attrs);
            }
            else
            {
//                 否则则返回右子树
                return testRe(node.getDemo().get("0"), sources, attrs);
            }

        }
    }
    public static void main(String args[])
    {
        Init.init("D:\\Daily\\DataMine\\src\\data.CSV");
        String path = "D:\\Daily\\DataMine\\src\\cate.csv";
        List<List<String>> allDatas = new ArrayList<>();
        List<String> allAttributes = new ArrayList<>();
//        从csv文件中读入数据
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
        List<String> attrs = new ArrayList<>();
        for (int i = 0; i < header.length; i++) {
            allAttributes.add(header[i]);
            attrs.add(header[i]);
        }
//      将数据格式化
        List<List<String>> te = new ArrayList<>();
        for (String[] strings:csvFileList) {
            List<String> temp = new ArrayList<>();
            List<String> temp2 = new ArrayList<>();
            for (int i = 0; i < strings.length; i++) {
                temp.add(strings[i]);
                temp2.add(strings[i]);
            }
            allDatas.add(temp);
            te.add(temp2);
        }
//      构造决策树
        IDTreeNode root = DecisionTree(allDatas, allAttributes);


//      测试结果
        te.remove(te.size() - 1);

        int right = 0;
        int error = 0;
        int total = te.size();
//        以所有的训练数据集为测试集测试
        for (List<String> item: te) {
            String type = item.get(item.size() - 1);
            item.remove(item.size() - 1);
            if(testRe(root, item,attrs).equals(type))
            {
                right++;
            }
            else
            {
                error++;
            }
        }
//输出结果
        System.out.println("right: "+right);
        System.out.println("error: "+error);
        System.out.println("total: "+total);
        System.out.println("Right Percent:"+1.0*right/total *100+"%");
    }
}
