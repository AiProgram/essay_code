package alg.NewAlg;

import myGraph.MyGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NewAlg {
    static double INFINITY=1<<30;
    public static MyGraph getResidualGraph(MyGraph myGraph,int scale){
        //找到最短路径时
        int nodeNum=0;
        int edgeNum=0;
        int startPoint=myGraph.startPoint;
        int sinkPoint=myGraph.sinkPoint;
        GraphPath<Integer,DefaultWeightedEdge> shortestPath;

        if(myGraph==null) return null;
        else{
            nodeNum=myGraph.graph.vertexSet().size();
            edgeNum=myGraph.graph.edgeSet().size();
        }

        try{
            BellmanFordShortestPath<Integer,DefaultWeightedEdge> bellmanFordShortestPath=new BellmanFordShortestPath<>(myGraph.graph);
            myGraph.shortestPath=bellmanFordShortestPath.getPath(startPoint,sinkPoint);
        }catch (Exception e){
            System.out.println("找不到最短路径，可能是两点间不联通");
            return null;
        }

        //产生点分离的余图
        //把G中所有不在P中边加入G‘中

        //首先复制G到G‘中
        MyGraph residualGraph=new MyGraph();
        residualGraph.costMap=new HashMap<>();
        residualGraph.graph=new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);
        residualGraph.maxComVertex=myGraph.maxComVertex;
        residualGraph.startPoint=myGraph.startPoint;
        residualGraph.sinkPoint=myGraph.sinkPoint;
        residualGraph.shortestPath=myGraph.shortestPath;

        Iterator vit=myGraph.graph.vertexSet().iterator();
        Iterator eit=myGraph.graph.edgeSet().iterator();
        while(vit.hasNext()){
            Integer v=(Integer) vit.next();
            if(residualGraph.shortestPath.getVertexList().contains(v)&&v!=startPoint&&v!=sinkPoint) continue;
            residualGraph.graph.addVertex(v);
        }

        //对于G中的每一条边，都尝试加入到G’中，但是端点被删除的添加会无效
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            int source=(int)myGraph.graph.getEdgeSource(edge);
            int target=(int)myGraph.graph.getEdgeTarget(edge);
            double weight=myGraph.graph.getEdgeWeight(edge);
            residualGraph.addNewEdge(source,target,weight,0);
        }

        if(myGraph.shortestPath.getLength()==1) residualGraph.graph.removeEdge(startPoint,sinkPoint);

        //对于shortestPath中的每一个中间点(除了起始点和终点以外)都把边(v1,v2)、(v2,v1)加入，而且加上weight和cost
        vit=residualGraph.shortestPath.getVertexList().iterator();
        while(vit.hasNext()){
            int v=(int)vit.next();
            if(v!=startPoint&&v!=sinkPoint){
                int v1=v+nodeNum;
                int v2=v+2*nodeNum;
                residualGraph.graph.addVertex(v1);//这里必须边添加成功，避免出现点不存在的情况
                residualGraph.graph.addVertex(v2);
                residualGraph.addNewEdge(v1,v2,0,scale);
                residualGraph.addNewEdge(v2,v1,0,0);
            }
        }


        eit=myGraph.graph.edgeSet().iterator();
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            double w=myGraph.graph.getEdgeWeight(edge);
            int u=(int)myGraph.graph.getEdgeSource(edge);
            int v=(int)myGraph.graph.getEdgeTarget(edge);
            List<Integer> vList=myGraph.shortestPath.getVertexList();
            //当边(source,target)在shortestPath中时
            if(vList.contains(u)&&vList.contains(v)&&(vList.indexOf(v)-vList.indexOf(u)==1)){
                int u2=u;
                int v1=v;
                if(u!=startPoint&&u!=sinkPoint)
                    u2=u+2*nodeNum;
                if(v!=startPoint&&v!=sinkPoint)
                    v1=v+nodeNum;

                residualGraph.graph.addVertex(v1);
                residualGraph.graph.addVertex(u2);
                residualGraph.addNewEdge(v1,u2,-w,0);
            }else {
                //当source是shortestPath中的点时
                if(vList.contains(u)&&u!=startPoint&&u!=sinkPoint)
                {
                    int u2=u+2*nodeNum;
                    if(vList.contains(v)&&v!=startPoint&&v!=sinkPoint){
                        residualGraph.graph.addVertex(u2);
                        residualGraph.graph.addVertex(v+nodeNum);
                        residualGraph.addNewEdge(u2,v+nodeNum,w,0);
                    }else{
                        residualGraph.graph.addVertex(u2);
                        residualGraph.graph.addVertex(v);
                        residualGraph.addNewEdge(u2,v,w,0);
                    }
                }
                //当target是shortestPath中的点时
                if(vList.contains(v)&&v!=startPoint&&v!=sinkPoint)
                {
                    int v1=v+nodeNum;
                    if(vList.contains(u)&&u!=startPoint&&u!=sinkPoint){
                        residualGraph.graph.addVertex(u+2*nodeNum);
                        residualGraph.graph.addVertex(v1);
                        residualGraph.addNewEdge(u+2*nodeNum,v1,w,0);
                    }else{
                        residualGraph.graph.addVertex(u);
                        residualGraph.graph.addVertex(v1);
                        residualGraph.addNewEdge(u,v1,w,0);
                    }
                }
            }
        }

        return residualGraph;
    }

    public static double RSPNoRecrusive(MyGraph myGraph){
        List<Integer> path;
        int startPoint=myGraph.startPoint;
        int sinkPoint=myGraph.sinkPoint;
        int maxComVertex=myGraph.maxComVertex;
        DefaultDirectedWeightedGraph graph=myGraph.graph;
        int nodeNum=graph.vertexSet().size();
        int pathMap[][]=new int[nodeNum*3][maxComVertex+1];
        double dynMat[][]=new double[nodeNum*3][maxComVertex+1];
        int min_node=0;

        //初始化
        Iterator vit=graph.vertexSet().iterator();
        Iterator eit;
        while(vit.hasNext()){
            int node=(int)vit.next();
            if(node!=startPoint)
                for(int mcv=0;mcv<maxComVertex+1;mcv++)
                    dynMat[node][mcv]=INFINITY;
        }
        for(int mcv=0;mcv<maxComVertex+1;mcv++)
            dynMat[startPoint][mcv]=0;

        boolean improveFlag=true;
        //主要算法
        for(int mcv=1;mcv<maxComVertex+1;mcv++)
        {
            vit=graph.vertexSet().iterator();
            while(vit.hasNext())
            {
                int node=(int)vit.next();
                dynMat[node][mcv]=dynMat[node][mcv-1];
                pathMap[node][mcv]=node;
            }
            improveFlag=true;
            int time=0;
            while(improveFlag)
            {
                time++;
                improveFlag=false;
                eit = graph.edgeSet().iterator();
                while (eit.hasNext())
                {
                    DefaultWeightedEdge edge = (DefaultWeightedEdge) eit.next();
                    int source = (int) graph.getEdgeSource(edge);
                    int target = (int) graph.getEdgeTarget(edge);
                    double weight = graph.getEdgeWeight(edge);
                    if (myGraph.costMap.get(edge) == 0)
                    {
                        if (dynMat[target][mcv] > dynMat[source][mcv] + weight)
                        {
                            dynMat[target][mcv] = dynMat[source][mcv] + weight;
                            pathMap[target][mcv] = source;
                            improveFlag=true;
                        }
                    } else if (myGraph.costMap.get(edge) == 1)
                    {
                        if (dynMat[target][mcv] > dynMat[source][mcv - 1] + weight) {
                            dynMat[target][mcv] = dynMat[source][mcv - 1] + weight;
                            pathMap[target][mcv] = source;
                            improveFlag=true;
                        }
                    }
                }
            }
        }
        return dynMat[sinkPoint][maxComVertex];
    }


    public static void main(String args[]){
    }
}