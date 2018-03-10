package MyGraph;

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
}
