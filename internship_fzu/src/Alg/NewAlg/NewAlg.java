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

        return myGraph;
    }
    public static void main(String args[]){
        GraphRandomGenerator randomGenerator=new GraphRandomGenerator();
        MyGraph myGraph=randomGenerator.generateRandomGraph(100,1000);
        NewAlg.getResidualGraph(0,90,myGraph,1);
    }
}
