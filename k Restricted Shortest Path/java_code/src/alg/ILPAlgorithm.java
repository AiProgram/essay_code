package alg;

import graphIO.GraphRandGen;
import myGraph.MyGraph;
import org.gnu.glpk.*;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ILPAlgorithm {
    public KRSPResult solveWithGLPK(MyGraph myGraph, int startPoint, int desPoint, int pathNum, int maxDelay)
    {
        int ret;
        KRSPResult result;
        glp_prob lp;
        glp_iocp parm;
        SWIGTYPE_p_int index;
        SWIGTYPE_p_double val;
        DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge> graph=myGraph.graph;
        Map<DefaultWeightedEdge,Integer> costMap=myGraph.costMap;
        Map<DefaultWeightedEdge,Integer> delayMap=myGraph.delayMap;
        List<Integer> vertexList=new ArrayList<>(graph.vertexSet());//确保每次访问点和边都是一样的顺序
        List<DefaultWeightedEdge> edgeList=new ArrayList<>(graph.edgeSet());


        int edgeNum=graph.edgeSet().size();
        int nodeNum=graph.vertexSet().size();
        try{
            //建立问题
            lp=GLPK.glp_create_prob();
            GLPK.glp_set_prob_name(lp,"kRSP");
            //定义变量
            GLPK.glp_add_cols(lp,edgeNum);
            for(int i=1;i<=edgeNum;i++)
            {
                GLPK.glp_set_col_kind(lp,i,GLPKConstants.GLP_IV);//设置变量种类,整数
                GLPK.glp_set_col_bnds(lp,i,GLPKConstants.GLP_DB,0,1);//设置变量取值范围,0-1之间
                GLPK.glp_set_col_name(lp,i,"x"+i);//设置变量名字
            }

            //设置约束

            //申请内存
            index=GLPK.new_intArray(edgeNum);
            val=GLPK.new_doubleArray(edgeNum);

            //预先设置约束的条数
            GLPK.glp_add_rows(lp,nodeNum);
            //具体设置约束
            for(int i=1,j=1;i<=nodeNum;i++)
            {
                int a;
                int b;
                GLPK.glp_set_row_name(lp,j,"c"+j);
                int node=vertexList.get(i-1);
                if(node==desPoint){
                    continue;
                }else{
                    for(int k=1;k<=edgeNum;k++)
                    {
                        DefaultWeightedEdge edge=edgeList.get(k-1);
                        int src=graph.getEdgeSource(edge);
                        int tar=graph.getEdgeTarget(edge);
                        if(src==node)//node的出边
                        {
                            a=1;
                        }else if(tar==node){
                            a=-1;
                        }else{
                            a=0;
                        }
                        GLPK.intArray_setitem(index,k,k);
                        GLPK.doubleArray_setitem(val,k,a);
                    }
                    if(node==startPoint)
                    {
                        b=pathNum;
                    }else{
                        b=0;
                    }
                    GLPK.glp_set_row_bnds(lp,j,GLPKConstants.GLP_FX,b,b);
                    GLPK.glp_set_mat_row(lp,j,edgeNum,index,val);
                    j++;
                }
            }
            GLPK.glp_set_row_name(lp,nodeNum,"c"+0);//设置最后一条关于最大延迟的约束
            GLPK.glp_set_row_bnds(lp,nodeNum,GLPKConstants.GLP_UP,0,maxDelay);
            for(int k=1;k<=edgeNum;k++)
            {
                GLPK.intArray_setitem(index,k,k);
                DefaultWeightedEdge edge=edgeList.get(k-1);
                GLPK.doubleArray_setitem(val,k,delayMap.get(edge));
            }
            GLPK.glp_set_mat_row(lp,nodeNum,edgeNum,index,val);

            //释放内存
            GLPK.delete_intArray(index);
            GLPK.delete_doubleArray(val);

            //设置最值式
            GLPK.glp_set_obj_name(lp,"result");
            GLPK.glp_set_obj_dir(lp,GLPKConstants.GLP_MIN);
            for(int i=1;i<=edgeNum;i++)
            {
                DefaultWeightedEdge edge=edgeList.get(i-1);
                GLPK.glp_set_obj_coef(lp,i,costMap.get(edge));
            }

            //解决模型
            parm=new glp_iocp();
            GLPK.glp_init_iocp(parm);
            parm.setPresolve(GLPKConstants.GLP_ON);
            ret=GLPK.glp_intopt(lp, parm);//这里必须使用MIP也就是混合整数线性规划来求解，否则结果会有小数
            //ret=GLPK.glp_exact(lp,parm);
            if(false) {//测试时输出文件使用
                GLPK.glp_write_mip(lp, "sol.sol");
                glp_cpxcp p = new glp_cpxcp();
                GLPK.glp_init_cpxcp(p);
                GLPK.glp_write_lp(lp, p, "lp.lp");
//                int r=GLPK.glp_read_sol(lp,"D:\\PythonProject\\Essays\\code\\k Restricted Shortest Path\\java_code\\sol.sol");
//                System.out.println("r:  "+r);
            }

            // 获得结果
            if (ret == 0) {
                result=write_lp_solution(lp,myGraph,edgeList,startPoint,desPoint);
                if(GLPK.glp_mip_status(lp)!=GLPKConstants.GLP_OPT)//问题运行成功但是没有达到要求
                    return null;//0表示没有结果，因为实际情况不会为0
            } else {
                System.out.println("The problem could not be solved");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            ret=-1;
            return null;
        }
        GLPK.glp_delete_prob(lp);
        return result;

    }

    static KRSPResult write_lp_solution(glp_prob lp, MyGraph graph, List<DefaultWeightedEdge>graphEdgeList, int startPoint, int desPoint) {//获取lp最后的结果
        int i;
        int n;
        String name;
        double val;
        KRSPResult result=new KRSPResult();
        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_mip_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        result.costSum=val;

        n = GLPK.glp_get_num_rows(lp);//用于提取delay总和
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_row_name(lp, i);
            //System.out.println(name);
            if(name.equals("c0")) {//找到delay统计所在的约束
                val = GLPK.glp_mip_row_val(lp, i);
                break;
            }else
                continue;
        }
        result.delaySum=val;

        n = GLPK.glp_get_num_cols(lp);//用于提取delay总和
        List<DefaultWeightedEdge>edgeList=new ArrayList<>();
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val = GLPK.glp_mip_col_val(lp, i);
            //System.out.println(name+"  "+val);
            int edgeIndex=Integer.parseInt(name.substring(1));//提取边的编号,注意这里同样从1开始
            if(val>0) edgeList.add(graphEdgeList.get(i-1));
        }
        result.paths=getPaths(graph,edgeList,startPoint,desPoint);
        return result;
    }

    static   List<List<Integer>> getPaths(MyGraph graph,List<DefaultWeightedEdge> edgeList,int startPoint,int desPoint){
        MyGraph myGraph=new MyGraph();
        Iterator iterator=edgeList.iterator();
        while(iterator.hasNext())
        {
            DefaultWeightedEdge edge=(DefaultWeightedEdge)iterator.next();
            int source=graph.graph.getEdgeSource(edge);
            int target=graph.graph.getEdgeTarget(edge);
            myGraph.graph.addVertex(source);
            myGraph.graph.addVertex(target);
            myGraph.addNewEdge(source,target,0,0);
        }
        //一次输出的simple path可能会有边相交，所以生成一条以后删除这条边继续生成
        List<List<Integer>>tmp=new ArrayList<>();
        while(myGraph.graph.edgeSet().size()>0){
            AllDirectedPaths<Integer,DefaultWeightedEdge> allDirectedPaths=new AllDirectedPaths<>(myGraph.graph);
            List<GraphPath<Integer,DefaultWeightedEdge>> oriPaths=allDirectedPaths.getAllPaths(startPoint,desPoint,true,null);
            GraphPath<Integer,DefaultWeightedEdge> oriPath=oriPaths.get(0);
            List<Integer>tmpPath=oriPath.getVertexList();
            tmp.add(tmpPath);
            System.out.println(tmpPath);
            for(int i=0;i<tmpPath.size()-1;i++)
            {
                int u=tmpPath.get(i);
                int v=tmpPath.get(i+1);
                myGraph.removeAllEdges(u,v);
            }
        }
        return tmp;
    }

    public static void main(String args[]){
        GraphRandGen graphRandGen=new GraphRandGen();
        MyGraph myGrap=graphRandGen.generateRandomGraph(60,800);
        ILPAlgorithm algorithm=new ILPAlgorithm();
        KRSPResult result=algorithm.solveWithGLPK(myGrap,1,20,4,32);
        System.out.println(result.costSum+"   "+result.delaySum);
        System.out.println(result.paths);
    }
}
