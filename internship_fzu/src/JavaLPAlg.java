import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.gnu.glpk.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class JavaLPAlg {
    public class Edge
    {
        Edge()
        {

        }
        Edge(int source,int target,int weight,int cost)
        {
            this.source=source;
            this.target=target;
            this.weight=weight;
            this.cost=cost;
        }
        public int source;
        public int target;
        public int weight;
        public int cost;
    }
    public class Graph
    {
        int nodeNumber;
        int edgeNumber;
        int nodes[];
        int start_point;
        int dest_point;
        int bound;
        List<Edge> edges;
    }

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


        Graph graph=new Graph();
        graph.nodeNumber=nodesArr.length();
        JSONObject graphObject=jsonObject.getJSONObject("graph");
        graph.start_point=graphObject.getInt("S");
        graph.dest_point=graphObject.getInt("T");
        graph.bound=graphObject.getInt("bound");



        //deal with the nodes
        graph.nodes=new int[graph.nodeNumber];
        for(int i=0;i<graph.nodeNumber;i++)
        {
            JSONObject tmpObject=nodesArr.getJSONObject(i);
            id=tmpObject.getInt("id");
            graph.nodes[i]=id;
        }


        //deal with the edges
        graph.edges=new ArrayList<>();
        for(int i=0;i<edgesArr.length();i++)
        {
            JSONObject tmpObject=edgesArr.getJSONObject(i);
            source=tmpObject.getInt("source");
            target=tmpObject.getInt("target");
            weight=tmpObject.getInt("weight");
            cost=tmpObject.getInt("cost");
            realSrc=source;
            realTar=target;
            graph.edges.add(new Edge(realSrc,realTar,weight,cost));
            //The graph with duplicates are too complex, so we add the second duplicator here
            if(cost==1)
            {
                graph.edges.add(new Edge(realSrc,realTar,0,0));
            }
        }

        graph.edgeNumber=graph.edges.size();
        return graph;
    }

    private void solveWithGLPK(Graph graph){
        glp_prob lp;
        glp_smcp parm;
        SWIGTYPE_p_int index;
        SWIGTYPE_p_double val;
        int ret;

        try{

            //建立问题
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created");
            GLPK.glp_set_prob_name(lp, "myProblem");

            //定义变量
            GLPK.glp_add_cols(lp, graph.edgeNumber);
            for(int i=1;i<=graph.edgeNumber;i++)
            {
                GLPK.glp_set_col_kind(lp,i,GLPKConstants.GLP_BV);
            }

            //设置约束

            //申请内存
            index=GLPK.new_intArray(graph.edgeNumber);
            val=GLPK.new_doubleArray(graph.edgeNumber);

            //预先设置约束的条数
            GLPK.glp_add_rows(lp, graph.nodeNumber);

            //具体设置约束
            for(int i=1,j=1;i<=graph.nodeNumber;i++)
            {
                int a;
                int b;

                GLPK.glp_set_row_name(lp,j,"c"+i);

                int node=graph.nodes[i-1];
                if(node==graph.dest_point) continue;
                for(int k=1;k<=graph.edgeNumber;k++)
                {
                    Edge edge=graph.edges.get(k-1);
                    if(edge.source==node)
                    {
                        a=1;
                    }else if(edge.target==node) {
                        a=-1;
                    }else{
                        a=0;
                    }
                    GLPK.intArray_setitem(index,k,k);
                    GLPK.doubleArray_setitem(val,k,a);
                }
                if(node==graph.start_point)
                {
                    b=2;
                }else{
                    b=0;
                }
                GLPK.glp_set_row_bnds(lp,i,GLPKConstants.GLP_FX,b,b);

                GLPK.glp_set_mat_row(lp, j, graph.edgeNumber, index, val);
                j++;
            }

            GLPK.glp_set_row_name(lp,graph.nodeNumber,"c"+0);
            GLPK.glp_set_row_bnds(lp,graph.nodeNumber,GLPKConstants.GLP_UP,0,graph.bound);
            for(int k=1;k<=graph.edgeNumber;k++)
            {
                GLPK.intArray_setitem(index,k,k);
                Edge edge=graph.edges.get(k-1);
                GLPK.doubleArray_setitem(val,k,edge.cost);
            }
            GLPK.glp_set_mat_row(lp,graph.nodeNumber,graph.edgeNumber,index,val);

            //释放内存
            GLPK.delete_intArray(index);
            GLPK.delete_doubleArray(val);

            //设置最值式
            GLPK.glp_set_obj_name(lp, "result");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);

            for(int i=0;i<graph.edgeNumber;i++)
            {
                Edge edge=graph.edges.get(i);
                GLPK.glp_set_obj_coef(lp, i, edge.weight);
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
