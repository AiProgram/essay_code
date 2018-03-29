package Alg.ILP;

import java.io.*;
import java.util.*;

import Alg.NewAlg.NewAlg;
import GraphIO.GraphRandomGenerator;
import MyGraph.*;
import MyGraph.MyGraph;
import org.gnu.glpk.*;
import org.jgrapht.graph.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jgrapht.*;

import javax.xml.stream.FactoryConfigurationError;

public class JavaLPAlg {

    Map<DefaultWeightedEdge,Integer> costMap=new HashMap<>();
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



        //deal with the nodes
        for(int i=0;i<myGraph.nodeNum;i++)
        {
            JSONObject tmpObject=nodesArr.getJSONObject(i);
            id=tmpObject.getInt("id");
            graph.addVertex(id);
        }


        //deal with the edges
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

            //graph.edges.add(new Edge(realSrc,realTar,weight,cost));
//            //The graph with duplicates are too complex, so we add the second duplicator here
//            if(cost==1)
//            {
//                myGraph.addNewEdge(realSrc,realTar,0,0);
//            }
        }
        myGraph.edgeNum=myGraph.graph.edgeSet().size();
        return myGraph;
    }

    public static double solveWithGLPK(ILPGraph myGraph,int probId){
        glp_prob lp;
        glp_smcp parm;
        SWIGTYPE_p_int index;
        SWIGTYPE_p_double val;
        DirectedWeightedMultigraph<Integer,DefaultWeightedEdge> graph=myGraph.graph;
        Map<DefaultWeightedEdge,Integer> costMap=myGraph.costMap;
        int ret;
        int edgeNumber=myGraph.edgeNum;
        int nodeNumber=myGraph.nodeNum;
        int startPoint=myGraph.startPoint;
        int sinkPoint=myGraph.sinkPoint;
        int bound=myGraph.maxComVertex;
        List<Integer>vertexList=new ArrayList<>(myGraph.graph.vertexSet());
        List<DefaultWeightedEdge>edgeList=new ArrayList<>(myGraph.graph.edgeSet());

        System.out.println(edgeNumber+"  "+nodeNumber);

        double result=0;
        try{

            //建立问题
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

            //预先设置约束的条数
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
            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);
//            if(this.isTest) {//测试时输出文件使用
//                GLPK.glp_write_sol(lp, probId + ".sol");
//                glp_cpxcp p = new glp_cpxcp();
//                GLPK.glp_init_cpxcp(p);
//                GLPK.glp_write_lp(lp, p, probId + ".lp");
//            }

            // 获得结果
            if (ret == 0) {
                result=write_lp_solution(lp);
                if(GLPK.glp_get_status(lp)!=GLPKConstants.GLP_OPT)//问题运行成功但是没有达到要求
                    return 0;//0表示没有结果，因为实际情况不会为0
            } else {
                System.out.println("The problem could not be solved");
                return -1;
            }

            // 释放内存
            GLPK.glp_delete_prob(lp);

        }catch (Exception e){
            e.printStackTrace();
            ret=1;
            return -1;
        }
        return result;
    }

    /**
     *
     * 为ILP算法提供G‘，G'是从原始图G变换过来的
     * @param myGraph G
     *
     *
     *
     * @return G'
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

    static double write_lp_solution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;

        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_get_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
//        n = GLPK.glp_get_num_cols(lp);
//        for (i = 1; i <= n; i++) {
//            name = GLPK.glp_get_col_name(lp, i);
//            val = GLPK.glp_get_col_prim(lp, i);
//            System.out.print(name);
//            System.out.print(" = ");
//            System.out.println(val);
//        }
        return val;
    }
    public void  test(){
        String graphData=readJsonGraph("ori_100_1000_4_3.json");
        MyGraph newGraph=parseJsonToGraph(graphData);
        newGraph.startPoint=0;
        newGraph.sinkPoint=10;
        newGraph.maxComVertex=4;
//        GraphRandomGenerator generator=new GraphRandomGenerator();
//        MyGraph myGraph=generator.generateRandomGraph(400,5000);
//        myGraph.startPoint=0;
//        myGraph.sinkPoint=20;
//        myGraph.maxComVertex=10;
//        System.out.println(newGraph.graph.vertexSet().size());
//        System.out.println(newGraph.graph.edgeSet().size());

        ILPGraph lpGraph=getGraphForILP(newGraph);
        System.out.println(lpGraph.graph.vertexSet().size());
        System.out.println(lpGraph.graph.edgeSet().size());
        solveWithGLPK(lpGraph,0);
    }

    public static  void main(String args[]){
        JavaLPAlg javaLPAlg =new JavaLPAlg();
        javaLPAlg.test();
    }
}