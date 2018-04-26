package alg.NewAlg;

import alg.ILP.JavaLPAlg;
import alg.MWLD.MWLD;
import alg.Util.Util;
import graphIO.CSVCol;
import graphIO.CSVRecorder;
import graphIO.GraphRandomGenerator;
import graphIO.GraphWriter;
import myGraph.ILPGraph;
import myGraph.MyGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

import static java.lang.StrictMath.round;

public class NewAlg_backup {
    static double INFINITY=1<<30;
    public static MyGraph getResidualGraph(MyGraph myGraph,int scale){
        //找到最短路径
        int nodeNum=0;
        int edgeNum=0;
        int startPoint=myGraph.startPoint;
        int sinkPoint=myGraph.sinkPoint;
        GraphPath<Integer,DefaultWeightedEdge> shortestPath;

        if(myGraph==null) return null;
        else{
            nodeNum=myGraph.graph.vertexSet().size();
            edgeNum=myGraph.graph.edgeSet().size();
        }

        try{
            BellmanFordShortestPath<Integer,DefaultWeightedEdge> bellmanFordShortestPath=new BellmanFordShortestPath<>(myGraph.graph);
            myGraph.shortestPath=bellmanFordShortestPath.getPath(startPoint,sinkPoint);
        }catch (Exception e){
            System.out.println("找不到最短路径，可能是两点间不联通");
            return null;
        }

        //产生点分离的余图
        //把G中所有不在P中边加入G‘中

        //首先复制G到G‘中
        MyGraph residualGraph=new MyGraph();
        residualGraph.costMap=new HashMap<>();
        residualGraph.graph=new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);
        residualGraph.maxComVertex=myGraph.maxComVertex;
        residualGraph.startPoint=myGraph.startPoint;
        residualGraph.sinkPoint=myGraph.sinkPoint;
        residualGraph.shortestPath=myGraph.shortestPath;

        Iterator vit=myGraph.graph.vertexSet().iterator();
        Iterator eit=myGraph.graph.edgeSet().iterator();
        while(vit.hasNext()){
            Integer v=(Integer) vit.next();
            if(residualGraph.shortestPath.getVertexList().contains(v)&&v!=startPoint&&v!=sinkPoint) continue;
            residualGraph.graph.addVertex(v);
        }

        //对于G中的每一条边，都尝试加入到G’中，但是端点被删除的添加会无效
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            int source=(int)myGraph.graph.getEdgeSource(edge);
            int target=(int)myGraph.graph.getEdgeTarget(edge);
            double weight=myGraph.graph.getEdgeWeight(edge);
            residualGraph.addNewEdge(source,target,weight,0);
        }

        if(myGraph.shortestPath.getLength()==1) residualGraph.graph.removeEdge(startPoint,sinkPoint);

        //对于shortestPath中的每一个中间点(除了起始点和终点以外)都把边(v1,v2)、(v2,v1)加入，而且加上weight和cost
        vit=residualGraph.shortestPath.getVertexList().iterator();
        while(vit.hasNext()){
            int v=(int)vit.next();
            if(v!=startPoint&&v!=sinkPoint){
                int v1=v+nodeNum;
                int v2=v+2*nodeNum;
                residualGraph.graph.addVertex(v1);//这里必须边添加成功，避免出现点不存在的情况
                residualGraph.graph.addVertex(v2);
                residualGraph.addNewEdge(v1,v2,0,scale);
                residualGraph.addNewEdge(v2,v1,0,0);
            }
        }


        eit=myGraph.graph.edgeSet().iterator();
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            double w=myGraph.graph.getEdgeWeight(edge);
            int u=(int)myGraph.graph.getEdgeSource(edge);
            int v=(int)myGraph.graph.getEdgeTarget(edge);
            List<Integer> vList=myGraph.shortestPath.getVertexList();
            //当边(source,target)在shortestPath中时
            if(vList.contains(u)&&vList.contains(v)&&(vList.indexOf(v)-vList.indexOf(u)==1)){
                int u2=u;
                int v1=v;
                if(u!=startPoint&&u!=sinkPoint)
                    u2=u+2*nodeNum;
                if(v!=startPoint&&v!=sinkPoint)
                    v1=v+nodeNum;

                residualGraph.graph.addVertex(v1);
                residualGraph.graph.addVertex(u2);
                residualGraph.addNewEdge(v1,u2,-w,0);
            }else {
                //当source是shortestPath中的点时
                if(vList.contains(u)&&u!=startPoint&&u!=sinkPoint)
                {
                    int u2=+2*nodeNum;
                    if(vList.contains(v)&&v!=startPoint&&v!=sinkPoint)
                        v+=nodeNum;

                    residualGraph.graph.addVertex(u2);
                    residualGraph.graph.addVertex(v);
                    residualGraph.addNewEdge(u2,v,w,0);
                }
                //当target是shortestPath中的点时
                if(vList.contains(v)&&v!=startPoint&&v!=sinkPoint)
                {
                    int v1=v+nodeNum;
                    if(vList.contains(u)&&u!=startPoint&&u!=sinkPoint)
                        u+=2*nodeNum;

                    residualGraph.graph.addVertex(u);
                    residualGraph.graph.addVertex(v1);
                    residualGraph.addNewEdge(u,v1,w,0);
                }
            }
        }

        return residualGraph;
    }

    public static double RSPNoRecrusive(MyGraph myGraph){
        List<Integer> path;
        int startPoint=myGraph.startPoint;
        int sinkPoint=myGraph.sinkPoint;
        int maxComVertex=myGraph.maxComVertex;
        DefaultDirectedWeightedGraph graph=myGraph.graph;
        int nodeNum=graph.vertexSet().size();
        int pathMap[][]=new int[nodeNum*3][maxComVertex+1];
        double dynMat[][]=new double[nodeNum*3][maxComVertex+1];
        int min_node=0;

        //初始化
        Iterator vit=graph.vertexSet().iterator();
        Iterator eit;
        while(vit.hasNext()){
            int node=(int)vit.next();
            if(node!=startPoint)
                for(int mcv=0;mcv<maxComVertex+1;mcv++)
                    dynMat[node][mcv]=INFINITY;
        }
        for(int mcv=0;mcv<maxComVertex+1;mcv++)
            dynMat[startPoint][mcv]=0;

        //主要算法
        for(int mcv=1;mcv<maxComVertex+1;mcv++){
            vit=graph.vertexSet().iterator();
            while(vit.hasNext()){
                int node=(int)vit.next();
                dynMat[node][mcv]=dynMat[node][mcv-1];
                pathMap[node][mcv]=node;
            }
            eit=graph.edgeSet().iterator();
            while(eit.hasNext()){
                DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
                int source=(int)graph.getEdgeSource(edge);
                int target=(int)graph.getEdgeTarget(edge);
                double weight= graph.getEdgeWeight(edge);
                if(myGraph.costMap.get(edge)==0){
                    if(dynMat[target][mcv]>=dynMat[source][mcv]+weight)
                    {
                        dynMat[target][mcv]=dynMat[source][mcv]+weight;
                        pathMap[target][mcv]=source;
                    }
                }else if(myGraph.costMap.get(edge)==1){
                    if(dynMat[target][mcv]>=dynMat[source][mcv-1]+weight)
                    {
                        dynMat[target][mcv]=dynMat[source][mcv-1]+weight;
                        pathMap[target][mcv]=source;
                    }
                }
            }
        }

        List<Integer>pathQ=new ArrayList<>();
        Stack<Integer>stack=new Stack<>();
        stack.push(sinkPoint);

        int curMcv=myGraph.maxComVertex;
        int curId=sinkPoint;
        int source;
        int target;
        int cost;
        while(curId!=startPoint)
        {
            target=curId;
            source=pathMap[curId][curMcv];
            DefaultWeightedEdge edge=(DefaultWeightedEdge) myGraph.graph.getEdge(source,target);
            if(myGraph.graph.getAllEdges(source,target).size()>1) System.out.println("double edge");
            cost=myGraph.costMap.get(edge);

            if(stack.peek()!=source) stack.push(source);
            curId=source;
            curMcv=curMcv-cost;
        }
        while(!stack.empty())
        {
            pathQ.add(stack.peek());
            stack.pop();
        }
//        System.out.println(pathQ);
//        System.out.println(Util.getSPWeight(myGraph.graph,pathQ));
//        System.out.println(dynMat[sinkPoint][maxComVertex]);
//        System.out.println("cost "+curMcv);

        myGraph.restrictedShortestPath=pathQ;//RSP算法得到的最短路径将被直接放在辅助图中

//        for(int i=0;i<dynMat.length;i++) {//用于输出pathMap，暂时不用
//            for (int j = 0; j < dynMat[i].length; j++){
//                System.out.print(pathMap[i][j]);
//                System.out.print("  ");
//            }
//            System.out.print("\n");
//        }

        return dynMat[sinkPoint][maxComVertex];
    }

    public static double newAlg(MyGraph myGraph){
        MyGraph residualGraph = NewAlg_backup.getResidualGraph(myGraph, 1);
        double w1= NewAlg_backup.RSPNoRecrusive(residualGraph);
        myGraph.restrictedShortestPath=residualGraph.restrictedShortestPath;
        List<Integer>[]paths=pathXOR(myGraph,myGraph.shortestPath.getVertexList(),residualGraph.restrictedShortestPath);
        myGraph.pathPair=paths;
        return w1+Util.getSPWeight(myGraph.graph,myGraph.shortestPath.getVertexList());
    }

    public static List<Integer>[] pathXOR(MyGraph originGraph,List<Integer>pathP,List<Integer>pathQ){
        int nodeNum=originGraph.nodeNum;

        Iterator vit=pathP.iterator();
        List<Integer>path1=new ArrayList<>();
        while(vit.hasNext()){
            int node=(int)vit.next();
            if(node>=2*nodeNum)
            {
                path1.add(node-2*nodeNum);
            }else if(node>=nodeNum){
                path1.add(node-nodeNum);
            }else{
                path1.add(node);
            }
        }

        vit=pathQ.iterator();
        List<Integer>path2=new ArrayList<>();
        while(vit.hasNext()){
            int node=(int)vit.next();
            if(node>=2*nodeNum)
            {
                path2.add(node-2*nodeNum);
            }else if(node>=nodeNum){
                path2.add(node-nodeNum);
            }else{
                path2.add(node);
            }
        }

        List<Integer>[] paths=MWLD.mwldPathXor(path1,path2);
        return paths;
    }
    public static void main(String args[]){
        int time=100;
        String resultArr[][]=new String[time][CSVCol.colNum];//用于记录算法运行数据记录进入csv表格中
        long startTime;
        long endTime;


        boolean correct=true;
        List<Integer>wrongList=new ArrayList<>();
        List<Integer>notExactList=new ArrayList<>();
        for(int t=0;t<time;t++) {
            System.out.println("id   "+t);

            GraphRandomGenerator randomGenerator = new GraphRandomGenerator();
//            String jsonStr= JavaLPAlg.readJsonGraph("97.json");
//            myGraph myGraph=JavaLPAlg.parseJsonToGraph(jsonStr);
            MyGraph myGraph=randomGenerator.generateRandomGraph(100,4000);
            myGraph.startPoint = 0;
            myGraph.sinkPoint = 5;
            myGraph.maxComVertex = 2;

            startTime=System.currentTimeMillis();
            double newResult=-1;
            try{
            newResult=newAlg(myGraph);
            }catch (Exception e){
                e.printStackTrace();
            }
            endTime=System.currentTimeMillis();
            long newRunTime=endTime-startTime;
            System.out.println("newResult  "+newResult);

            startTime=System.currentTimeMillis();
            ILPGraph graph1 = JavaLPAlg.getGraphForILP(myGraph);
            double ILPResult=JavaLPAlg.solveWithGLPK(graph1,0, JavaLPAlg.LPSolver.GLPK);
            endTime=System.currentTimeMillis();
            long ILPRunTime=endTime-startTime;
//
//            double mwldResult=-1;
//            startTime=System.currentTimeMillis();
//            try {
//                mwldResult = MWLD.mwldALg(myGraph);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            endTime=System.currentTimeMillis();
//            long mwldRunTime=endTime-startTime;
//            System.out.println("mwld   "+mwldResult);

            //GraphWriter.saveGraphToJson(myGraph,"graph.json");

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

            if(round(ILPResult)>round(newResult)&&newResult!=-1){
                correct=false;
                wrongList.add(t);
                GraphWriter.saveGraphToJson(myGraph,t+".json");
                System.out.println(myGraph.shortestPath);
            }else if(round(ILPResult)<round(newResult)){
                notExactList.add(t);
                System.out.println(myGraph.shortestPath);
                System.out.println(myGraph.restrictedShortestPath);
                System.out.println(myGraph.pathPair[0]);
                System.out.println(myGraph.pathPair[1]);
//                System.out.println(myGraph.shortestPath);
                GraphWriter.saveGraphToJson(myGraph,t+".json");
            }
            System.out.print("\n\n");
        }

        CSVRecorder csvRecorder=new CSVRecorder();
        csvRecorder.writeToCSV("2.csv",resultArr);
        if(!correct){
            System.out.println("  结果出现错误  ");
            System.out.println(wrongList);
        }else{
            System.out.println("  结果正确  ");
            System.out.println(notExactList);
        }

    }
}
