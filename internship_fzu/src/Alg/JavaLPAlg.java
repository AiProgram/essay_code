package Alg;

import java.io.*;
import java.util.*;

import MyGraph.MyGraph;
import org.gnu.glpk.*;
import org.jgrapht.graph.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jgrapht.*;

public class JavaLPAlg {

    public int start_point;
    public int dest_point;
    public int bound;
    Map<DefaultWeightedEdge,Integer> costMap=new HashMap<>();

    private String readJsonGraph(String fileName)
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

    private Graph parseJsonToGraph(String jsonStr)
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

        WeightedMultigraph<Integer,DefaultWeightedEdge> graph=new WeightedMultigraph<>(DefaultWeightedEdge.class);
        int nodeNumber=nodesArr.length();
        JSONObject graphObject=jsonObject.getJSONObject("graph");
        start_point=graphObject.getInt("S");
        dest_point=graphObject.getInt("T");
        bound=graphObject.getInt("bound");



        //deal with the nodes
        for(int i=0;i<nodeNumber;i++)
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
            cost=tmpObject.getInt("cost");
            realSrc=source;
            realTar=target;

            DefaultWeightedEdge edge=edgeEdgeFactory.createEdge(realSrc,realTar);
            costMap.put(edge,cost);
            graph.setEdgeWeight(edge,weight);
            graph.addEdge(realSrc,realTar,edge);

            //graph.edges.add(new Edge(realSrc,realTar,weight,cost));
            //The graph with duplicates are too complex, so we add the second duplicator here
            if(cost==1)
            {
                edge=edgeEdgeFactory.createEdge(realSrc,realTar);
                costMap.put(edge,0);
                graph.setEdgeWeight(edge,0);
                graph.addEdge(realSrc,realTar,edge);
            }
        }

        return graph;
    }

    private void solveWithGLPK(Graph graph){
        glp_prob lp;
        glp_smcp parm;
        SWIGTYPE_p_int index;
        SWIGTYPE_p_double val;
        int ret;
        int edgeNumber=graph.edgeSet().size();
        int nodeNumber=graph.vertexSet().size();

        System.out.println(edgeNumber+"  "+nodeNumber);

        try{

            //建立问题
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created");
            GLPK.glp_set_prob_name(lp, "myProblem");

            //定义变量
            GLPK.glp_add_cols(lp, edgeNumber);
            for(int i=1;i<=edgeNumber;i++)
            {
                GLPK.glp_set_col_kind(lp,i,GLPKConstants.GLP_BV);
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

                GLPK.glp_set_row_name(lp,j,"c"+i);

                //int node=graph.nodes[i-1];
                int node=(int)vit.next();
                if(node==dest_point) continue;
                Iterator eit=graph.edgeSet().iterator();
                for(int k=1;k<=edgeNumber;k++)
                {
                    //MyEdge edge=graph.edge.get(k-1);
                    DefaultWeightedEdge edge=(DefaultWeightedEdge) eit.next();
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
                if(node==start_point)
                {
                    b=2;
                }else{
                    b=0;
                }
                GLPK.glp_set_row_bnds(lp,i,GLPKConstants.GLP_FX,b,b);

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
                DefaultWeightedEdge edge=(DefaultWeightedEdge) eit.next();
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
            for(int i=0;i<edgeNumber;i++)
            {
                //Edge edge=graph.edges.get(i);
                DefaultWeightedEdge edge=(DefaultWeightedEdge) eit.next();
                GLPK.glp_set_obj_coef(lp, i, graph.getEdgeWeight(edge));
            }

            //解决模型
            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);

            // 获得结果
            if (ret == 0) {
                write_lp_solution(lp);
            } else {
                System.out.println("The problem could not be solved");
            }

            // 释放内存
            GLPK.glp_delete_prob(lp);

        }catch (Exception e){
            e.printStackTrace();
            ret=1;
        }

    }

    /**
     *
     * 为ILP算法提供G‘，G'是从原始图G变换过来的
     * @param myGraph G
     * @param startPoint 起始点
     * @param sinkPoint  终止点
     * @param max_com_vertex 最大的共同点数量
     * @return G'
     */
    public MyGraph getGraphForILP(MyGraph myGraph, int startPoint, int sinkPoint, int max_com_vertex)
    {
        WeightedMultigraph<Integer,DefaultWeightedEdge> graph=myGraph.graph;
        WeightedMultigraph<Integer,DefaultWeightedEdge> lpGraph=new WeightedMultigraph<>(DefaultWeightedEdge.class);
        lpGraph.addVertex(startPoint);
        lpGraph.addVertex(sinkPoint);
        //我们认为点的id应该在0和nodeNum-1之间
        int nodeNum=graph.vertexSet().size();
        Iterator vit=graph.vertexSet().iterator();
        while(vit.hasNext()){
            int node=(Integer)vit.next();
            if(node==startPoint||node==sinkPoint) continue;
            else{
                lpGraph.addVertex(node);
                lpGraph.addVertex(node+nodeNum);
            }
        }

        //在G中进入v的边现在G’中进入v1，而离开v的边现在G‘中离开v2
        Iterator eit=graph.edgeSet().iterator();
        while(eit.hasNext()){
            DefaultWeightedEdge edge=(DefaultWeightedEdge)eit.next();
            double w=graph.getEdgeWeight(edge);
            int u=graph.getEdgeSource(edge);
            int v=graph.getEdgeTarget(edge);
            int v1=0;
            int u2=0;

            if(v==startPoint||v==sinkPoint) v1=v;
            else v1=v;
            if(u==startPoint||u==sinkPoint) u2=u;
            else u2=u+nodeNum;

            DefaultWeightedEdge newEdge=lpGraph.getEdgeFactory().createEdge(u2,v1);
            lpGraph.addEdge(u2,v1,newEdge);
            lpGraph.setEdgeWeight(newEdge,w);
            myGraph.costMap.put(newEdge,0);//这里暂时使用全局变量储存cost，因为这个库不允许单独添加属性，后面可以改进
        }

        //在G’中加入边(v1,v2)并且它的cost是1而weight是0
        vit=graph.vertexSet().iterator();
        while(vit.hasNext()){
            int node=(int)vit.next();
            int v1=node;
            int v2=node+nodeNum;
            if(node==startPoint||node==sinkPoint) continue;
            DefaultWeightedEdge newEdge=lpGraph.getEdgeFactory().createEdge(v1,v2);
            lpGraph.addEdge(v1,v2,newEdge);
            lpGraph.setEdgeWeight(newEdge,0);
            myGraph.costMap.put(newEdge,1);
        }

        myGraph.graph=lpGraph;
        return myGraph;
    }

    static void write_lp_solution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;

        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_get_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        n = GLPK.glp_get_num_cols(lp);
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val = GLPK.glp_get_col_prim(lp, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }
    }
    public void  test(){
        String graphData=readJsonGraph("lp_500_25000_10_2.json");
        Graph graph=parseJsonToGraph(graphData);
        solveWithGLPK(graph);
    }

    public static  void main(String args[]){
        JavaLPAlg javaLPAlg =new JavaLPAlg();
        javaLPAlg.test();
    }
}
