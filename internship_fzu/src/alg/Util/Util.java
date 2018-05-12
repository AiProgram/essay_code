package alg.Util;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

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
            try {
                sum += graph.getEdgeWeight(edge);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(path);
                return -1;
            }
            source=target;
        }
        return sum;
    }


}
