import Alg.ILP.JavaLPAlg;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GraphLPAlg {
    private class Edge
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
    private class Graph
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

    public void getAlgLPFile(Graph graph,String lpFileName)
    {
        float c[];

        float a3[][];
        float b3[];
        float a1[][];
        float b1[];

        float d1[];
        float d2[];

        LPGenerator lpGenerator=new LPGenerator();

        c=new float[graph.edgeNumber];
        for(int i=0;i<c.length;i++)
        {
            Edge edge=graph.edges.get(i);
            c[i]=edge.weight;
        }
        lpGenerator.setExpression(LPGenerator.ExpType.Minmize,c);

        a3=new float[graph.nodeNumber-1][graph.edgeNumber];
        b3=new float[graph.nodeNumber-1];
        for(int i=0,j=0;i<graph.nodeNumber;i++)
        {
            int node=graph.nodes[i];
            if(node==graph.dest_point) continue;
            for(int k=0;k<graph.edgeNumber;k++)
            {
                Edge edge=graph.edges.get(k);
                if(edge.source==node)
                {
                    a3[j][k]=1;
                }else if(edge.target==node) {
                    a3[j][k]=-1;
                }else{
                    a3[j][k]=0;
                }
            }
            if(node==graph.start_point)
            {
                b3[j]=2;
            }else{
                b3[j]=0;
            }
            j++;
        }
        a1=new float[1][graph.edgeNumber];
        b1=new float[1];
        for(int i=0;i<graph.edgeNumber;i++)
        {
            Edge edge=graph.edges.get(i);
            a1[0][i]=edge.cost;
        }
        b1[0]=graph.bound;

        lpGenerator.setConstraints(a1,null,a3,b1,null,b3);
        lpGenerator.setVariable(LPGenerator.VarType.INT,graph.edgeNumber,null,null);
        lpGenerator.generateLPFile(lpFileName);
    }

    public void getLPGroup()
    {
        String graphDataFolder="./graph_data/";
        try
        {
            File dataFile=new File(graphDataFolder+"data.json");
            FileReader dataReader=new FileReader(dataFile);
            BufferedReader bufDataReader=new BufferedReader(dataReader);
            StringBuilder dataStr=new StringBuilder();
            String tmp;
            while((tmp=bufDataReader.readLine())!=null)
            {
                dataStr.append(tmp);
            }
            JSONObject recMain=new JSONObject(dataStr.toString());

            int fileNumber=recMain.getInt("file_number");
            int repeateTime=recMain.getInt("repeate_time");

            JSONArray fileRecArr=recMain.getJSONArray("file_Rec_Arr");
            for(int i=0;i<fileNumber;i++)
            {
                JSONObject fileRec=fileRecArr.getJSONObject(i);
                String lpGraphFile=fileRec.getString("lp_graph_file");
                String graphData=readJsonGraph(graphDataFolder+lpGraphFile);
                Graph graph=parseJsonToGraph(graphData);
                String lpFileName=lpGraphFile.split("\\.")[0];
                getAlgLPFile(graph,lpFileName);
                fileRec.put("lp_file",lpFileName+".lp");
            }
            dataReader.close();

            FileWriter dataWriter=new FileWriter(dataFile);
            dataWriter.write(recMain.toString());
            dataWriter.flush();
            dataWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void runCodeGroup()
    {
        String curPath=System.getProperty("user.dir");
        String graphDataFolder=curPath+"\\graph_data\\";
        String outPutFolder=curPath+"\\solFiles\\";
        String lpFolder=curPath+"\\lpFiles\\";
        String glpkPath="E:\\ProgramFiles\\glpk-4.63\\w64\\glpsol";
        try {
            File dataFile = new File(graphDataFolder + "data.json");
            FileReader dataReader = new FileReader(dataFile);
            BufferedReader bufDataReader = new BufferedReader(dataReader);
            StringBuilder dataStr = new StringBuilder();
            String tmp;
            while ((tmp = bufDataReader.readLine()) != null) {
                dataStr.append(tmp);
            }
            JSONObject recMain = new JSONObject(dataStr.toString());
            JSONArray  fileRecArr=recMain.getJSONArray("file_Rec_Arr");

            int repeateTime=recMain.getInt("repeate_time");
            for(int i=0;i<fileRecArr.length();i++)
            {
                JSONObject fileRec=fileRecArr.getJSONObject(i);
                String lpFile=fileRec.getString("lp_file");

                //System.out.println(lpFile);

                String solFile=lpFile.split("\\.")[0]+".sol";
                lpFile=lpFolder+lpFile;
                solFile=outPutFolder+solFile;

                String cmd=glpkPath+" --cpxlp "+lpFile+" -o "+solFile;
                long timeAll=0;
                long start=System.currentTimeMillis();
                for(int j=0;j<repeateTime;j++)
                {
                    //System.out.println("repeate: "+j);
                    exeCmd(cmd);
                }
                long end=System.currentTimeMillis();
                timeAll=end-start;
                System.out.println(start+" "+end+" "+timeAll);
                Double time=(timeAll/1000.0)/repeateTime;
                fileRec.put("lp_alg_run_time",time);
                fileRec.put("lp_alg_complete",true);
            }
            dataReader.close();

            FileWriter dataWriter=new FileWriter(dataFile);
            dataWriter.write(recMain.toString());
            dataWriter.flush();
            dataWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public  void test()
    {
        getLPGroup();
        runCodeGroup();
    }

    public static  void main(String args[]){
        JavaLPAlg javaLPAlg =new JavaLPAlg();
        javaLPAlg.test();
    }

    public static String exeCmd(String commandStr) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(commandStr);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            //System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
