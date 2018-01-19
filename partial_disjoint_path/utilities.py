import GraphRandGen as grg
import AlgorithmMain as am
import os
import time
import networkx as nx
from networkx.readwrite import json_graph
import numpy as np
import json
# the following two classes is used to record the data of code's running
graph_data_folder="D:\PythonProject\Essays\code\partial_disjoint_path\java_code\graph_data\\"
class RunCodeRecMain:
    repeate_time=20
    file_number=0
    file_Rec_Arr=[]
    new_run_time=0
    lp_run_time=0
    def to_object(self):
        out={}
        out["repeate_time"]=self.repeate_time
        out["file_number"]=self.file_number
        tmp=[]
        for rec in self.file_Rec_Arr:
            tmp.append(rec.to_object())
        out["file_Rec_Arr"]=tmp
        out["new_run_time"]=self.new_run_time
        out["lp_run_time"]=self.lp_run_time
        return out
class FileRec:
    #without extend name
    origin_graph_file=""
    lp_graph_file=""
    lp_file=""

    node_number=0# in origin graph
    edge_number=0
    max_com_vertex=0
    id_number=0#when there exists file with same node_number and edge_number
    
    new_alg_complete=False
    lp_alg_complete=False

    new_alg_run_time=0
    lp_alg_run_time=0

    def to_object(self):
        out={}
        out["origin_graph_file"]=self.origin_graph_file
        out["lp_graph_file"]=self.lp_graph_file
        out["lp_file"]=self.lp_file
        out["node_number"]=self.node_number
        out["edge_number"]=self.edge_number
        out["max_com_vertex"]=self.max_com_vertex
        out["id_number"]=self.id_number
        out["new_alg_complete"]=self.new_alg_complete
        out["lp_alg_complete"]=self.lp_alg_complete
        out["new_alg_run_time"]=self.new_alg_run_time
        out["lp_alg_run_time"]=self.lp_alg_run_time
        return out

def generate_graph_group(node_number,edge_number,max_com_vertex,graph_num,start_point_num,des_point_num,repeate_time):
    os.chdir(graph_data_folder)
    con_file=open("data.json","w+")
    rec_main=RunCodeRecMain()
    rec_main.file_number=0
    rec_main.repeate_time=repeate_time

    for n in range(graph_num):
        graph=grg.generate_graph_random(node_number,edge_number)
        graph.graph["S"]=start_point_num
        graph.graph["T"]=des_point_num
        LP_graph=am.get_graph_for_LP(graph,start_point_num,des_point_num,max_com_vertex)
        graph_name="ori_"+str(node_number)+"_"+str(edge_number)+"_"+str(max_com_vertex)+"_"+str(n)+".json"
        LP_graph_name="lp_"+str(node_number)+"_"+str(edge_number)+"_"+str(max_com_vertex)+"_"+str(n)+".json"

        file_rec=FileRec()
        file_rec.node_number=node_number
        file_rec.edge_number=edge_number
        file_rec.origin_graph_file=graph_name
        file_rec.lp_graph_file=LP_graph_name
        file_rec.max_com_vertex=max_com_vertex
        file_rec.id_number=n
        file_rec.new_alg_complete=False
        file_rec.lp_alg_complete=False

        rec_main.file_Rec_Arr.append(file_rec)
        save_graph_to_json(graph,graph_name)
        save_graph_to_json(LP_graph,LP_graph_name)
        rec_main.file_number+=1
    json.dump(rec_main.to_object(),con_file)
    con_file.close()

def run_code_group(SCALE):
    os.chdir(graph_data_folder)
    con_file=open("data.json","r")
    rec_object=json.load(con_file)

    repeate_time=20
    file_number=0
    file_Rec_Arr=[]
    new_run_time=0
    lp_run_time=0

    repeate_time=rec_object["repeate_time"]
    file_number=rec_object["file_number"]
    file_Rec_Arr=rec_object["file_Rec_Arr"]
    new_run_time=rec_object["new_run_time"]
    lp_run_time=rec_object["lp_run_time"]

    time_all=0
    for rec_file in file_Rec_Arr:
        if rec_file["new_alg_complete"] is True:
            time_all+=rec_file["new_alg_run_time"]
            continue
        else:
            origin_graph_file=rec_file["origin_graph_file"]
            max_com_vertex=rec_file["max_com_vertex"]
            time_sum=0
            graph_file=open(origin_graph_file,"r")
            graph=json_graph.node_link_graph(json.load(graph_file))
            start_point_num=graph.graph["S"]
            des_point_num=graph.graph["T"]
            for n in range(repeate_time):
                time_start=time.time()
                try:
                    path_P,residual_graph=am.get_residual_graph(start_point_num,des_point_num,graph=graph)
                    dist,path_Q=am.RSP_with_recursion(residual_graph,start_point_num,des_point_num,max_com_vertex*SCALE)
                except:
                    pass
                time_end=time.time()
                time_sum+=(time_end-time_start)
            time_sum=time_sum/repeate_time
            rec_file["new_alg_run_time"]=time_sum/SCALE
            rec_file["new_alg_complete"]=True
            time_all+=time_sum
            graph_file.close()

            print("file %s complete"%(rec_file["origin_graph_file"]))

    time_all=time_all/file_number
    rec_object["new_run_time"]=time_all
    con_file.close()
    con_file=open("data.json","w+")
    json.dump(rec_object,con_file)
    con_file.close()


def verify_RSP_result(graph,path,max_com_vertex):
    success=True
    cur_com_vertex=0
    node_number=graph.number_of_nodes()
    for i in range(len(path)-1):
        if graph.successors(path[i]).count(path[i+1])==0:
            print("edge not existed")
            success=False
            break
        cur_com_vertex+=graph[path[i]][path[i+1]]["cost"]
    if cur_com_vertex>max_com_vertex:
        success=False
    return success

def get_SP_weight(graph,path):
    total=0
    for index in range(len(path)-1):
        total+=graph[path[index]][path[index+1]]["weight"]
    return total
def save_graph_to_json(graph,graph_file_name):
    """convert graph to json format to store it"""
    file=open(graph_file_name,"w+")
    graph_data=json_graph.node_link_data(graph)
    json_data=json.dumps(graph_data)
    file.write(json_data)
    file.close()