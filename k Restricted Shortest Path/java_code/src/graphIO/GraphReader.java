package graphIO;

import myGraph.MyGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Random;

public class GraphReader {
    final String graphDataFolder="./graph_data/";
    public String readJsonStr(String fileName){
        File file=new File(fileName);
        if(!file.exists()){
            System.err.println("图文件： "+fileName+" 不存在");
            return null;
        }else{
            try {
                FileInputStream inputStream=new FileInputStream(file);
                Long fileLength=file.length();
                byte bufffer[]=new byte[fileLength.intValue()];
                inputStream.read(bufffer);
                return new String(bufffer);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
    public MyGraph readJsonGraph(String fileName){
        String jsonStr=readJsonStr(fileName);
        if(jsonStr==null)return null;
        else{
            JSONObject graphObj=new JSONObject(jsonStr);
            JSONArray nodeArr=graphObj.getJSONArray("nodes");
            JSONArray linkArr=graphObj.getJSONArray("links");

            MyGraph myGraph=new MyGraph();
            for(int i=0;i<nodeArr.length();i++)
            {
                JSONObject nodeObj=nodeArr.getJSONObject(i);
                int node=(int)nodeObj.getInt("id");
                myGraph.graph.addVertex(node);
            }
            for(int i=0;i<linkArr.length();i++)
            {
                JSONObject linkObj=linkArr.getJSONObject(i);
                int source=linkObj.getInt("source");
                int target=linkObj.getInt("target");
                int cost=linkObj.getInt("cost");
                int delay=linkObj.getInt("delay");
                myGraph.addNewEdge(source,target,cost,delay);
            }
            return myGraph;
        }
    }
    /**
     * 读取SNAP图数据，并转换成自定义的图
     * @param fileName SNAP图文件名
     * @return 图
     */
    public MyGraph readSnapGraph(String fileName){
        MyGraph myGraph=new MyGraph();
        File snapFile=new File(fileName);
        if(!snapFile.exists()){
            System.err.println("文件"+snapFile+"不存在");
            return null;
        }
        try{
            Random random=new Random();
            String line;
            FileReader reader=new FileReader(snapFile);
            BufferedReader bufferedReader=new BufferedReader(reader);
            while((line=bufferedReader.readLine())!=null)
            {
                if(line.charAt(0)!='#') {
                    String strArr[]=line.split("\\s+");
                    if(strArr.length>=2){
                        int source=Integer.parseInt(strArr[0]);
                        int target=Integer.parseInt(strArr[1]);
                        myGraph.addNewEdgeWithNode(source,target,1,0);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return myGraph;
    }
}
