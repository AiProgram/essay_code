package graphIO;

public class CSVCol {
    /**
     *
     * 该类用于记录csv文件在记录时各个列的排布顺序
     */
    public  static int graphId =0;//表示的是在csvHeader中的索引
    public  static int nodeNum=1;
    public  static int edgeNum=2;
    public  static int startPoint=3;
    public  static int sinkPoint=4;
    public  static int maxComVertex=5;
    public  static int graphFile=6;

    public  static int newAlgRunTime=7;
    public  static int newAlgResult=8;

    public  static int ILPRunTime=9;
    public  static int ILPResult=10;

    public static int mwldRunTime=11;
    public static int mwldResult=12;

    public static int colNum=13;//列的总数量
    public static String csvHeader[]={"graphId","nodeNum","edgeNum","startPoint","sinkPoint","maxComVertex","graphFile"
    ,"newAlgRunTime","newAlgResult", "ILPRunTime","ILPResult","mwldRunTime","mwldResult","colNum"
    };
}
