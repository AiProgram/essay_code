package Alg.MWLD;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

public class MWLD {

    public static List<Integer>[] mwldPathXor(List<Integer> pathP,List<Integer>pathQ){
        List<Integer>pathP1=new ArrayList<>();
        List<Integer>pathP2=new ArrayList<>();
        DirectedMultigraph<Integer,DefaultEdge>graph=new DirectedMultigraph<>(DefaultEdge.class);
        Iterator vit=pathP.iterator();

        while (vit.hasNext())
        {
            int node=(int)vit.next();
            graph.addVertex(node);
        }
        vit=pathQ.iterator();
        while(vit.hasNext())
        {
            int node=(int)vit.next();
            graph.addVertex(node);
        }

        Map<Integer,Integer> edgeP1=new HashMap<>();
        for(int index=0;index<pathP.size()-1;index++)
        {
            edgeP1.put(pathP.get(index),pathP.get(index+1));
        }
        Map<Integer,Integer> edgeShared=new HashMap<>();
        for(int index=0;index<pathQ.size()-1;index++)
        {
            if(edgeP1.get(pathQ.get(index+1))==pathQ.get(index))
            {
                edgeShared.put(pathQ.get(index+1),pathQ.get(index));
            }else {
                graph.addEdge(pathQ.get(index),pathQ.get(index+1));
            }
        }
        for(int index=0;index<pathP.size()-1;index++)
        {
            if(edgeShared.get(pathP.get(index))!=pathP.get(index+1))
                graph.addEdge(pathP.get(index),pathP.get(index+1));
        }

        int s=pathP.get(0);
        List<DefaultEdge> s_succ=new ArrayList<>(graph.outgoingEdgesOf(s));

        pathP1.add(s);
        int now=graph.getEdgeTarget(s_succ.get(0));
        pathP1.add(now);
        List<DefaultEdge> tmp=new ArrayList<>(graph.outgoingEdgesOf(now));
        while(tmp.size()>0){
            now=graph.getEdgeTarget(tmp.get(0));
            pathP1.add(now);
            tmp=new ArrayList<>(graph.outgoingEdgesOf(now));
        }

        pathP2.add(s);
        now=graph.getEdgeTarget(s_succ.get(1));
        pathP2.add(now);
        tmp=new ArrayList<>(graph.outgoingEdgesOf(now));
        while(tmp.size()>0){
            now=graph.getEdgeTarget(tmp.get(0));
            pathP2.add(now);
            tmp=new ArrayList<>(graph.outgoingEdgesOf(now));
        }

        List<Integer> result[]=new List[2];
        result[0]=pathP1;
        result[1]=pathP2;
        return result;
    }
}
