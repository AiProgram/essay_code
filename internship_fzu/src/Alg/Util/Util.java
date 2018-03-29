package Alg.Util;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Iterator;
import java.util.List;

public class Util {
    public static double getSPWeight(DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge> graph, List<Integer> path){
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


}
