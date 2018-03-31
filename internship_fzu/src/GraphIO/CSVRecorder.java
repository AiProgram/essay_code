package GraphIO;

import com.csvreader.CsvWriter;

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
