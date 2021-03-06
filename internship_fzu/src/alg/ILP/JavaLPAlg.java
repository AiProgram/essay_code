package alg.ILP;

import myGraph.ILPGraph;
import myGraph.MyGraph;
import ilog.cplex.IloCplex;
import org.gnu.glpk.*;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class JavaLPAlg {
    /**
     * 用于读取JSON图的文本数据
     * @param fileName JSON图文件名（完整路径）
     * @return JSON图文本内容
     */
    public static String readJsonGraph(String fileName)
    {
        String graphData=null;
        String line;
        try {
            File jsonFile = new File(fileName);
            if (!jsonFile.exists())
            {
                System.out.println("cant find the json file ");
                return null;
            } else {
                FileReader reader = new FileReader(jsonFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder=new StringBuilder();
                while ((line=bufferedReader.readLine())!=null)
                {
                    builder.append(line);
                }
                graphData=builder.toString();
            }
        }catch (Exception e){

        }
        return graphData;
    }

    /**
     * 将读取到的JSON图文件文本内容转换成自定义的图数据类型
     * @param jsonStr JSON图文本内容
     * @return 图
     */
    public static MyGraph parseJsonToGraph(String jsonStr)
    {
        int id;
        int source;
        int target;
        int weight;
        int cost;
        int realSrc;
        int realTar;

        JSONObject jsonObject=new JSONObject(jsonStr);

        JSONArray  nodesArr=new JSONArray();
        nodesArr=jsonObject.getJSONArray("nodes");
        JSONArray  edgesArr=new JSONArray();
        edgesArr=jsonObject.getJSONArray("links");


        MyGraph myGraph=new MyGraph();
        DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge> graph=new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        myGraph.graph=graph;
        myGraph.costMap=new HashMap<>();
        int nodeNumber=nodesArr.length();
        JSONObject graphObject=jsonObject.getJSONObject("graph");
//        int  startPoint =graphObject.getInt("S");
//        int  sinkPoint =graphObject.getInt("T");
//        int bound=graphObject.getInt("bound");
//        myGraph.startPoint=startPoint;
//        myGraph.sinkPoint=sinkPoint;
//        myGraph.maxComVertex=bound;
        myGraph.nodeNum=nodesArr.length();



        //加入顶点
        for(int i=0;i<myGraph.nodeNum;i++)
        {
            JSONObject tmpObject=nodesArr.getJSONObject(i);
            id=tmpObject.getInt("id");
            graph.addVertex(id);
        }


        //加入边
        EdgeFactory<Integer,DefaultWeightedEdge> edgeEdgeFactory=graph.getEdgeFactory();
        for(int i=0;i<edgesArr.length();i++)
        {
            JSONObject tmpObject=edgesArr.getJSONObject(i);
            source=tmpObject.getInt("source");
            target=tmpObject.getInt("target");
            weight=tmpObject.getInt("weight");
            cost=0;
            realSrc=source;
            realTar=target;

            myGraph.addNewEdge(realSrc,realTar,weight,cost);

        }
        myGraph.edgeNum=myGraph.graph.edgeSet().size();
        return myGraph;
    }

    /**
     * 用于指定线性规划算法的求解器，目前可选GLPK或者CPLEX，使用时需要指定二者的java库
     * 路径
     */
    public  static enum LPSolver{
        GLPK,CPLEX
    }

    /**
     * @param oriGraph 问题的原图（未加任何变换）
     * @param probId 问题编号（调试时输出图文件使用）
     * @param lpSolver 线性规划求解器种类
     * @return 问题的解（weight总和）
     */
    public static double solveWithLP(ILPGraph oriGraph, int probId, LPSolver lpSolver){
        glp_prob lp;
        glp_iocp parm;
        glp_cpxcp p;
        SWIGTYPE_p_int index;
        SWIGTYPE_p_double val;
        DirectedWeightedMultigraph<Integer,DefaultWeightedEdge> graph=oriGraph.graph;
        Map<DefaultWeightedEdge,Integer> costMap=oriGraph.costMap;
        int ret;
        int edgeNumber=oriGraph.edgeNum;
        int nodeNumber=oriGraph.nodeNum;
        int startPoint=oriGraph.startPoint;
        int sinkPoint=oriGraph.sinkPoint;
        int bound=oriGraph.maxComVertex;
        List<Integer>vertexList=new ArrayList<>(oriGraph.graph.vertexSet());
        List<DefaultWeightedEdge>edgeList=new ArrayList<>(oriGraph.graph.edgeSet());

        System.out.println(edgeNumber+"  "+nodeNumber);

        double result=0;
        try{

            //建立问题
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created");
            GLPK.glp_set_prob_name(lp, "myProblem");

            //定义变量
            GLPK.glp_add_cols(lp, edgeNumber);
            for(int i=1;i<=edgeNumber;i++)
            {
                GLPK.glp_set_col_kind(lp,i,GLPKConstants.GLP_IV);
                GLPK.glp_set_col_bnds(lp,i,GLPKConstants.GLP_DB,0,1);
                GLPK.glp_set_col_name(lp,i,"x"+i);
            }

            //设置约束

            //申请内存
            index=GLPK.new_intArray(edgeNumber);
            val=GLPK.new_doubleArray(edgeNumber);

            //预先设置约束条数
            GLPK.glp_add_rows(lp,nodeNumber);

            //具体设置约束
            Iterator vit=graph.vertexSet().iterator();
            for(int i=1,j=1;i<=nodeNumber;i++)
            {
                int a;
                int b;

                GLPK.glp_set_row_name(lp,j,"c"+j);

                //int node=graph.nodes[i-1];
                int node=vertexList.get(i-1);
                if(node== sinkPoint) continue;
                Iterator eit=graph.edgeSet().iterator();
                for(int k=1;k<=edgeNumber;k++)
                {
                    //MyEdge edge=graph.edge.get(k-1);
                    DefaultWeightedEdge edge=edgeList.get(k-1);
                    if((int)graph.getEdgeSource(edge)==node)
                    {
                        a=1;
                    }else if((int)graph.getEdgeTarget(edge)==node) {
                        a=-1;
                    }else{
                        a=0;
                    }
                    GLPK.intArray_setitem(index,k,k);
                    GLPK.doubleArray_setitem(val,k,a);
                }
                if(node== startPoint)
                {
                    b=2;
                }else{
                    b=0;
                }
                GLPK.glp_set_row_bnds(lp,j,GLPKConstants.GLP_FX,b,b);

                GLPK.glp_set_mat_row(lp, j, edgeNumber, index, val);
                j++;
            }

            GLPK.glp_set_row_name(lp,nodeNumber,"c"+0);
            GLPK.glp_set_row_bnds(lp,nodeNumber,GLPKConstants.GLP_UP,0,bound);
            Iterator eit=graph.edgeSet().iterator();
            for(int k=1;k<=edgeNumber;k++)
            {
                GLPK.intArray_setitem(index,k,k);
                //Edge edge=graph.edges.get(k-1);
                DefaultWeightedEdge edge=edgeList.get(k-1);
                GLPK.doubleArray_setitem(val,k,costMap.get(edge));
            }
            GLPK.glp_set_mat_row(lp,nodeNumber,edgeNumber,index,val);

            //释放内存
            GLPK.delete_intArray(index);
            GLPK.delete_doubleArray(val);

            //设置最值式
            GLPK.glp_set_obj_name(lp, "result");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            eit=graph.edgeSet().iterator();
            for(int i=1;i<=edgeNumber;i++)
            {
                //Edge edge=graph.edges.get(i);
                DefaultWeightedEdge edge=edgeList.get(i-1);
                GLPK.glp_set_obj_coef(lp, i, graph.getEdgeWeight(edge));
            }

            //解决模型
            if(lpSolver==LPSolver.GLPK) {//使用GLPK作为lpsolver
                parm = new glp_iocp();
                GLPK.glp_init_iocp(parm);
                parm.setPresolve(GLPKConstants.GLP_ON);
                ret = GLPK.glp_intopt(lp, parm);
                // 获得结果
                if (ret == 0) {
                    result = getLPSolution(lp);
                    if (GLPK.glp_mip_status(lp) != GLPKConstants.GLP_OPT) {//问题运行成功但没有达到要求
                        System.err.println("error");
                        GLPK.glp_delete_prob(lp);
                        return 0;//0表示没有结果，因为实际情况不会为0
                    }
                    else {
                        GLPK.glp_delete_prob(lp);
                        return result;
                    }
                } else {
                    // 释放内存
                    GLPK.glp_delete_prob(lp);
                    System.out.println("The problem could not be solved");
                    return -1;
                }
            } else if (lpSolver == LPSolver.CPLEX) {//使用CPLEX
                //将GLPK改成cplex求解
                p = new glp_cpxcp();
                GLPK.glp_init_cpxcp(p);
                GLPK.glp_write_lp(lp, p, "tmp.lp");
                GLPK.glp_delete_prob(lp);
                try {
                    IloCplex cplex = new IloCplex();
                    cplex.setParam(IloCplex.Param.Threads, 1);//为了与其他算法对比，设置为单线程
                    cplex.importModel("tmp.lp");
                    if (cplex.solve()) {
                        System.out.println("Solution status = " + cplex.getStatus());
                        System.out.println("Solution value  = " + cplex.getObjValue());
                        if (cplex.getStatus() == IloCplex.Status.Optimal)
                            return cplex.getObjValue();
                        else
                            return -1;

                    }
                    cplex.end();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
        //释放内存
        GLPK.glp_delete_prob(lp);
        return result;
    }

    /**
     * 为LPPDSP算法提供G'，G'是从原始图变换过来的
     * @param myGraph G 等待变换的原图
     * @return G' LPPDSP辅助图
     */
    public  static ILPGraph getGraphForILP(MyGraph myGraph)
    {
        int startPoint =myGraph.startPoint;
        int sinkPoint =myGraph.sinkPoint;
        int bound=myGraph.maxComVertex;

        ILPGraph lpGraph=new ILPGraph();
        lpGraph.startPoint=startPoint;
        lpGraph.sinkPoint=sinkPoint;
        lpGraph.maxComVertex=bound;
        lpGraph.nodeNum=myGraph.nodeNum;
        lpGraph.edgeNum=myGraph.edgeNum;
        lpGraph.graph=new DirectedWeightedMultigraph(DefaultWeightedEdge.class);
        lpGraph.costMap=new HashMap<>();

        lpGraph.graph.addVertex(startPoint);
        lpGraph.graph.addVertex(sinkPoint);
        //我们认为点的id应该在0和nodeNum-1之间
        int nodeNum=myGraph.graph.vertexSet().size();
        Iterator vit=myGraph.graph.vertexSet().iterator();
        while(vit.hasNext()){
            int node=(Integer)vit.next();
            if(node==startPoint||node==sinkPoint) continue;
            else{
                lpGraph.graph.addVertex(node);
                lpGraph.graph.addVertex(node+nodeNum);
            }
        }

        //在G中进入v的边现在G’中进入v1，而离开v的边现在G‘中离开v2
        Iterator eit=myGraph.graph.edgeSet().iterator();
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            double w=myGraph.graph.getEdgeWeight(edge);
            int u=(int)myGraph.graph.getEdgeSource(edge);
            int v=(int)myGraph.graph.getEdgeTarget(edge);
            int v1=0;
            int u2=0;

            if(v==startPoint||v==sinkPoint) v1=v;
            else v1=v;
            if(u==startPoint||u==sinkPoint) u2=u;
            else u2=u+nodeNum;

            lpGraph.addNewEdge(u2,v1,w,0);//这里暂时使用全局变量储存cost，因为这个库不允许单独添加属性，后面可以改进
        }

        //在G’中加入边(v1,v2)并且它的cost是1而weight是0
        vit=myGraph.graph.vertexSet().iterator();
        while(vit.hasNext()){
            int node=(int)vit.next();
            int v1=node;
            int v2=node+nodeNum;
            if(node==startPoint||node==sinkPoint) continue;
            lpGraph.addNewEdge(node,node+nodeNum,0,1);
            lpGraph.addNewEdge(node,node+nodeNum,0,0);
        }
        lpGraph.edgeNum=lpGraph.graph.edgeSet().size();
        lpGraph.nodeNum=lpGraph.graph.vertexSet().size();
        return lpGraph;
    }

    /**
     * @param lp 求解用给的GLPK问题对象
     * @return 最后的解（weight总和）
     */
    static double getLPSolution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;

        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_mip_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
//        n = GLPK.glp_get_num_cols(lp);
//        for (i = 1; i <= n; i++) {
//            name = GLPK.glp_get_col_name(lp, i);
//            val = GLPK.glp_mip_col_val(lp, i);
//            System.out.print(name);
//            System.out.print(" = ");
//            System.out.println(val);
//        }
        return val;
    }
    public void  test(){
    }

    public static  void main(String args[]){
        JavaLPAlg javaLPAlg =new JavaLPAlg();
        javaLPAlg.test();
    }
}