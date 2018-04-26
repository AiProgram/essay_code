package graphIO;

public class CSVCol {
    /**
     *
     * 该类用于记录csv文件在记录时各个列的排布顺序
     */
    public  final int graphId =0;//表示的是在csvHeader中的索引
    public  final int nodeNum=1;
    public  final int edgeNum=2;
    public  final int startPoint=3;
    public  final int desPoint=4;
    public  final int maxDelay=5;
    public  final int graphFile=6;

    public  final int newAlgRunTime=7;
    public  final int newAlgCost=8;
    public  final int newAlgDelay=9;
    public  final int newAlgPath=10;

    public  final int ILPRunTime=11;
    public  final int ILPCost=12;
    public  final int ILPDelay=13;
    public  final int ILPPath=14;

    public final int costRatio=15;
    public final int delayRatio=16;


    public static int colNum=17;//列的总数量
    public static String csvHeader[]={
            "graphId","nodeNum","edgeNum","startPoint","desPoint","maxDelay","graphFile"
            ,"newAlgRunTime","newAlgCost","newAlgDelay","newAlgPath","ILPRunTime","ILPCost"
            ,"ILPDelay","ILPPath","costRatio","delayRatio"
    };
}