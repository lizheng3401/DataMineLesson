import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class datum {
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
    public static Map<String, Integer> getTotal(String path)
    {
//        将CSV文件的数据格式化为Prescription（药方）集合
        ArrayList<String[]> csvFileList = csvRead(path);
        Map<String, Integer> herb = new HashMap<>();

        for (int row = 0, i = 0; row < csvFileList.size(); row++, i++) {
            String[] temp = csvFileList.get(row)[2].split("；");
            for (int j = 0; j < temp.length; j++) {
                String tmp = temp[j].split("：")[0];
                if(!herb.containsKey(tmp))
                {
                    herb.put(tmp, 1);
                }
                else
                {
                    int num = herb.get(tmp) + 1;
                    herb.put(tmp, num);
                }
            }
        }
        return herb;
    }

    public static void csvWriter(String output,Map<String, Integer> sources)
    {
//        将读入的数据格式化后存入csv文件中
        try {
            CsvWriter csvWriter = new CsvWriter(output, ',', Charset.forName("GBK"));
            int length = 2;
            String[] csvheader = new String[length];
            csvheader[0] = "药名";
            csvheader[1] = "次数";

            csvWriter.writeRecord(csvheader);

            for (String str:  sources.keySet()) {
                String[] csvContent = new String[length];
                csvContent[0] = str;
                csvContent[1] = sources.get(str) + "";
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void work(String input, String output)
    {
        Map<String, Integer> herb = getTotal(input);
        for (String str:herb.keySet()) {
            System.out.println(str+": "+herb.get(str));
        }
        System.out.println("共计："+herb.size()+"种");
        csvWriter(output, herb);
    }
    public static void main(String[] args)
    {
//        文件路径
        String input = "D:\\Daily\\数据挖掘\\实验一\\DataMine\\src\\data.CSV";
        String inputS = "D:\\Daily\\数据挖掘\\实验一\\DataMine\\src\\inputS.CSV";
        String output = "D:\\Daily\\数据挖掘\\实验一\\DataMine\\src\\output.CSV";
        String outputS = "D:\\Daily\\数据挖掘\\实验一\\DataMine\\src\\outputS.CSV";

        System.out.println("******首次统计********");
        work(inputS, outputS);
        System.out.println("******处理后统计******");
        work(input, output);

    }
}
