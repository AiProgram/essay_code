package util;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Iterator;
import java.util.List;

public class Util {
    /**
     * 获取路径在图中的weight总和
     * @param graph 图，带有weight属性
     * @param path 路径
     * @return weight总和
     */
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
