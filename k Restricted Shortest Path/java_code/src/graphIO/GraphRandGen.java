package graphIO;

import myGraph.MyGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class GraphRandGen {
    public DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge> graph=new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    public Map<DefaultWeightedEdge,Integer> costMap=new HashMap<>();
    public Map<DefaultWeightedEdge,Integer> delayMap=new HashMap<>();

    public MyGraph generateRandomGraph(int nodeNum, int edgeNum)
    {
        MyGraph myGraph=new MyGraph();
        GnmRandomGraphGenerator<Integer,DefaultWeightedEdge> randomGraphGenerator=new GnmRandomGraphGenerator(nodeNum,edgeNum);
        randomGraphGenerator.generateGraph(graph,new MyVertextFactory(),null);
        myGraph.graph=this.graph;
        myGraph.setCurentWeight(MyGraph.CurentWeight.cost);

        //添加weight和cost以及delay
        Random random=new Random();
        Iterator eit = graph.edgeSet().iterator();
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            int cost=random.nextInt(2)+1;
            int delay=random.nextInt(2)+1;

            myGraph.graph.setEdgeWeight(edge,cost);
            myGraph.costMap.put(edge,cost);
            myGraph.delayMap.put(edge,delay);
        }

        return myGraph;
    }

    public class MyVertextFactory implements VertexFactory {
        Integer count=0;
        @Override
        public Object createVertex() {
            Integer t;
            t=count++;
            return t;
        }
    }
}
