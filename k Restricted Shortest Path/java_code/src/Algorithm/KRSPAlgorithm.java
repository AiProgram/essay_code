package Algorithm;

import GraphStructure.MyGraph;
import jdk.jshell.spi.ExecutionControlProvider;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.List;

public class KRSPAlgorithm {
    /**
     * 求k条路径时，每加入一条路径，处理一次，因为pathP中会有反向边
     *
     * @param paths
     * @param pathP
     * @return
     */
    public List<List<Integer>> pathsXor(List<List<Integer>> paths,List<Integer> pathP){
        int startPoint=pathP.get(0);
        int desPoint=pathP.get(pathP.size()-1);
        DefaultDirectedGraph<Integer,DefaultEdge> graph=new DefaultDirectedGraph<>(DefaultEdge.class);
        //首先加入需要的点
        for(int i=0;i<paths.size();i++)
        {
            List<Integer> path=paths.get(i);
            for(int j=0;j<path.size();j++)
            {
                int node=path.get(j);
                graph.addVertex(node);
            }
        }
        for(int i=0;i<pathP.size();i++)
        {
            int node=pathP.get(i);
            graph.addVertex(node);
        }

        //处理边
        for(int i=0;i<paths.size();i++)
        {
            List<Integer> path=paths.get(i);
            for(int j=0;j<path.size()-1;j++)
            {
                int u=path.get(j);
                int v=path.get(j+1);
                graph.addEdge(u,v);
            }
        }

        for(int i=0;i<pathP.size()-1;i++)
        {
           int u=pathP.get(i);
           int v=pathP.get(i-1);
           if(graph.containsEdge(v,u)){
                graph.removeAllEdges(v,u);
           }
           else{
               graph.addEdge(u,v);
           }
        }

        //一次输出的simple path可能会有边相交，所以生成一条以后删除这条边继续生成
        List<List<Integer>>tmp=new ArrayList<>();
        while(graph.edgeSet().size()>0){
            AllDirectedPaths<Integer,DefaultEdge> allDirectedPaths=new AllDirectedPaths<>(graph);
            List<GraphPath<Integer,DefaultEdge>> oriPaths=allDirectedPaths.getAllPaths(startPoint,desPoint,true,null);
            GraphPath<Integer,DefaultEdge> oriPath=oriPaths.get(0);
            List<Integer>tmpPath=oriPath.getVertexList();
            tmp.add(tmpPath);

            for(int i=0;i<tmpPath.size()-1;i++)
            {
                int u=tmpPath.get(i);
                int v=tmpPath.get(i+1);
                graph.removeAllEdges(u,v);
            }
        }
        return tmp;
    }

    /**
     * 将图中的所有相关路径反向，并且只有cost取反，默认不修改原图
     * @param oriGraph
     * @param paths
     * @return
     */
    public MyGraph getCostReverseGraph(MyGraph oriGraph,List<List<Integer>>paths){
        MyGraph reverseGraph=oriGraph.copyGraph();

        for(int i=0;i<paths.size();i++)
        {
            List<Integer> path=paths.get(i);
            for(int index=0;index<path.size()-1;index++)
            {
                int u=path.get(index);
                int v=path.get(index+1);
                DefaultWeightedEdge edge=reverseGraph.graph.getEdge(u,v);

                int cost=reverseGraph.costMap.get(edge);
                int delay=reverseGraph.delayMap.get(edge);

                reverseGraph.removeAllEdges(u,v);
                reverseGraph.addNewEdge(v,u,-cost,delay);
            }
        }
        return reverseGraph;
    }

    /**
     *
     * 获得k条最短路径，只考虑cost不考虑delay
     * @param myGraph
     * @param startPoint
     * @param desPoint
     * @param spNum
     * @return
     */
    public List<List<Integer>> getKSPWithCost(MyGraph myGraph,int startPoint,int desPoint,int spNum){
        MyGraph auxGraph=myGraph.copyGraph();
        List<List<Integer>> allSp=new ArrayList<>();
        List<Integer>sp=new ArrayList<>();
        int curSpNum=0;
        while(curSpNum<spNum){
            try{
                auxGraph.setCurentWeight(MyGraph.CurentWeight.cost);//需要事先设置weight
                BellmanFordShortestPath<Integer,DefaultWeightedEdge>shortestPath=new BellmanFordShortestPath<>(auxGraph.graph);
                GraphPath<Integer,DefaultWeightedEdge> oriSP=shortestPath.getPath(startPoint,desPoint);
                sp=oriSP.getVertexList();

                if(sp==null) return null;
                else{
                    allSp=pathsXor(allSp,sp);
                    curSpNum+=1;
                }
                auxGraph=getCostReverseGraph(myGraph,allSp);
            }catch (Exception e){
                return null;
            }
        }
        return allSp;
    }

    public MyGraph getDelayReverseGraph(MyGraph oriGraph,List<List<Integer>>paths){
        MyGraph reverseGraph=oriGraph.copyGraph();

        for(int i=0;i<paths.size();i++)
        {
            List<Integer> path=paths.get(i);
            for(int index=0;index<path.size()-1;index++)
            {
                int u=path.get(index);
                int v=path.get(index+1);
                DefaultWeightedEdge edge=reverseGraph.graph.getEdge(u,v);

                int cost=reverseGraph.costMap.get(edge);
                int delay=reverseGraph.delayMap.get(edge);

                reverseGraph.removeAllEdges(u,v);
                reverseGraph.addNewEdge(v,u,cost,-delay);
            }
        }
        return reverseGraph;
    }

    public List<List<Integer>>getKSPWithDelay(MyGraph myGraph,int startPoint,int desPoint,int spNum){
        MyGraph auxGraph=myGraph.copyGraph();
        List<List<Integer>> allSp=new ArrayList<>();
        List<Integer>sp=new ArrayList<>();
        int curSpNum=0;
        while(curSpNum<spNum){
            try{
                auxGraph.setCurentWeight(MyGraph.CurentWeight.delay);//需要事先设置weight
                BellmanFordShortestPath<Integer,DefaultWeightedEdge>shortestPath=new BellmanFordShortestPath<>(auxGraph.graph);
                GraphPath<Integer,DefaultWeightedEdge> oriSP=shortestPath.getPath(startPoint,desPoint);
                sp=oriSP.getVertexList();

                if(sp==null) return null;
                else{
                    allSp=pathsXor(allSp,sp);
                    curSpNum+=1;
                }
                auxGraph=getDelayReverseGraph(myGraph,allSp);
            }catch (Exception e){
                return null;
            }
        }
        return allSp;
    }
    public enum Attr{
        delay,cost
    }

    public double countAttr(MyGraph graph,List<List<Integer>>paths,Attr attr){
        double sum=0;
        for(int i=0;i<paths.size();i++)
        {
            List<Integer> path=paths.get(i);
            for(int index=0;index<path.size()-1;index++)
            {
                int u=path.get(index);
                int v=path.get(index+1);
                DefaultWeightedEdge edge=graph.graph.getEdge(u,v);
                if(attr==Attr.cost) sum+=graph.costMap.get(edge);
                else sum+=graph.delayMap.get(edge);
            }
        }
        return sum;
    }

    public int getSplitNode(int oriNode,int upperNum,int nodeNum){
        return oriNode+upperNum*nodeNum;
    }

    public class OriNode{
        int oriNode;
        int upperNum;
    }

    public OriNode getOriNode(int curNode,int nodeNum){
        OriNode splitNode=new OriNode();
        splitNode.oriNode=curNode%nodeNum;
        splitNode.upperNum=(curNode-splitNode.oriNode)/nodeNum;
        return splitNode;
    }

    /**
     *获得环O需要获得拆点的辅助图，这个就是辅助图
     * @param myGraph
     * @param costBound
     * @param desPoint
     * @return
     */
    public MyGraph getCycleAuxGraph(MyGraph myGraph,int costBound,int desPoint){
        int nodeNum=myGraph.graph.vertexSet().size();
        MyGraph auxGraph=new MyGraph();
        auxGraph.setCurentWeight(MyGraph.CurentWeight.delay);
        List<Integer>vertexList=new ArrayList<>(myGraph.graph.vertexSet());
        List<DefaultWeightedEdge> edgeList=new ArrayList<>(myGraph.graph.edgeSet());

        for(int i=0;i<vertexList.size();i++)
        {
            int node=vertexList.get(i);
            for(int upperNum=0;upperNum<costBound+1;upperNum++)
            {
                int splitNode=getSplitNode(node,upperNum,nodeNum);
                auxGraph.graph.addVertex(splitNode);
            }
        }

        for(int i=0;i<edgeList.size();i++)
        {
            DefaultWeightedEdge edge=edgeList.get(i);
            int u=myGraph.graph.getEdgeSource(edge);
            int v=myGraph.graph.getEdgeTarget(edge);
            int cost=myGraph.costMap.get(edge);
            int delay=myGraph.delayMap.get(edge);
            for(int uUpperNum=0;uUpperNum<costBound+1;uUpperNum++)
            {
                int vUpperNum=uUpperNum+cost;
                if(vUpperNum<costBound && vUpperNum>=0)
                {
                    int newU=getSplitNode(u,uUpperNum,nodeNum);
                    int newV=getSplitNode(v,vUpperNum,nodeNum);
                    auxGraph.addNewEdge(newU,newV,0,delay);
                }
            }
        }

        for(int upperNum=1;upperNum<costBound+1;upperNum++)
        {
            int splitNode=getSplitNode(desPoint,upperNum,nodeNum);
            auxGraph.addNewEdge(splitNode,desPoint,0,0);
        }
        return auxGraph;
    }

    public List<Integer> getOriPath(List<Integer>path,int nodeNum){
        List<Integer>tmp=new ArrayList<>();
        for(int i=0;i<path.size();i++)
        {
            int node=path.get(i);
            OriNode oriNode=getOriNode(node,nodeNum);
            tmp.add(oriNode.oriNode);
        }
        return tmp;
    }

    public List<Integer>getBicameralCycle(MyGraph reverseGraph,int costBound,int desPoint){
        MyGraph auxGraph=getCycleAuxGraph(reverseGraph,costBound,desPoint);
        int nodeNum=reverseGraph.graph.vertexSet().size();

        //发现有负环时使用负环

        //等待编写负环使用代码

        //没有负环时
        List<Integer>vertexList=new ArrayList<>(reverseGraph.graph.vertexSet());
        for(int i=0;i<vertexList.size();i++)
        {
            int node=vertexList.get(i);
            for(int upperNumS=0;upperNumS<costBound;upperNumS++)
            {
                for(int upperNumT=upperNumS+1;upperNumT<costBound+1;upperNumT++)
                {
                    int s=getSplitNode(node,upperNumS,nodeNum);
                    int t=getSplitNode(node,upperNumT,nodeNum);
                    auxGraph.setCurentWeight(MyGraph.CurentWeight.delay);
                    BellmanFordShortestPath<Integer,DefaultWeightedEdge>shortestPath=new BellmanFordShortestPath<>(auxGraph.graph);
                    try{//可能存在不可到达的边
                        double delaySum=shortestPath.getPathWeight(s,t);
                        if(delaySum>0) continue;//没有改善时启用这条路径
                        else{
                            GraphPath<Integer,DefaultWeightedEdge>path=shortestPath.getPath(s,t);
                            return getOriPath(path.getVertexList(),nodeNum);//这里返回的环是点集，且首尾重复
                        }
                    }catch (Exception e){
                        continue;
                    }
                }
            }

        }
        return null;
    }
    public static void main(String args[]){

    }
}
