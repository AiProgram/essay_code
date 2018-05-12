package util;

import alg.ILP.JavaLPAlg;
import alg.MWLD.MWLD;
import alg.NewAlg.NewAlg;
import graphIO.*;
import myGraph.ILPGraph;
import myGraph.MyGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static alg.Util.Util.getSPWeight;
import static java.lang.StrictMath.round;

public class RunCodeGroup {
    public void runSnapGroup(boolean runNewAlg,boolean runMWLD,boolean runILP){
        String snapFolder=GraphReader.snapFolder;
        File folder=new File(snapFolder);
        File files[]=folder.listFiles();
        int time=files.length;
        int repeatTime=40;
        String resultArr[][]=new String[repeatTime][CSVCol.colNum];//用于记录算法运行数据记录进入csv表格中
        long startTime;
        long endTime;
        GraphReader graphReader=new GraphReader();

        List<Integer> wrongList=new ArrayList<>();
        List<Integer> wrongList2=new ArrayList<>();
        for(int t=0;t<time;t++) {
            for (int i = 0; i < repeatTime; ) {
                Random random=new Random();
                MyGraph myGraph = graphReader.readSnapGraph(files[t].getAbsolutePath());
                myGraph.startPoint = random.nextInt(myGraph.nodeNum);
                myGraph.sinkPoint = random.nextInt(myGraph.nodeNum);
                myGraph.maxComVertex = 5;

                MyGraph graph = null;
                double newResult = -1;
                if (runNewAlg) {
                    startTime = System.currentTimeMillis();
                    try {
                        graph = NewAlg.getResidualGraph(myGraph, 1);
                        NewAlg.RSPResult result = NewAlg.RSPNoRecrusive(graph,myGraph);
                        if(result!=null) {
                            List<Integer>[] paths = new List[2];
                            paths = NewAlg.pathXor(result.path, myGraph.shortestPath.getVertexList());
                            newResult = getSPWeight(myGraph.graph, paths[0]) + getSPWeight(myGraph.graph, paths[1]);
                            resultArr[i][CSVCol.paths] = paths[0].toString() + "," + paths[1].toString();
                        }else{
                            newResult=-1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    endTime = System.currentTimeMillis();
                    long newRunTime = endTime - startTime;
                    resultArr[i][CSVCol.newAlgResult] = Double.toString(newResult);
                    resultArr[i][CSVCol.newAlgRunTime] = Long.toString(newRunTime);
                }

                if (runILP) {
                    startTime = System.currentTimeMillis();
                    ILPGraph graph1 = JavaLPAlg.getGraphForILP(myGraph);
                    double ILPResult = JavaLPAlg.solveWithGLPK(graph1, t, JavaLPAlg.LPSolver.GLPK);
                    endTime = System.currentTimeMillis();
                    long ILPRunTime = endTime - startTime;
                    resultArr[i][CSVCol.ILPResult] = Double.toString(ILPResult);
                    resultArr[i][CSVCol.ILPRunTime] = Long.toString(ILPRunTime);
                }

                if (runMWLD) {
                    startTime = System.currentTimeMillis();
                    double mwldResult = MWLD.mwldALg(myGraph);
                    endTime = System.currentTimeMillis();
                    long mwldRunTime = endTime - startTime;
                    resultArr[i][CSVCol.mwldResult] = Double.toString(mwldResult);
                    resultArr[i][CSVCol.mwldRunTime] = Long.toString(mwldRunTime);
                }

                //GraphWriter.saveGraphToJson(myGraph,t+".json");

                resultArr[i][CSVCol.graphId] = files[t].getName();
                resultArr[i][CSVCol.startPoint] = Integer.toString(myGraph.startPoint);
                resultArr[i][CSVCol.sinkPoint] = Integer.toString(myGraph.sinkPoint);
                resultArr[i][CSVCol.maxComVertex] = Integer.toString(myGraph.maxComVertex);
                resultArr[i][CSVCol.nodeNum]=Integer.toString(myGraph.nodeNum);
                resultArr[i][CSVCol.edgeNum]=Integer.toString(myGraph.edgeNum);
                if(newResult!=-1) {
                    i++;
                    if(i%5==0)
                        System.out.println(i);
                }
            }
            System.out.println(wrongList);
            System.out.println(wrongList2);
            CSVRecorder csvRecorder = new CSVRecorder();
            csvRecorder.writeToCSV(files[t].getName()+".csv", resultArr);
        }
    }
    public static void main(String args[]){
        RunCodeGroup runCodeGroup=new RunCodeGroup();
        runCodeGroup.runSnapGroup(true,false,false);
    }
}
