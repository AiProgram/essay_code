package Alg.NewAlg;

import Alg.ILP.JavaLPAlg;
import GraphIO.GraphRandomGenerator;
import MyGraph.MyGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NewAlg {
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
            nodeNum=myGraph.nodeNum;
            edgeNum=myGraph.edgeNum;
        }

        try{
            DijkstraShortestPath<Integer,DefaultWeightedEdge> dijkstraShortestPath=new DijkstraShortestPath<>(myGraph.graph);
            myGraph.shortestPath=dijkstraShortestPath.getPath(startPoint,sinkPoint);
        }catch (Exception e){
            System.out.println("找不到最短路径，可能是两点间不联通");
            return null;
        }

        //产生点分离的余图
        //把G中所有不在P中边加入G‘中

        //首先复制G到G‘中
        MyGraph residualGraph=new MyGraph();
        residualGraph.costMap=new HashMap<>();
        residualGraph.graph=new WeightedMultigraph(DefaultWeightedEdge.class);
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
            int source=(int)myGraph.graph.getEdgeSource(edge);
            int target=(int)myGraph.graph.getEdgeTarget(edge);
            List<Integer> vList=myGraph.shortestPath.getVertexList();
            //当边(source,target)在shortestPath中时
            if(vList.contains(source)&&vList.contains(target)&&(vList.indexOf(target)-vList.indexOf(source)==1)){
                int u2=source;
                int v1=target;
                if(source!=startPoint&&target!=sinkPoint)
                    u2=source+2*nodeNum;
                if(target!=startPoint&&target!=sinkPoint)
                    v1=target+nodeNum;
                residualGraph.addNewEdge(v1,u2,-w,0);
            }else {
                //当source是shortestPath中的点时
                if(vList.contains(source)&&source!=startPoint&&source!=sinkPoint)
                {
                    int u2=source+2*nodeNum;
                    int v=target;
                    if(vList.contains(target)&&target!=startPoint&&target!=sinkPoint)
                        v+=nodeNum;
                    residualGraph.addNewEdge(u2,v,w,0);
                }
                //当target是shortestPath中的点时
                if(vList.contains(target)&&target!=startPoint&&target!=sinkPoint)
                {
                    int v1=target+nodeNum;
                    int u=source;
                    if(vList.contains(source)&&source!=startPoint&&source!=sinkPoint)
                        u+=2*nodeNum;
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
        WeightedMultigraph graph=myGraph.graph;
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
        return dynMat[sinkPoint][maxComVertex];
    }

    public static double getSPWeight(WeightedMultigraph<Integer,DefaultWeightedEdge> graph,List<Integer>path){
        double sum=0;
        Iterator vit=path.iterator();
        int source=(int)vit.next();
        int target;
        while(vit.hasNext()){
            target=(int)vit.next();
            DefaultWeightedEdge edge=graph.getEdge(source,target);
            sum+= graph.getEdgeWeight(edge);
            source=target;
        }
        return sum;
    }
    public static void main(String args[]){
        GraphRandomGenerator randomGenerator=new GraphRandomGenerator();
        MyGraph myGraph=randomGenerator.generateRandomGraph(400,5000);
        myGraph.startPoint=0;
        myGraph.sinkPoint=20;
        myGraph.maxComVertex=4;
        MyGraph graph=NewAlg.getResidualGraph(myGraph,1);
        double w=NewAlg.RSPNoRecrusive(graph);
        double w2=getSPWeight(myGraph.graph,myGraph.shortestPath.getVertexList());
        System.out.println(w+w2);
        MyGraph graph1=JavaLPAlg.getGraphForILP(myGraph);
        JavaLPAlg.solveWithGLPK(graph1);
    }
}
