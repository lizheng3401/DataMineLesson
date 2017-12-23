import apriori.Trancation;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Apriori {


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
    public static List<String> init(String path)
    {
       /**
        * 数据预处理，将输入数据格式化，并存入trancation.csv文件中
        *
        * */
        List<Prescription> prescriptions = getPrescription(path);

        /**
         * 统计所有的草药
         * */
        List<String> herbList = new ArrayList<>();
        for (Prescription p:prescriptions) {
            List<String> herbs = p.getHerb();
            Iterator it = herbs.iterator();
            while(it.hasNext())
            {
                String herb = it.next().toString();
                herbList.add(herb);
            }
        }

        String[] S = new String[herbList.size()];
        for(int i = 0; i < S.length; i++)
        {
            S[i] = herbList.get(i);
        }

        List<String> allherb = new ArrayList<>();

        for (int i = 0; i < S.length; i++) {
            if(S[i].equals(""))
            {
                continue;
            }

            for (int j = i + 1; j < S.length; j++) {
                if(S[i].equals(S[j]))
                {
                    S[j] = "";
                }
            }
            allherb.add(S[i]);
        }
        int totalNum = allherb.size();
        String[] items = new String[allherb.size()];
        for (int i = 0; i < allherb.size(); i++) {
            items[i] = allherb.get(i);
        }
        /**
         * 生成统计数据，构造事务数据
         * */
        List<Trancation> trancations = new ArrayList<>();

        for (Prescription p:prescriptions) {
            List<String> herb = p.getHerb();
            Trancation trancation = new Trancation();
            trancation.setName(p.getName());
            for (int i = 0; i < totalNum; i++) {
                for (String h: herb) {
                    if(items[i].equals(h))
                    {
                        trancation.getItem()[i] = 1;
                    }
                }
                if(i == herb.size())
                {
                    trancation.getItem()[i] = 0;
                }
            }

            trancations.add(trancation);
        }
        /**
         * 将药草数据（即交易数据）数据写入csv文件
         * */
        String csvFilePath = "D://Daily/DataMine/src/trancation.csv";
        try {
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("GBK"));

            String[] csvheader = new String[items.length + 2];
            csvheader[0] = "编号";
            csvheader[1] = "药名";

            for (int i = 0; i < items.length; i++) {
                csvheader[i+2] = items[i];
            }
            csvWriter.writeRecord(csvheader);

            for (int i = 0; i < trancations.size(); i++) {
                String[] csvContent = new String[items.length + 2];
                csvContent[0] = (i+1)+"";
                csvContent[1] = trancations.get(i).getName();
                for (int j = 0; j < items.length; j++) {
                    csvContent[j + 2] = trancations.get(i).getItem()[j] + "";
                }
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return herbList;
    }

    public static void main(String[] args) {
//        源数据文件路径
        String path = "D://Daily/DataMine/src/data.csv";
//        最小支持度
        int min_sup = 2;

        List<String> herbList = init(path);
//        获得频繁1-项集
        List<ItemSets> L_1_ItemSets = find_frequent_1_itemsets(herbList, min_sup);
//        频繁k-1项集
        List<ItemSets> L_k_1_ItemSets = L_1_ItemSets;
//        候选集Ck
        List<ItemSets> C_K_ItemSets = null;
        List<Prescription> prescriptionList = getPrescription(path);

        for (int k = 2; L_k_1_ItemSets.size() != 0; k++) {
//            连接和剪枝
            C_K_ItemSets = apriori_gen(L_k_1_ItemSets);
//            对候选项集计数
            for(ItemSets it: C_K_ItemSets)
            {
                List<String> temp = it.getHerbItems();
                for (Prescription p: prescriptionList) {
                    if(p.getHerb().containsAll(temp))
                    {
                        it.times++;
                    }
                }
            }
            L_k_1_ItemSets.clear();
//            从候选项集中找到频繁项集
            System.out.println("频繁  " + k + " 项集:");
            for (ItemSets it: C_K_ItemSets
                 ) {

                if(it.getTimes() >= min_sup)
                {
                    L_k_1_ItemSets.add(new ItemSets(it.getHerbItems(), it.getTimes()));
                    it.showHerbItems();
                    System.out.println("times: "+it.getTimes());
                }
            }
        }
    }

    public static List<ItemSets> find_frequent_1_itemsets(List<String> herbList, int min_sup)
    {
//        获取频繁1-项集
        String[] S = new String[herbList.size()];
        for(int i = 0; i < S.length; i++)
        {
            S[i] = herbList.get(i);
        }

        List<ItemSets> itemSetsList = new ArrayList<ItemSets>();
        System.out.println("频繁1项集");
//        计数
        for (int i = 0; i < S.length; i++) {
            if(S[i] == "")
            {
                continue;
            }

            int k = 1;
            for (int j = i + 1; j < S.length; j++) {
                if(S[i].equals(S[j]))
                {
                    S[j] = "";
                    k++;
                }
            }
            if(k >= min_sup)
            {
                List<String> herbItems = new ArrayList<String>();
                herbItems.add(S[i]);
                itemSetsList.add(new ItemSets(herbItems, k));
                System.out.println(S[i] + ": " + k);
            }
        }
        return itemSetsList;
    }

    public static List<ItemSets> apriori_gen(List<ItemSets> L_k_1_ItemSets)
    {
//        连接和剪枝
        List<ItemSets> C_K_ItemSets = new ArrayList<>();
        ItemSets I1 = null;
        ItemSets I2 = null;
        System.out.println("产生候选 "+(L_k_1_ItemSets.get(0).getHerbItems().size() + 1) + " 项集");
        for (int i = 0; i < L_k_1_ItemSets.size(); i++) {
            I1 = L_k_1_ItemSets.get(i);
            for (int j = i+1; j < L_k_1_ItemSets.size(); j++) {
                I2 = L_k_1_ItemSets.get(j);
//                连接
                if(can_link(I1.getHerbItems(),I2.getHerbItems()))
                {
                    List<String> c = link(I1.getHerbItems(),I2.getHerbItems());
//                    剪枝
                    if(!has_infrequent_subset(c, L_k_1_ItemSets))
                    {
                        C_K_ItemSets.add(new ItemSets(c,0));
                    }
                }
            }
        }
        return C_K_ItemSets;
    }

    public static List<String> link(List<String> herbItem1, List<String> herbItem2)
    {
//        将两个String集合连接
        List<String>  re = new ArrayList<>(herbItem1);
        String I_k_1 = herbItem2.get(herbItem2.size() - 1);
        re.add(I_k_1);
        return re;
    }
    public static Boolean can_link(List<String> herbItem1, List<String> herbItem2)
    {
//        判断两个String 集合是否能进行连接
        String s1 = null;
        String s2 = null;
        int i = 0;
        if(herbItem1.size() == 1 | herbItem2.size() == 1)
        {
            return true;
        }
        for (; i < herbItem1.size(); i++) {
            s1 = herbItem1.get(i);
            s2 = herbItem2.get(i);
            if(!s1.equals(s2))
            {
                break;
            }
        }
        if(i == herbItem2.size() - 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public static Boolean has_infrequent_subset(List<String> c, List<ItemSets> L_k_1_ItemSets)
    {
//        除去子集中不在L_k_1的候选项，即频繁项的子集也是频繁的
        List<String> temp = null;

        for (int i = 0; i < c.size(); i++) {
            temp = new ArrayList<>(c);
            temp.remove(i);

            for (int j = 0; j < L_k_1_ItemSets.size(); j++) {
                List<String> a = L_k_1_ItemSets.get(j).getHerbItems();
                if(temp.containsAll(a))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
