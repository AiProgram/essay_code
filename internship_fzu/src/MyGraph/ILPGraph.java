package myGraph;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.List;
import java.util.Map;

public class ILPGraph {
    public DirectedWeightedMultigraph graph;//JGraphT所能提供的graph格式
    public Map<DefaultWeightedEdge,Integer> costMap;
    public int nodeNum;
    public int edgeNum;
    public int startPoint;
    public int sinkPoint;
    public int maxComVertex;
    public boolean multiGraph=true;
    public boolean directed=true;
    public GraphPath<Integer,DefaultWeightedEdge> shortestPath;//普通的起点到终点的最短路径，且是原图的
    public List<Integer> restrictedShortestPath;
    public List<Integer>[]pathPair;//最后的最短路径对，暂时不用

    //方便在添加边的同时设置weight以及cost
    public DefaultWeightedEdge addNewEdge(int source ,int target,double weight,int cost){
        DefaultWeightedEdge newEdge=(DefaultWeightedEdge) this.graph.getEdgeFactory().createEdge(source,target);
        try {
            this.graph.addEdge(source, target, newEdge);//当边无法添加时返回null表示失败
        }catch (Exception e){
            return null;
        }
        this.graph.setEdgeWeight(newEdge,weight);
        this.costMap.put(newEdge,cost);
        return newEdge;
    }
}
