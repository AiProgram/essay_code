package graphIO;

import com.csvreader.CsvWriter;

public class CSVRecorder {
    private String csvPath="./csv_result/";

    /**
     * 将数据写入到csv文件中
     * @param csvFileName csv文件名称
     * @param data 数据，第一维是行，第二维是列
     */
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
