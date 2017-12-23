import com.csvreader.CsvReader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class Test2 {

    public static Boolean isTheSame(List<List<String>> sources)
    {
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
        double re = 0;
        int last = sources.get(0).size() - 1;

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

        return re;
    }

    public static int selectMaxAttribute(List<List<String>> sources, List<String> attributes)
    {
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
        Map<String, List<List<String>>> subSources = new HashMap<>();
        List<String> values = getValue(sources, selectedAttr);

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
        IDTreeNode node = new IDTreeNode();
        if(isTheSame(sources))
        {
            node.setHerb(sources.get(0).get(sources.get(0).size() - 1));
            return node;
        }

        if(attributes.size() == 0)
        {
            return node;
        }

        int selectedAttr = selectMaxAttribute(sources, attributes);
        node.setHerb(attributes.get(selectedAttr));
        attributes.remove(selectedAttr);

        Map<String, List<List<String>>> subSources = getSubSources(sources, selectedAttr);

        for (String k:subSources.keySet()) {
            Map<String, IDTreeNode> child = node.getDemo();
            child.put(k,DecisionTree(subSources.get(k), attributes));
            node.setDemo(child);
        }

        return node;

    }

    public static String testRe(IDTreeNode node, List<String> sources, List<String> attrs)
    {
        List<String> re = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            String temp =sources.get(i);
            if(temp.equals("1"))
            {
                re.add(attrs.get(i));
            }
        }
        if(node.getDemo().size() == 0)
        {
            return node.getHerb();
        }
        else
        {
            if(re.contains(node.getHerb()))
            {
                return testRe(node.getDemo().get("1"), sources, attrs);
            }
            else
            {
                return testRe(node.getDemo().get("0"), sources, attrs);
            }

        }
    }
    public static void main(String args[])
    {
        String path = "D:\\Daily\\DataMine\\src\\category.csv";
        List<List<String>> allDatas = new ArrayList<>();
        List<String> allAttributes = new ArrayList<>();

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

        IDTreeNode root = DecisionTree(allDatas, allAttributes);



        te.remove(te.size() - 1);

        int right = 0;
        int error = 0;
        int total = te.size();

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

        System.out.println("right: "+right);
        System.out.println("error: "+error);
        System.out.println("total: "+total);
        System.out.println("Right Percent:"+1.0*right/total *100+"%");
    }
}
