package util;

import alg.ILP.JavaLPAlg;
import alg.MWLD.MWLD;
import alg.NewAlg.NewAlg;
import graphIO.*;
import myGraph.ILPGraph;
import myGraph.MyGraph;
import org.AlgRuner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static util.Util.getSPWeight;
import static java.lang.StrictMath.round;

public class RunCodeGroup {
    /**
     * 用于批量运行某一文件夹中的SNAP图，数据记录到表csv格中
     * @param folderPath 指定SNAP图文件存放的文件夹，为null则用默认文件夹
     * @param runNewAlg 是否在SNAP图上运行FPPDSP算法
     * @param runMWLD 是否在SNAP图上运行MWLD算法
     * @param runILP 是否在SNAP图上运行LPPDSP算法
     */
    private String folderPath="./graph_data/";
    public void setFolderPath(String path){
        this.folderPath=path;
        checkFolder(path);
    }
    private boolean checkFolder(String folderPath){
        File path=new File(folderPath);
        if(!path.exists())
        {
            System.err.println("路径"+folderPath+"不存在");
            return false;
        }
        if(!path.isDirectory()){
            System.err.println(folderPath+"不是文件夹");
            return false;
        }
        return true;
    }
    public void runCodeGroup(GroupRuner runer){
        checkFolder(this.folderPath);
        File path=new File(this.folderPath);
        File files[]=path.listFiles();
        GraphReader graphReader=new GraphReader();
        for(int i=0;i<files.length;i++)
        {
            File graphFile=files[i];
            MyGraph oriGraph=new MyGraph();
            if(graphFile.isFile())
            {
                switch (runer.fileFilter(graphFile.getName()))
                {
                    case SnapGraph:oriGraph=graphReader.readSnapGraph(graphFile.getAbsolutePath());break;
                    case JsonGraph:oriGraph=graphReader.readJsonGraph(graphFile.getAbsolutePath());break;
                    default:oriGraph=new MyGraph();break;
                }
                if(!runer.runOnSingleGraph(oriGraph,graphFile.getName())) break;
            }
        }
    }
    public void runAlgGroup(AlgRuner runer,int times,boolean runNewAlg,boolean runMWLD,boolean runILP){
        GroupRuner groupRuner=new GroupRuner() {
            @Override
            public GraphType fileFilter(String fileName) {
                return runer.fileFilter(fileName);
            }

            @Override
            public boolean runOnSingleGraph(MyGraph oriGraph, String fileName) {
                GraphReader graphReader=new GraphReader();
                long startTime;
                long endTime;
                String resultArr[][]=new String[times][CSVCol.colNum];
                for(int t=1;t<=times;t++)
                {
                    oriGraph.startPoint = runer.onGetStartPoint(oriGraph,t,fileName);
                    oriGraph.sinkPoint = runer.onGetSinkPoint(oriGraph,t,fileName);
                    oriGraph.maxComVertex = runer.onGetMaxComVertex(oriGraph,t,fileName);

                    MyGraph graph = null;
                    double newResult = -1;
                    if (runNewAlg) {
                        startTime = System.currentTimeMillis();
                        try {
                            graph = NewAlg.getResidualGraph(oriGraph, 1);
                            NewAlg.RSPResult result = NewAlg.RSPNoRecrusive(graph,oriGraph);
                            if(result!=null) {
                                List<Integer>[] paths = new List[2];
                                paths = NewAlg.pathXor(result.path, oriGraph.shortestPath.getVertexList());
                                newResult = getSPWeight(oriGraph.graph, paths[0]) + getSPWeight(oriGraph.graph, paths[1]);
                                resultArr[t][CSVCol.paths] = paths[0].toString() + "," + paths[1].toString();
                            }else{
                                newResult=-1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        endTime = System.currentTimeMillis();
                        long newRunTime = endTime - startTime;
                        resultArr[t][CSVCol.newAlgResult] = Double.toString(newResult);
                        resultArr[t][CSVCol.newAlgRunTime] = Long.toString(newRunTime);
                    }

                    if (runILP) {
                        startTime = System.currentTimeMillis();
                        ILPGraph graph1 = JavaLPAlg.getGraphForILP(oriGraph);
                        double ILPResult = JavaLPAlg.solveWithLP(graph1, t, JavaLPAlg.LPSolver.GLPK);
                        endTime = System.currentTimeMillis();
                        long ILPRunTime = endTime - startTime;
                        resultArr[t][CSVCol.ILPResult] = Double.toString(ILPResult);
                        resultArr[t][CSVCol.ILPRunTime] = Long.toString(ILPRunTime);
                    }

                    if (runMWLD) {
                        startTime = System.currentTimeMillis();
                        double mwldResult = MWLD.mwldALg(oriGraph);
                        endTime = System.currentTimeMillis();
                        long mwldRunTime = endTime - startTime;
                        resultArr[t][CSVCol.mwldResult] = Double.toString(mwldResult);
                        resultArr[t][CSVCol.mwldRunTime] = Long.toString(mwldRunTime);
                    }

                    //GraphWriter.saveGraphToJson(myGraph,t+".json");

                    resultArr[t][CSVCol.graphId] = fileName;
                    resultArr[t][CSVCol.startPoint] = Integer.toString(oriGraph.startPoint);
                    resultArr[t][CSVCol.sinkPoint] = Integer.toString(oriGraph.sinkPoint);
                    resultArr[t][CSVCol.maxComVertex] = Integer.toString(oriGraph.maxComVertex);
                    resultArr[t][CSVCol.nodeNum]=Integer.toString(oriGraph.nodeNum);
                    resultArr[t][CSVCol.edgeNum]=Integer.toString(oriGraph.edgeNum);
                }
                CSVRecorder csvRecorder = new CSVRecorder();
                csvRecorder.writeToCSV(runer.onGetCSVName(oriGraph,fileName)+".csv", resultArr);
                return true;
                }
            };
        this.runCodeGroup(groupRuner);
        }
    public static void main(String args[]){
        RunCodeGroup runCodeGroup=new RunCodeGroup();
        runCodeGroup.setFolderPath("./snap_graph_data/");
        GroupRuner groupRuner=new GroupRuner() {
            @Override
            public GraphType fileFilter(String fileName) {
                return GraphType.SnapGraph;
            }

            @Override
            public boolean runOnSingleGraph(MyGraph oriGraph, String fileName) {
                oriGraph.startPoint=0;
                oriGraph.sinkPoint=20;
                oriGraph.maxComVertex=4;
                ILPGraph graph1 = JavaLPAlg.getGraphForILP(oriGraph);
                double ILPResult = JavaLPAlg.solveWithLP(graph1, 1, JavaLPAlg.LPSolver.GLPK);
                return true;
            }
        };
        runCodeGroup.runCodeGroup(groupRuner);
    }
}
