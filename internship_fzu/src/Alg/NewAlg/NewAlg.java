package Alg.NewAlg;

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
    public static MyGraph getResidualGraph(int startPoint,int sinkPoint,MyGraph myGraph,int scale){
        //找到最短路径
        int nodeNum=0;
        int edgeNum=0;
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


        return myGraph;
    }
    public static void main(String args[]){
        GraphRandomGenerator randomGenerator=new GraphRandomGenerator();
        MyGraph myGraph=randomGenerator.generateRandomGraph(100,1000);
        NewAlg.getResidualGraph(0,90,myGraph,1);
    }
}
