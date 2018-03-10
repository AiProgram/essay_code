package GraphIO;

import MyGraph.MyGraph;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class GraphRandomGenerator {
    public WeightedMultigraph<Integer,DefaultWeightedEdge> graph=new WeightedMultigraph<>(DefaultWeightedEdge.class);
    public Map<DefaultWeightedEdge,Integer> costMap=new HashMap<>();

    public MyGraph generateRandomGraph(int nodeNum, int edgeNum)
    {
        GnmRandomGraphGenerator<Integer,DefaultWeightedEdge> randomGraphGenerator=new GnmRandomGraphGenerator(nodeNum,edgeNum);
        randomGraphGenerator.generateGraph(graph,new MyVertextFactory(),null);

        //添加weight和cost
        Random random=new Random();
        Iterator eit = graph.edgeSet().iterator();
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            graph.setEdgeWeight(edge,random.nextInt(990)+10);
            costMap.put(edge,0);
        }

        MyGraph myGraph=new MyGraph();
        myGraph.graph=graph;
        myGraph.costMap=new HashMap<>();
        return myGraph;
    }

    public class MyVertextFactory implements VertexFactory{
        Integer count=0;
        @Override
        public Object createVertex() {
            Integer t;
            t=count++;
            return t;
        }
    }

    public static void main(String args[]){
        GraphRandomGenerator randomGenerator=new GraphRandomGenerator();
        MyGraph myGraph=randomGenerator.generateRandomGraph(20,100);
        WeightedMultigraph graph=myGraph.graph;
        BellmanFordShortestPath<Integer,DefaultWeightedEdge> bellmanFordShortestPath=new BellmanFordShortestPath(graph);
        Random random=new Random();
        double weight=bellmanFordShortestPath.getPathWeight(2,10);
        System.out.println(weight);
    }
}
