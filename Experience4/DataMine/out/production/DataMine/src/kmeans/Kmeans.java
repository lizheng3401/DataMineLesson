package kmeans;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


public class Kmeans {
    public static ArrayList<String[]> csvRead(String path)
    {
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

    public static void main(String args[])
    {
        String input = "D:\\Daily\\DataMine\\src\\kmeans\\input.CSV";
        String output = "D:\\Daily\\DataMine\\src\\kmeans\\output.CSV";
        csvWriter(input,output);
    }
}
