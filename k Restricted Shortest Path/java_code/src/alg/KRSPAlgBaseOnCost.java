package alg;

import myGraph.MyGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.List;

public class KRSPAlgBaseOnCost {
    /**
     * 求k条路径时，每加入一条路径，处理一次，因为pathP中会有反向边
     *
     * @param paths
     * @param pathP
     * @return
     */
    public final int INF=1<<28;
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
           int v=pathP.get(i+1);
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
                e.printStackTrace();
                return null;
            }
        }
        return allSp;
    }
    public enum Attr{
        delay,cost
    }

    public int countAttr(MyGraph graph,List<List<Integer>>paths,Attr attr){
        int  sum=0;
        for(int i=0;i<paths.size();i++)
        {
            List<Integer> path=paths.get(i);
            for(int index=0;index<path.size()-1;index++)
            {
                int u=path.get(index);
                int v=path.get(index+1);
                DefaultWeightedEdge edge=graph.graph.getEdge(u,v);
//                if(!graph.graph.containsEdge(edge))
//                    System.out.println("找不到边");
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

    public List<Integer>findNegativeCycle(MyGraph graph,int startPoint){
        int nodeNum=graph.graph.vertexSet().size();
        int distArr[]=new int[nodeNum];
        int pre[]=new int[nodeNum];
        for(int i=0;i<nodeNum;i++)
        {
            distArr[i]=INF;
            pre[i]=-1;
        }

        List<DefaultWeightedEdge>edgeList=new ArrayList<>(graph.graph.edgeSet());
        for(int i=1;i<nodeNum;i++) {
            for (int j = 0; j < edgeList.size(); j++) {
                DefaultWeightedEdge edge = edgeList.get(j);
                int w = graph.delayMap.get(edge);
                int u = graph.graph.getEdgeSource(edge);
                int v = graph.graph.getEdgeTarget(edge);
                if (distArr[u] + w < distArr[v]) {
                    distArr[v] = distArr[u] + w;
                    pre[v] = u;
                }
            }
        }

        boolean existArr[]=new boolean[nodeNum];
        for(int i=0;i<nodeNum;i++)
            existArr[i]=false;

        int negativeOri=0;
        boolean hasCycle=false;
        for(int i=0;i<edgeList.size();i++)
        {
            DefaultWeightedEdge edge=edgeList.get(i);
            int u=graph.graph.getEdgeSource(edge);
            int v=graph.graph.getEdgeTarget(edge);
            int w=graph.delayMap.get(edge);
            if(distArr[u]+w<distArr[v])
            {
                hasCycle=true;
                negativeOri=u;
            }
        }
        if(hasCycle)
        {
            int curNode=negativeOri;
            List<Integer> cycle=new ArrayList<>();
            while(true)
            {
                if(existArr[curNode]==false)//还没有发现环的终点，也就是重复的点
                {
                    existArr[curNode]=true;
                    cycle.add(curNode);
                    curNode=pre[curNode];
                }else{
                    cycle.add(curNode);
                    List<Integer>tmp=new ArrayList<>();
                    boolean flag=false;
                    for(int i=0;i<cycle.size();i++)//环已经找到，但是前面可能有多余的点
                    {
                        int node=cycle.get(i);
                        if(flag==false)
                        {
                            if (node == curNode) {
                                flag = true;
                                tmp.add(curNode);
                            }
                        }
                        else{
                            tmp.add(node);
                        }
                    }
                    Collections.reverse(tmp);
                    return tmp;
                }
            }
        }
        else
            return null;

    }

    /**
     *
     * 可能存在找到的环不是简单环的情况，这时取最好的环
     * @param myGraph   反向但是没有拆点的辅助图
     * @param ori_cycle 拆点之前的环
     * @return  非简单环中最好的环
     */
    public List<Integer> getBestCycle(MyGraph myGraph,List<Integer>ori_cycle){
        MyGraph tmpGraph=new MyGraph();
        List<Integer>bestCycle=new ArrayList<>();
        int bestDelay=INF;
        for(int i=0;i<ori_cycle.size();i++)
        {
            int node=ori_cycle.get(i);
            tmpGraph.graph.addVertex(node);
        }
        for(int i=0;i<ori_cycle.size()-1;i++)
        {
            int u=ori_cycle.get(i);
            int v=ori_cycle.get(i+1);
            DefaultWeightedEdge edge=myGraph.graph.getEdge(u,v);
            int delay=myGraph.delayMap.get(edge);
            tmpGraph.addNewEdge(u,v,0,delay);
        }
        TarjanSimpleCycles<Integer,DefaultWeightedEdge>simpleCycles=new TarjanSimpleCycles<>(tmpGraph.graph);
        List<List<Integer>>cycles=simpleCycles.findSimpleCycles();
        List<Integer>cycle=new ArrayList<>();
        for(int i=0;i<cycles.size();i++)
        {
            cycle=cycles.get(i);
            cycle.add(cycle.get(0));
            List<List<Integer>>tmpCycles=new ArrayList<>();
            tmpCycles.add(cycle);
            int curDelay=countAttr(myGraph,tmpCycles,Attr.delay);
            if(curDelay<bestDelay)
            {
                bestCycle=cycle;
                bestDelay=curDelay;
            }
        }
        return bestCycle;
    }

    public List<Integer>getBicameralCycle(MyGraph reverseGraph,List<List<Integer>>ksp,int costBound,int startPoint,int desPoint,int spNum){
        MyGraph auxGraph=getCycleAuxGraph(reverseGraph,costBound*2,desPoint);
        int nodeNum=reverseGraph.graph.vertexSet().size();


        //发现有负环时使用负环
//        for(int upperNum=0;upperNum<costBound+1;upperNum++)
//        {
            List<Integer>cycle=findNegativeCycle(auxGraph,getSplitNode(startPoint,costBound,nodeNum));
            if(cycle!=null){
                System.out.println("负环");
                List<Integer>tmp=getOriPath(cycle,nodeNum);
                return getBestCycle(reverseGraph,tmp);
                //当找到的环不可取时需要 继续寻找,不可取的原因见cyclePathXor函数
//                if(cyclePathXor(tmp,ksp,spNum)!=null) return tmp;
//                else {
//                    getBestCycle(reverseGraph,tmp);
//                    System.err.println("负圈不可用");
//                    System.err.println(ksp);
//                    System.err.println(cycle);
//                    break;
//                }
            }
//        }
        //等待编写负环使用代码
        //auxGraph=getCycleAuxGraph(reverseGraph,costBound*2,desPoint);
        BellmanFordShortestPath<Integer,DefaultWeightedEdge>shortestPath=new BellmanFordShortestPath<>(auxGraph.graph);
        //没有负环时
        List<Integer>vertexList=new ArrayList<>(reverseGraph.graph.vertexSet());
        auxGraph.setCurentWeight(MyGraph.CurentWeight.delay);
        for(int i=0;i<vertexList.size();i++)
        {
            int node=vertexList.get(i);
//            for(int upperNumS=0;upperNumS<costBound;upperNumS++)
//            {
            try {
                int upperNumS = costBound;
                int s = getSplitNode(node, upperNumS, nodeNum);
                ShortestPathAlgorithm.SingleSourcePaths<Integer,DefaultWeightedEdge> sp=shortestPath.getPaths(s);
                for (int upperNumT = upperNumS + 1; upperNumT < 2 * costBound + 1; upperNumT++) {
                    int t = getSplitNode(node, upperNumT, nodeNum);
                    GraphPath<Integer, DefaultWeightedEdge> path = sp.getPath(t);
                    List<List<Integer>>tmp=new ArrayList<>();
                    tmp.add(path.getVertexList());
                    double delaySum = countAttr(auxGraph,tmp,Attr.delay);
                    if (delaySum > 0) continue;//没有改善时启用这条路径
                    else {
                        cycle = getOriPath(path.getVertexList(), nodeNum);//这里返回的环是点集，且首尾重复
                        return getBestCycle(reverseGraph, cycle);
                        //当找到的环不可取时继续寻找
//                            if(cyclePathXor(cycle,ksp,spNum)!=null) return cycle;
//                            else{
//                                getBestCycle(reverseGraph,cycle);
//                                System.err.println("圈不可用");
//                                System.err.println(ksp);
//                                System.err.println(cycle);
//                                System.err.println(path.getVertexList());
//                            }
                    }

                }
            }catch (Exception e){
                continue;
            }
//            }

        }
        return null;
    }

    public MyGraph getAllReverseGraph(MyGraph myGraph,List<List<Integer>>paths){
        MyGraph reverseGraph=myGraph.copyGraph();
        for(int i=0;i<paths.size();i++)
        {
            List<Integer>path=paths.get(i);
            for(int index=0;index<path.size()-1;index++)
            {
                int u=path.get(index);
                int v=path.get(index+1);
                DefaultWeightedEdge edge=myGraph.graph.getEdge(u,v);
                int cost=myGraph.costMap.get(edge);
                int delay=myGraph.delayMap.get(edge);
                reverseGraph.removeAllEdges(u,v);
                reverseGraph.addNewEdge(v,u,-cost,-delay);
            }
        }
        return reverseGraph;
    }

    public List<List<Integer>>cyclePathXor(List<Integer>cycle,List<List<Integer>>paths,int spNum){
        DefaultDirectedGraph<Integer,DefaultEdge>graph=new DefaultDirectedGraph<>(DefaultEdge.class);
        int startPoint=paths.get(0).get(0);
        int desPoint=paths.get(0).get(paths.get(0).size()-1);
        //事先添加所有的点
        for(int i=0;i<cycle.size();i++)
        {
            int node=cycle.get(i);
            graph.addVertex(node);
        }
        for(int i=0;i<paths.size();i++)
        {
            List<Integer>path=paths.get(i);
            for(int j=0;j<path.size();j++)
            {
                int node=path.get(j);
                graph.addVertex(node);
            }
        }
        for(int  i=0;i<paths.size();i++)
        {
            List<Integer>path=paths.get(i);
            for(int j=0;j<path.size()-1;j++)
            {
                int u=path.get(j);
                int v=path.get(j+1);
                graph.addEdge(u,v);
            }
        }
        //这里存在一种特殊情况：在paths中有一条边(u,v)，而cycle中有两条(v,u)，这时第一条边(v,u)抵消掉了(u,v)，而第二条(v,u)会被错误添加
        //但是 我们可以保证paths中没有边 会重复，所以不会有相反的情况发生
        Map<Integer,Integer>deletedEdge=new HashMap<>();//记录已经被删掉的反向边
        for(int i=0;i<cycle.size()-1;i++)
        {
            int u=cycle.get(i);
            int v=cycle.get(i+1);
            if(graph.containsEdge(v,u))
            {
                graph.removeAllEdges(v,u);
                deletedEdge.put(v,u);
            }else{
                if(!deletedEdge.containsKey(v)||deletedEdge.get(v)!=u)//处理特殊情况
                    graph.addEdge(u,v);
            }
        }
        List<List<Integer>>tmp=new ArrayList<>();
        int pNum=0;
        while(pNum<spNum){
            AllDirectedPaths<Integer,DefaultEdge> allDirectedPaths=new AllDirectedPaths<>(graph);
            List<GraphPath<Integer,DefaultEdge>> oriPaths=allDirectedPaths.getAllPaths(startPoint,desPoint,true,null);
            GraphPath<Integer,DefaultEdge> oriPath;
            try {
                //前面的路径和bicameral cycle的异或可能会使不相交路径的条数减少，所以可能会使异或后不相交路径数量少于规定的数值，这时bicameral cycle不可取
                oriPath = oriPaths.get(0);
            }catch (Exception e){
                //e.printStackTrace();
                System.out.println("残余路径"+ tmp);
                return null;
            }
            List<Integer>tmpPath=oriPath.getVertexList();
            tmp.add(tmpPath);
            pNum++;
            for(int i=0;i<tmpPath.size()-1;i++)
            {
                int u=tmpPath.get(i);
                int v=tmpPath.get(i+1);
                graph.removeAllEdges(u,v);
            }
        }
        return tmp;
    }
    public List<List<Integer>>getKSP(MyGraph graph,int startPoint,int desPoint,int spNum,int maxDelay){
        List<List<Integer>> kspForDelay=getKSPWithDelay(graph,startPoint,desPoint,spNum);
        if(kspForDelay==null)//可能delay无法得到ksp
            return null;
        int totalDelay=countAttr(graph,kspForDelay,Attr.delay);
        if(totalDelay>maxDelay)//提前结束算法，不会有结果
            return null;


        List<List<Integer>>kspForCost=getKSPWithCost(graph,startPoint,desPoint,spNum);
        totalDelay=countAttr(graph,kspForCost,Attr.delay);
        if(totalDelay<maxDelay)//提前结束，已经找到结果
            return kspForCost;

        int lowBoundCost=countAttr(graph,kspForCost,Attr.cost);
        int upBoundCost=countAttr(graph,kspForDelay,Attr.cost);
        while(lowBoundCost<upBoundCost)
        {
            System.out.println("bound: "+lowBoundCost+"-----"+upBoundCost);
            System.out.println("current delay:  "+countAttr(graph,kspForCost,Attr.delay)+" current cost: "+countAttr(graph,kspForCost,Attr.cost));
            System.out.println("curPath:  "+kspForCost);
            //此处利用二分法缩短costbound
            int midCostBound=(lowBoundCost+upBoundCost)/2;
            if(midCostBound==lowBoundCost)//不然会死循环
                break;
            MyGraph reverseGraph=getAllReverseGraph(graph,kspForCost);
            int curCost=countAttr(graph,kspForCost,Attr.cost);
            List<Integer> bicameralCycle=getBicameralCycle(reverseGraph,kspForCost,midCostBound-curCost,startPoint,desPoint,spNum);
            System.out.println("cycle:  "+bicameralCycle);
            if(bicameralCycle!=null)
            {
                kspForCost=cyclePathXor(bicameralCycle,kspForCost,spNum);
                //System.out.println(kspForCost);
                if(countAttr(graph,kspForCost,Attr.delay)<maxDelay)
                {
                    upBoundCost=midCostBound;
                    return kspForCost;
                }else{
                    lowBoundCost=midCostBound;
                }
            }else{
                lowBoundCost=midCostBound;
            }
        }
        //如果到了最后delay还是不满足条件，说明只能取delay最小的集合了
        if(countAttr(graph,kspForCost,Attr.delay)>maxDelay)
            return kspForDelay;
        return kspForCost;
    }
    public static void main(String args[]){
    }
}
