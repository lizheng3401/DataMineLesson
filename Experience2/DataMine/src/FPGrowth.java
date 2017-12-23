import com.csvreader.CsvReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class FPGrowth {

    static int  min_sup = 2;
    static List<List<String>>  all = new ArrayList<>();
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
            System.out.println("**********************");
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
                String[] strings = item.split(",");
                List<String> strings1 = new ArrayList<>();
                for (String s:strings) {
                    strings1.add(s);
                }
                strings1.add(topItem.getHerb());
                all.add(strings1);
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

    public static void getSubList(List<String> resourses, int nonius, ArrayList<String> childs, List<ArrayList<String>> results) {
        for (int i = nonius; i < resourses.size(); i++) {
            // 去掉自己本身
            if (childs.size() < resourses.size() - 1) {
                // 将数据源中的每个元素分别拿出来
                childs.add(resourses.get(i));
                // 将每个元素的集合作为元素放入结果集合
                results.add(new ArrayList<>(childs));
                // 递归向后移动
                getSubList(resourses, i + 1, childs, results);
                // 移除
                childs.remove(childs.size() - 1);
            }
        }
    }

    public static List<ArrayList<String>> getSub(List<String> list)
    {
        ArrayList<String> child = new ArrayList<>();
        List<ArrayList<String>> result = new ArrayList<>();
        getSubList(list, 0, child, result);
        System.out.println("真子集 " + result.size() + " 个");
        return result;
    }

    public static int getTime(List<String> sub, List<Prescription> prescriptions)
    {
        int num = 0;
        for (Prescription p: prescriptions) {
            List<String> pherb = p.getHerb();
            if(pherb.containsAll(sub))
            {
                num++;
            }
        }
        return num;
    }

    public static void showResult(ItemSets result, List<Prescription> prescriptions)
    {
        List<ArrayList<String>> subs = getSub(result.getHerbItems());
        Map<List<String>, Integer> map = new HashMap<>();

        for (List<String> sub:subs)
        {
            List<String> su = new ArrayList<>();
            List<String> others = new ArrayList<>();

            int num = getTime(sub, prescriptions);
            for (String te:sub) {
                su.add(te);
            }
            for (String s:result.getHerbItems()) {
                if(!sub.contains(s))
                {
                    others.add(s);
                }
            }
            su.add("#");
            su.addAll(others);
            map.put(su, num);
        }

        for (int i = 0; i < 10; i++) {

            int min = 1000;
            List<String> aim = null;
            for (List<String> str:map.keySet()) {
                int temp = map.get(str);
                if(0 < temp && temp < min)
                {
                    min = temp;
                    aim = str;
                }
            }
            System.out.print("[");
            for (String a:aim) {
                if(a.equals("#"))
                {
                    System.out.print("] => [");
                    continue;
                }
                System.out.print(a+",");
            }
            System.out.print("] : 2/"+ map.get(aim) + ": "+(2*1.0/map.get(aim))*100+"%");
            System.out.println();
            map.put(aim, 0);
        }

    }

    public static void main(String args[])
    {
        String path = "D://Daily/DataMine/src/data.csv";
        List<List<String>> source = init(path);
        FP_growth(source, null);
        int max = 0;
        int position = 0;
        for (int i = 0; i < all.size(); i++) {
            if(all.get(i).size() > max)
            {
                max = all.get(i).size();
                position = i;
            }
        }
        List<String> result = all.get(position);
        ItemSets key = new ItemSets();
        key.setHerbItems(result);
        List<Prescription> prescriptions = getPrescription(path);
        showResult(key, prescriptions);
    }
}
