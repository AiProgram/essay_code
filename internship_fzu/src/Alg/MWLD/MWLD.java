package Alg.MWLD;

import Alg.NewAlg.NewAlg;
import Alg.Util.Util;
import MyGraph.MyGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

public class MWLD {
    public static  double mwldALg(MyGraph myGraph){
        MyGraph auxGraph=mwldGetAuxGraph(myGraph.graph);
        auxGraph.startPoint=myGraph.startPoint;
        auxGraph.sinkPoint=myGraph.sinkPoint;
        auxGraph.maxComVertex=myGraph.maxComVertex;
        return NewAlg.RSPNoRecrusive(auxGraph);
    }

    public static MyGraph mwldGetAuxGraph(DirectedWeightedMultigraph graph){
        MyGraph auxGraph=new MyGraph();
        auxGraph.graph=new DirectedWeightedMultigraph(DefaultWeightedEdge.class);
        auxGraph.costMap=new HashMap<>();

        Iterator vit=graph.vertexSet().iterator();
        while(vit.hasNext()){
            int node=(int)vit.next();
            auxGraph.graph.addVertex(node);
        }

        Iterator sit=graph.vertexSet().iterator();
        Iterator tit;
        while(sit.hasNext())
        {
            int pointS=(int)sit.next();
            tit=graph.vertexSet().iterator();
            while(tit.hasNext())
            {
                int pointT=(int)tit.next();
                if(pointS==pointT) continue;
                else{
                    List<Integer> paths[]=mwldGetAuxGraphEdge(graph,pointS,pointT);
                    if(paths==null) continue;
                    else{
                        double wSum=Util.getSPWeight(graph,paths[0])+Util.getSPWeight(graph,paths[1]);
                        auxGraph.addNewEdge(pointS,pointT,wSum,1);
                    }
                }
            }
        }
        return auxGraph;
    }

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
    public static List<Integer>[] mwldGetAuxGraphEdge(DirectedWeightedMultigraph graph,int pointS,int pointT)
    {
        List<Integer> pathP;
        try{
            BellmanFordShortestPath<Integer,DefaultWeightedEdge> shortestPath=new BellmanFordShortestPath<>(graph);
            pathP=shortestPath.getPath(pointS,pointT).getVertexList();
        }catch (Exception e){
            return null;
        }

        DirectedWeightedMultigraph reverseGraph=getSPReverseGraph(graph,pathP);

        List<Integer> pathQ;
        try{
            BellmanFordShortestPath<Integer,DefaultWeightedEdge> shortestPath=new BellmanFordShortestPath<>(reverseGraph);
            pathQ=shortestPath.getPath(pointS,pointT).getVertexList();
        }catch (Exception e){
            return null;
        }

        List<Integer>paths[]=mwldPathXor(pathP,pathQ);
        return paths;//获取两条路径weight之和的工作可以放在外面
    }

    public static DirectedWeightedMultigraph<Integer,DefaultWeightedEdge> getSPReverseGraph(DirectedWeightedMultigraph graph,List<Integer> shortestPath)
    {
        DirectedWeightedMultigraph<Integer,DefaultWeightedEdge> reverseGraph=new DirectedWeightedMultigraph(DefaultWeightedEdge.class);
        Iterator vit;
        Iterator eit;

        //首先将graph复制到reverseGraph中去
        vit=graph.vertexSet().iterator();
        while(vit.hasNext())
        {
            int node=(int)vit.next();
            reverseGraph.addVertex(node);
        }

        eit=graph.edgeSet().iterator();
        while(eit.hasNext())
        {
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            double w=graph.getEdgeWeight(edge);
            int source=(int)graph.getEdgeSource(edge);
            int target=(int)graph.getEdgeTarget(edge);

            DefaultWeightedEdge newEdge=reverseGraph.getEdgeFactory().createEdge(source,target);
            reverseGraph.addEdge(source,target,newEdge);
            reverseGraph.setEdgeWeight(newEdge,w);
        }

        for(int index=0;index<shortestPath.size()-1;index++)
        {
            int pointU=shortestPath.get(index);
            int pointV=shortestPath.get(index+1);
            double w=graph.getEdgeWeight(graph.getEdge(pointU,pointV));
            reverseGraph.removeEdge(pointU,pointV);

            DefaultWeightedEdge edge=reverseGraph.getEdgeFactory().createEdge(pointV,pointU);
            reverseGraph.addEdge(pointV,pointU);
            reverseGraph.setEdgeWeight(edge,-w);
        }
        return reverseGraph;
    }
}