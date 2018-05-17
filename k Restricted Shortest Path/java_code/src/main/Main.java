package main;

import algorithm.ILPAlgorithm;
import algorithm.KRSPAlgBaseOnDelay;
import algorithm.kRSPResult;
import graphIO.CSVCol;
import graphIO.CSVRecorder;
import graphIO.GraphRandGen;
import graphIO.GraphWriter;
import graphStructure.MyGraph;

import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String args[]){

        KRSPAlgBaseOnDelay algorithm=new KRSPAlgBaseOnDelay();
        int nodeNum=1000;
        int edgeNum=100000;
        int maxDelay=20;
        int spNum=5;
        int startPoint=20;
        int desPoint=40;
        int times=50;
        long startTime;
        long  endTime;



        GraphWriter writer=new GraphWriter();
        Random random=new Random();


        String csvData[][]=new String[times][CSVCol.colNum];
        ILPAlgorithm ilpAlgorithm=new ILPAlgorithm();
        CSVCol col=new CSVCol();
        for(int i=0;i<times;i++)
        {
            GraphRandGen graphRandGen=new GraphRandGen();
            MyGraph myGrap=graphRandGen.generateRandomGraph(nodeNum,edgeNum);

            int tmp;
            startPoint=random.nextInt(nodeNum);
            while((tmp=random.nextInt(nodeNum))==startPoint);
            desPoint=tmp;

            String graphFileName=nodeNum+"-"+edgeNum+"-"+i+".json";
            writer.saveGraphToJson(myGrap,graphFileName);

            csvData[i][col.maxDelay]=Integer.toString(maxDelay);
            csvData[i][col.graphId]=Integer.toString(i);
            csvData[i][col.startPoint]=Integer.toString(startPoint);
            csvData[i][col.desPoint]=Integer.toString(desPoint);
            csvData[i][col.graphFile]=graphFileName;

            startTime=System.currentTimeMillis();
            kRSPResult result=ilpAlgorithm.solveWithGLPK(myGrap,startPoint,desPoint,spNum,maxDelay);
            endTime=System.currentTimeMillis();
            if(result!=null) {
                System.out.println(result.costSum + "   " + result.delaySum);
                System.out.println(result.paths);
                csvData[i][col.ILPCost] = Double.toString(result.costSum);
                csvData[i][col.ILPDelay] = Double.toString(result.delaySum);
                csvData[i][col.ILPPath]=result.paths.toString();
            }
            csvData[i][col.ILPRunTime]=Double.toString((endTime-startTime)/1000.0);
            System.out.println("----------------");


            startTime=System.currentTimeMillis();
            List<List<Integer>>ksp=algorithm.getKSP(myGrap,startPoint,desPoint,spNum,maxDelay);
            endTime=System.currentTimeMillis();
            if(ksp!=null)
            {
                System.out.println(ksp);
                int cost=algorithm.countAttr(myGrap,ksp,KRSPAlgBaseOnDelay.Attr.cost);
                int delay=algorithm.countAttr(myGrap,ksp,KRSPAlgBaseOnDelay.Attr.delay);
                System.out.println("cost: "+cost);
                System.out.println("delay: "+delay);
                csvData[i][col.newAlgCost]=Integer.toString(cost);
                csvData[i][col.newAlgDelay]=Integer.toString(delay);
                csvData[i][col.costRatio]=Double.toString(cost/result.costSum);
                csvData[i][col.delayRatio]=Double.toString(delay/result.delaySum);
                csvData[i][col.newAlgPath]=ksp.toString();
            }else{
                System.out.println("没有结果");
            }
            csvData[i][col.newAlgRunTime]=Double.toString((endTime-startTime)/1000.0);
            System.out.println("++++++++++++++++");
            csvData[i][col.nodeNum]=Integer.toString(nodeNum);
            csvData[i][col.edgeNum]=Integer.toString(edgeNum);

            if(i%10==0)System.gc();
        }
        CSVRecorder recorder=new CSVRecorder();
        recorder.writeToCSV("data.csv",csvData);
    }
}
