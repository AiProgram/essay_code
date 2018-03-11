package MyGraph;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 由于JGraphT提供的图最多只能在边上添加weight这一个属性，所以
 * 包装MyGraph以便添加其他属性,例如边上的cost等
 */
public class MyGraph {
    public WeightedMultigraph graph;//JGraphT所能提供的graph格式
    public Map<DefaultWeightedEdge,Integer> costMap;
    public int nodeNum;
    public int edgeNum;
    public int startPoint;
    public int sinkPoint;
    public int maxComVertex;
    public GraphPath<Integer,DefaultWeightedEdge> shortestPath;//普通的起点到终点的最短路径，且是原图的
}
