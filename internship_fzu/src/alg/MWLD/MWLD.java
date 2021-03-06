package alg.MWLD;

import alg.NewAlg.NewAlg;
import util.Util;
import myGraph.MyGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.*;

import java.util.*;

public class MWLD {
    /**
     * MWLD算法的主入口
     * @param oriGraph 原图，也包括了算法所需要的信息
     * @return δV-2EDSP解（路径weight之和）
     */
    public static  double mwldALg(MyGraph oriGraph){
        MyGraph auxGraph=mwldGetAuxGraph(oriGraph.graph,oriGraph.sinkPoint);
        auxGraph.startPoint=oriGraph.startPoint;
        auxGraph.sinkPoint=oriGraph.sinkPoint;
        auxGraph.maxComVertex=oriGraph.maxComVertex-1;
        double weight=-1;
        try {
            weight=NewAlg.RSPNoRecrusive(auxGraph, oriGraph).weight;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
        return weight;
    }

    /**
     * 为MWLD算法构造辅助图
     * @param graph  原图
     * @param sinkPoint 问题中的终点
     * @return 辅助图
     */
    public static MyGraph mwldGetAuxGraph(DefaultDirectedWeightedGraph graph,int sinkPoint){
        MyGraph auxGraph=new MyGraph();
        auxGraph.graph=new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);
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
                        if(pointT == sinkPoint)
                        auxGraph.addNewEdge(pointS,pointT,wSum,0);
                        else auxGraph.addNewEdge(pointS,pointT,wSum,1);
                    }
                }
            }
        }
        return auxGraph;
    }

    /**
     * 两条可能含有反向边的路径经过异或去除反向边，最后得到两条新路径
     * @param pathP 路径P
     * @param pathQ 路径Q
     * @return 含有两条新路径的数组
     */
    public static List<Integer>[] mwldPathXor(List<Integer> pathP,List<Integer>pathQ){
        List<Integer>pathP1=new ArrayList<>();
        List<Integer>pathP2=new ArrayList<>();
        DefaultDirectedGraph<Integer,DefaultEdge> graph=new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
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
        int t=pathP.get(pathP.size()-1);
        AllDirectedPaths<Integer,DefaultEdge> allDirectedPaths=new AllDirectedPaths<>(graph);
        List<GraphPath<Integer,DefaultEdge>> pathList=allDirectedPaths.getAllPaths(s,t,true,null);
        List<Integer> result[]=new List[2];
        result[0]=pathList.get(0).getVertexList();
        result[1]=pathList.get(1).getVertexList();
        return result;
    }

    /**
     * 获取MWLD辅助图的边
     * @param graph 原图
     * @param pointS 辅助边起点
     * @param pointT 辅助边终点
     * @return 用于获取辅助边weight的两条边
     */
    public static List<Integer>[] mwldGetAuxGraphEdge(DefaultDirectedGraph graph,int pointS,int pointT)
    {
        int nodeNum=graph.vertexSet().size();
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

        List<Integer>tmp=new ArrayList<>();
        for(int index=0;index<pathQ.size();index++)
        {
            int node=pathQ.get(index);
            if(node>=2*nodeNum)
                tmp.add(node-2*nodeNum);
            else if(node>=nodeNum)
                tmp.add(node-nodeNum);
            else
                tmp.add(node);
        }
        pathQ=tmp;

        List<Integer>paths[]=mwldPathXor(pathP,pathQ);
        return paths;//获取两条路径weight之和的工作可以放在外面
    }

    /**
     * 得到原图关于最短路径的残余图
     * @param graph 原图
     * @param shortestPath 最短路径
     * @return 残余图
     */
    public static DirectedWeightedMultigraph<Integer,DefaultWeightedEdge> getSPReverseGraph(DefaultDirectedGraph graph,List<Integer> shortestPath)
    {
        int s=shortestPath.get(0);
        int t=shortestPath.get(shortestPath.size()-1);
        List<Integer> oriVertexList=new ArrayList<>(graph.vertexSet());
        List<DefaultWeightedEdge> oriEdgeList=new ArrayList<>(graph.edgeSet());
        int nodeNum=oriVertexList.size();
        int edgeNum=oriEdgeList.size();

        DirectedWeightedMultigraph<Integer,DefaultWeightedEdge> reverseGraph=new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        for(int index=0;index<oriVertexList.size();index++)
        {
            int node=oriVertexList.get(index);
            if(shortestPath.contains(node))
            {
                if(node==s || node==t)
                    reverseGraph.addVertex(node);
                else{
                    int v1=node+nodeNum;
                    int v2=node+2*nodeNum;
                    reverseGraph.addVertex(v1);
                    reverseGraph.addVertex(v2);
                    DefaultWeightedEdge edge=reverseGraph.getEdgeFactory().createEdge(v1,v2);
                    reverseGraph.addEdge(v1,v2,edge);
                    reverseGraph.setEdgeWeight(edge,0);
                }
            }else{
                reverseGraph.addVertex(node);
            }
        }

        for(int i=0;i<oriEdgeList.size();i++)
        {
            DefaultWeightedEdge edge=oriEdgeList.get(i);
            double w=graph.getEdgeWeight(edge);
            int u=(int)graph.getEdgeSource(edge);
            int v=(int)graph.getEdgeTarget(edge);
            int u1=u+nodeNum;
            int v2=v+2*nodeNum;

            if(shortestPath.contains(u)&&shortestPath.contains(v)&&shortestPath.indexOf(v)-shortestPath.indexOf(u)==1)
            {
            //边(u,v)在shortestPath中
                int newU;
                int newV;
                if(u==s)
                    newU=s;
                else
                    newU=u1;

                if(v==t)
                    newV=t;
                else
                    newV=v2;
                DefaultWeightedEdge newEdge=reverseGraph.getEdgeFactory().createEdge(newV,newU);
                reverseGraph.addEdge(newV,newU,newEdge);
                reverseGraph.setEdgeWeight(newEdge,-w);
            }else{
                int newU;
                int newV;
                if(u==s||u==t||(!shortestPath.contains(u)))
                    newU=u;
                else
                    newU=u1;

                if(v==s||v==t||(!shortestPath.contains(v)))
                    newV=v;
                else
                    newV=v2;
                DefaultWeightedEdge newEdge=reverseGraph.getEdgeFactory().createEdge(newU,newV);
                reverseGraph.addEdge(newU,newV,newEdge);
                reverseGraph.setEdgeWeight(newEdge,w);
            }
        }
        return reverseGraph;
    }
}