package GraphStructure;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyGraph {
    public DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge> graph;
    public Map<DefaultWeightedEdge,Integer> costMap;
    public Map<DefaultWeightedEdge,Integer> delayMap;
    private CurentWeight curentWeight=CurentWeight.cost;//默认把cost当作weight放进去
    MyGraph(){
        graph=new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        costMap=new HashMap<>();
        delayMap=new HashMap<>();
        curentWeight=CurentWeight.cost;
    }

    public CurentWeight getCurentWeight() {
        return curentWeight;
    }

    public enum CurentWeight{//用来指示当前用来存入边的weight的是cost还是delay
        cost,delay
    }
    public DefaultWeightedEdge addNewEdge(int src,int tar,int cost,int delay)//封装好的添加新边
    {
        DefaultWeightedEdge newEdge=this.graph.getEdgeFactory().createEdge(src,tar);
        graph.addEdge(src,tar);

        if(curentWeight==CurentWeight.cost)graph.setEdgeWeight(newEdge,cost);//防止在调整了curentWeight后出现混乱
        else graph.setEdgeWeight(newEdge,delay);

        costMap.put(newEdge,cost);
        delayMap.put(newEdge,delay);
        return newEdge;
    }

    /**
     *
     * 用来设置当前的图的weight是cost还是delay，因为GraphT不支持动态切换
     * @param curentWeight
     */
    public void setCurentWeight(CurentWeight curentWeight)
    {
        if(this.curentWeight==curentWeight) return;
        if(curentWeight==CurentWeight.cost)
        {
            Iterator eit=graph.edgeSet().iterator();
            while(eit.hasNext())
            {
                DefaultWeightedEdge edge=(DefaultWeightedEdge) eit.next();
                int cost=costMap.get(edge);
                graph.setEdgeWeight(edge,cost);
            }
        }else{
            Iterator eit=graph.edgeSet().iterator();
            while(eit.hasNext())
            {
                DefaultWeightedEdge edge=(DefaultWeightedEdge) eit.next();
                int delay=delayMap.get(edge);
                graph.setEdgeWeight(edge,delay);
            }
        }
        this.curentWeight=curentWeight;
        return;
    }
    public MyGraph copyGraph()//复制一份本图，但是完全独立
    {
        MyGraph myGraph=new MyGraph();
        myGraph.setCurentWeight(curentWeight);//提前设置，关系到后面添加边的正确性
        Iterator vit=graph.vertexSet().iterator();
        while(vit.hasNext())
        {
            Integer node=(Integer)vit.next();
            myGraph.graph.addVertex(node);
        }
        Iterator eit=graph.edgeSet().iterator();
        while(eit.hasNext())
        {
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            int cost=this.costMap.get(edge);
            int delay=this.delayMap.get(edge);
            int src=this.graph.getEdgeSource(edge);
            int tar=this.graph.getEdgeTarget(edge);

            myGraph.addNewEdge(src,tar,cost,delay);
        }
        return myGraph;
    }
}
