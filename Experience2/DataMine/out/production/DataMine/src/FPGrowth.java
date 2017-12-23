import com.csvreader.CsvReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class FPGrowth {

    static int  min_sup = 2;

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
    public static List<List<String>> init(String path)
    {
        /**
         * 数据预处理，将输入数据格式化.
         * */
        List<Prescription> prescriptions = getPrescription(path);
        List<List<String>> itemSetsList = new ArrayList<>();
        for (Prescription p: prescriptions) {
            itemSetsList.add(p.getHerb());
        }
        return itemSetsList;
    }

    public static List<TreeNode> getHerbTop(List<List<String>> source)
    {
//        按照支持度由大到小对传入的药方内容重新排序
        List<TreeNode> top = new ArrayList<TreeNode>();
        List<String> s = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            s.addAll(source.get(i));
        }

        for (int i = 0; i < s.size(); i++) {
            String str = s.get(i);
            if(str.equals(""))
            {
                continue;
            }

            int k = 1;
            for (int j = i + 1; j < s.size(); j++) {
                if(str.equals(s.get(j)))
                {
                    s.set(j, "");
                    k++;
                }
            }

            if(k >= min_sup)
            {
//                若大于最小支持度，则加入序列中
                TreeNode tn = new TreeNode();
                tn.setHerb(str);
                tn.setNum(k);
                top.add(tn);
            }
        }

        List<TreeNode> topc = new ArrayList<>();
//        对得到的序列进行排序
        while(topc.size() != top.size())
        {
            int max = 0;
            int position = 0;
            for (int i = 0; i < top.size(); i++) {
                int temp = top.get(i).getNum();
                if( temp > max)
                {
                    max = temp;
                    position = i;
                }
            }
            TreeNode te = new TreeNode();
            te.setNum(top.get(position).getNum());
            te.setHerb(top.get(position).getHerb());
            topc.add(te);
            top.get(position).setNum(0);
        }
        return topc;
    }

    public static void sort(List<String> re, List<TreeNode> top)
    {
//        利用键值对存储排序后的药方
        Map<Integer, String> map = new HashMap<Integer, String>();
        int j = 0;
        for (int i = 0; i < top.size(); i++) {
            String temp = top.get(i).getHerb();
            for (int k = 0; k < re.size(); k++) {
                if(temp.equals(re.get(k)))
                {
                    map.put(j, temp);
                    j++;
                }
            }
        }

        re.clear();
//        将键值对中的信息按照排序后的位置管理存入原先的List中
        for (int i = 0; i < map.size(); i++) {
            re.add(map.get(i));
        }
    }
    public static TreeNode insertTree(TreeNode root, List<String> it, List<TreeNode> top)
    {
        if(it.size() <= 0)
            return null;

        String first = it.get(0);
//        查找子树时候有该节点
        TreeNode n = root.findChild(first);
//        没有就新增
        if(n == null)
        {
            n = new TreeNode();
            n.setHerb(first);
            n.setNum(1);
            n.setParent(root);
            root.addChild(n);

//            将该节点链接至同名节点上
            for (TreeNode tn:top) {
                if(tn.getHerb().equals(first))
                {
                    while(tn.getNextNode()!=null){
                        tn = tn.getNextNode();
                    }
                    tn.setNextNode(n);
                    break;
                }
            }
        }
        else {
//            有的话就计数加1
            n.setNum(n.getNum() + 1);
        }

//        把已经计数过的字符串移除
        it.remove(0);
//        剩下的递归的插入树中
        insertTree(n, it, top);
        return root;
    }

    public static TreeNode createFPTree(List<List<String>> source, List<TreeNode> top)
    {
        if(source.size()<=0){
            return null;
        }
        TreeNode root = new TreeNode();
//        循环插入所有的事务
        for (List<String> re: source) {
            sort(re, top);
            insertTree(root,re, top);
        }
        return root;
    }

    public static void goToRoot(TreeNode node,List<String> newRecord)
    {
//        从节点递归至根节点
        if( node.getParent() == null)
            return;
        String herb=node.getHerb();
        newRecord.add(herb);
        goToRoot(node.getParent(),newRecord);
    }

    public static void FP_growth(List<List<String>> sources, String item)
    {
//        FP生成树
        List<List<String>> newRecords = new ArrayList<>();
//      创建条件生成树
        List<TreeNode> top = getHerbTop(sources);
        TreeNode TreeRoot = createFPTree(sources, top);

        if(top.size() <= 0 || TreeRoot == null)
        {
            System.out.println("***********");
            return;
        }

        if(item != null)
        {
            for (int i = top.size() - 1; i >= 0 ; i--) {
                TreeNode topItem = top.get(i);
                String herb = topItem.getHerb();
                int num = 0;
                while (topItem.getNextNode() != null)
                {
                    topItem = topItem.getNextNode();
                    num = num + topItem.getNum();
                }

                System.out.println(topItem.getHerb()+","+item+": "+num);
            }
        }

//       递归生成FP-Tree
        for (int i = top.size() - 1; i >= 0; i--) {
            TreeNode topItem = top.get(i);
            String herb;
            if(item == null)
            {
                herb = topItem.getHerb();
            }
            else{
                herb = topItem.getHerb() + ","+item;
            }

            while(topItem.getNextNode() != null)
            {
                topItem = topItem.getNextNode();
                int num = topItem.getNum();
                for (int j = 0; j < num; j++) {
                    List<String> record = new ArrayList<>();
                    goToRoot(topItem.getParent(), record);
                    newRecords.add(record);
                }
            }

            FP_growth(newRecords, herb);
        }
    }


    public static void main(String args[])
    {
        String path = "D://Daily/DataMine/src/data.csv";
        List<List<String>> source = init(path);
        FP_growth(source, null);
    }
}
