package main;

import alg.ILP.JavaLPAlg;
import alg.NewAlg.NewAlg;
import graphIO.CSVCol;
import graphIO.CSVRecorder;
import graphIO.GraphRandomGenerator;
import myGraph.ILPGraph;
import myGraph.MyGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static util.Util.getSPWeight;
import static java.lang.StrictMath.round;

public class Main {
    public static void main(String args[]){
        int time=50;
        String resultArr[][]=new String[time][CSVCol.colNum];//用于记录算法运行数据记录进入csv表格中
        long startTime;
        long endTime;
        Random random=new Random();

        List<Integer> wrongList=new ArrayList<>();
        List<Integer> wrongList2=new ArrayList<>();
        for(int t=0;t<time;t++) {
            System.out.println("第 "+(t+1)+" 遍");
            GraphRandomGenerator randomGenerator = new GraphRandomGenerator();
//            String jsonStr= JavaLPAlg.readJsonGraph("graph.json");
//            myGraph myGraph=JavaLPAlg.parseJsonToGraph(jsonStr);
            MyGraph myGraph=randomGenerator.generateRandomGraph(1000,100000);
            myGraph.startPoint = random.nextInt(myGraph.nodeNum);
            int tmp;
            while((tmp=random.nextInt(myGraph.nodeNum))==myGraph.startPoint);//起点和
            myGraph.sinkPoint=tmp;
            myGraph.maxComVertex = 12;

            MyGraph graph=null;
            double newResult=-1;
            startTime=System.currentTimeMillis();
            try {
                graph = NewAlg.getResidualGraph(myGraph, 1);
                NewAlg.RSPResult result = NewAlg.RSPNoRecrusive(graph,myGraph);
                List<Integer>[] paths=new List[2];
                paths=NewAlg.pathXor(myGraph.shortestPath.getVertexList(),result.path);
//                double w2 = getSPWeight(myGraph.graph, myGraph.shortestPath.getVertexList());
//                newResult=result.weight+w2;
                newResult=getSPWeight(myGraph.graph,myGraph.shortestPath.getVertexList())+result.weight;
                resultArr[t][CSVCol.paths]=paths[0].toString()+","+paths[1].toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            endTime=System.currentTimeMillis();
            long newRunTime=endTime-startTime;

            startTime=System.currentTimeMillis();
            ILPGraph graph1 = JavaLPAlg.getGraphForILP(myGraph);
            double ILPResult=JavaLPAlg.solveWithLP(graph1,t, JavaLPAlg.LPSolver.GLPK);
            endTime=System.currentTimeMillis();
            long ILPRunTime=endTime-startTime;

//            startTime=System.currentTimeMillis();
//            double mwldResult = MWLD.mwldALg(myGraph);
//            endTime=System.currentTimeMillis();
//            long mwldRunTime=endTime-startTime;

            //GraphWriter.saveGraphToJson(myGraph,t+".json");

            resultArr[t][CSVCol.graphId]=Integer.toString(t);
            resultArr[t][CSVCol.startPoint]=Integer.toString(myGraph.startPoint);
            resultArr[t][CSVCol.sinkPoint]=Integer.toString(myGraph.sinkPoint);
            resultArr[t][CSVCol.maxComVertex]=Integer.toString(myGraph.maxComVertex);

            resultArr[t][CSVCol.ILPResult]=Double.toString(ILPResult);
            resultArr[t][CSVCol.ILPRunTime]=Long.toString(ILPRunTime);

//            resultArr[t][CSVCol.mwldResult]=Double.toString(mwldResult);
//            resultArr[t][CSVCol.mwldRunTime]=Long.toString(mwldRunTime);

            resultArr[t][CSVCol.newAlgResult]=Double.toString(newResult);
            resultArr[t][CSVCol.newAlgRunTime]=Long.toString(newRunTime);

            resultArr[t][CSVCol.nodeNum]=Integer.toString(myGraph.nodeNum);
            resultArr[t][CSVCol.edgeNum]=Integer.toString(myGraph.edgeNum);
            resultArr[t][CSVCol.startPoint]=Integer.toString(myGraph.startPoint);
            resultArr[t][CSVCol.sinkPoint]=Integer.toString(myGraph.sinkPoint);

            System.out.print("\n\n");
        }
        System.out.println(wrongList);
        System.out.println(wrongList2);
        CSVRecorder csvRecorder=new CSVRecorder();
        csvRecorder.writeToCSV("result.csv",resultArr);
    }
}
