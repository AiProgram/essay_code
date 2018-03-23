package GraphIO;

import com.csvreader.CsvWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

public class CSVRecorder {
    private String csvPath="./csv_result/";
    public void writeToCSV(String csvFileName, String data[][]){
        try{
            CsvWriter csvWriter=new CsvWriter(csvPath+csvFileName);
            csvWriter.writeRecord(CSVCol.csvHeader);
            for(int i=0;i<data.length;i++)
            {
                csvWriter.writeRecord(data[i]);
            }
            csvWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
