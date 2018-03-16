import GraphRandGen as grg
import AlgorithmMain as am
import os
import time
import networkx as nx
from networkx.readwrite import json_graph
import numpy as np
import json
import csv
import re
#these folder paths is relative to our folder that stores this python file
graph_data_folder="\\java_code\\graph_data\\"
sol_files_folder="\\java_code\\solFiles\\"#folder to store the result file (which name is like xxx.sol) of glpk
# the following two classes is used to record the data of code's running
class RunCodeRecMain:
    """the class used to record the main data of files in the folder"""
    repeate_time=20
    file_number=0
    file_Rec_Arr=[]
    new_run_time=0
    lp_run_time=0
    mwld_run_time=0
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
        out["mwld_run_time"]=self.mwld_run_time

        return out
class FileRec:
    """The class used to record the results of algorithms running a single graph"""
    #with extend name
    origin_graph_file=""
    lp_graph_file=""
    lp_file=""

    node_number=0# in origin graph
    edge_number=0
    max_com_vertex=0
    id_number=0#when there exists file with same node_number and edge_number
    
    #mark that if we hava completed the alg on thie graph
    new_alg_complete=False
    lp_alg_complete=False
    mwld_alg_complete=False

    #running time of these three algorithms
    new_alg_run_time=0
    lp_alg_run_time=0
    mwld_alg_run_time=0

    #result refers to the sum of the weight of pair of shortest path
    #thie mainly used to compare if the result is the same for that compare two path is 
    #a little bit inefficent
    new_alg_result=0
    lp_alg_result=0
    mwld_alg_result=0

    best_mcv=1

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
        out["mwld_alg_complete"]=self.mwld_alg_complete
        out["new_alg_run_time"]=self.new_alg_run_time
        out["lp_alg_run_time"]=self.lp_alg_run_time
        out["mwld_alg_run_time"]=self.mwld_alg_run_time
        out["new_alg_result"]=self.new_alg_result
        out["lp_alg_result"]=self.lp_alg_result
        out["mwld_alg_result"]=self.mwld_alg_result
        out["best_mcv"]=self.best_mcv
        return out

def generate_graph_group(node_number,edge_number,max_com_vertex,graph_num,start_point_num,des_point_num,repeate_time):
    """generate random graphs in batch and restore them into json file"""
    os.chdir(os.path.dirname(__file__)+graph_data_folder)
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

def run_new_alg_group():
    """run new algorithm on graphs in the big folder which has many samll folders"""
    os.chdir(os.path.dirname(__file__)+graph_data_folder)
    folders=os.listdir(os.path.dirname(__file__)+graph_data_folder)
    for folder in folders:
        if os.path.isdir(folder):
            run_new_alg_dir(folder)

def run_new_alg_dir(cur_folder_path,SCALE=1):
    """run new algorithm on graphs in the folder in batch"""
    os.chdir(cur_folder_path)
    con_file=open("data.json","r")
    rec_object=json.load(con_file)
    con_file.close()

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
        else:
            origin_graph_file=rec_file["origin_graph_file"]
            max_com_vertex=rec_file["max_com_vertex"]
            time_sum=0
            graph_file=open(origin_graph_file,"r")
            graph=json_graph.node_link_graph(json.load(graph_file))
            graph_file.close()
            start_point_num=graph.graph["S"]
            des_point_num=graph.graph["T"]
            for n in range(repeate_time):
                time_start=time.time()
                try:
                    path_P,residual_graph=am.get_residual_graph(start_point_num,des_point_num,graph=graph)
                    #dist,path_Q=am.RSP_with_recursion(residual_graph,start_point_num,des_point_num,max_com_vertex*SCALE)
                    dist,pathQ=am.constrained_shortest_path(start_point_num,des_point_num,residual_graph,max_com_vertex)
                    rec_file["new_alg_result"]=dist+get_SP_weight(graph,path_P)
                except BaseException as e:
                    print(format(e))
                time_end=time.time()
                time_sum+=(time_end-time_start)
            time_sum=time_sum/repeate_time
            rec_file["new_alg_run_time"]=time_sum
            rec_file["new_alg_complete"]=True
            time_all+=time_sum

            print("file %s complete"%(rec_file["origin_graph_file"]))

    time_all=time_all/file_number
    rec_object["new_run_time"]=time_all
    con_file=open("data.json","w+")
    json.dump(rec_object,con_file)
    con_file.close()


def run_mwld_group():
    """run mwld algorithm on graphs in the folder in batch"""
    os.chdir(os.path.dirname(__file__)+graph_data_folder)
    con_file=open("data.json","r")
    rec_object=json.load(con_file)
    con_file.close()

    repeate_time=20
    file_number=0
    file_Rec_Arr=[]
    mwld_run_time=0

    repeate_time=rec_object["repeate_time"]
    file_number=rec_object["file_number"]
    file_Rec_Arr=rec_object["file_Rec_Arr"]
    mwld_run_time=rec_object["mwld_run_time"]

    time_all=0
    for rec_file in file_Rec_Arr:
        if rec_file["mwld_alg_complete"] is True:
            time_all+=rec_file["mwld_alg_run_time"]
        else:
            origin_graph_file=rec_file["origin_graph_file"]
            max_com_vertex=rec_file["max_com_vertex"]
            time_sum=0
            graph_file=open(origin_graph_file,"r")
            graph=json_graph.node_link_graph(json.load(graph_file))
            graph_file.close()
            start_point_num=graph.graph["S"]
            des_point_num=graph.graph["T"]
            for n in range(repeate_time):
                time_start=time.time()
                try:
                    weight_sum,path=am.mwld_alg(graph,start_point_num,des_point_num,max_com_vertex)
                    rec_file["mwld_alg_result"]=weight_sum
                except BaseException as e:
                    print(format(e))
                time_end=time.time()
                time_sum+=(time_end-time_start)
            time_sum=time_sum/repeate_time
            rec_file["mwld_alg_run_time"]=time_sum
            rec_file["mwld_alg_complete"]=True
            time_all+=time_sum

            print("file %s complete"%(rec_file["origin_graph_file"]))

    time_all=time_all/file_number
    rec_object["mwld_run_time"]=time_all
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
    """the path is a validate path in the graph, and this returns its sum of weight"""
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

def collect_data_csv():
    os.chdir(os.path.dirname(__file__) + graph_data_folder)
    folders=os.listdir(os.path.dirname(__file__) + graph_data_folder)
    for folder in folders:
        if os.path.isdir(folder):
            collect_data_folder(folder)
def collect_data_folder(cur_csv_folder):
    """collect the information of data.json and write them into a csv file for futuer analysis"""
    os.chdir(cur_csv_folder)
    con_file=open("data.json","r")
    rec_object=json.load(con_file)
    con_file.close()
    
    csv_file_name="data.csv"
    with open(csv_file_name,"w+",newline='') as csv_file:
        csv_header=["id","new_alg_run_time","new_alg_result","mwld_alg_run_time","mwld_alg_result","lp_alg_run_time","lp_alg_result","best_mcv"]
        writer = csv.DictWriter(csv_file, fieldnames=csv_header)
        file_Rec_Arr=rec_object["file_Rec_Arr"]
        data=[]
        for file_rec in file_Rec_Arr:
            rec={}
            rec["id"]=file_rec["id_number"]
            rec["new_alg_run_time"]=file_rec["new_alg_run_time"]
            rec["new_alg_result"]=file_rec["new_alg_result"]
            rec["mwld_alg_run_time"]=file_rec["mwld_alg_run_time"]
            rec["mwld_alg_result"]=file_rec["mwld_alg_result"]
            rec["lp_alg_run_time"]=file_rec["lp_alg_run_time"]
            rec["best_mcv"]=file_rec["best_mcv"]

            sol_file_name=file_rec["lp_file"].split(".")[0]+".sol"
            #rec["lp_alg_result"]=0
            rec["lp_alg_result"]=get_result_from_sol(sol_file_name)
            file_rec["lp_alg_result"]=get_result_from_sol(sol_file_name)
            if rec["new_alg_result"]>0 and rec["lp_alg_result"]>0 and rec["new_alg_result"]!=rec["lp_alg_result"]:
                continue
            data.append(rec)
        writer.writeheader()
        writer.writerows(data)
    con_file=open("data.json","w+")
    json.dump(rec_object,con_file)
    con_file.close()

def get_result_from_sol(sol_file_name):
    """the result of ILP algorithm will be in file like xxxx.sol and this function extract result from it """
    full_path=os.path.dirname(__file__)+sol_files_folder+sol_file_name
    if os.path.exists(full_path):#in case we use this function when we have not run the lp algorithm
        sol_file=open(os.path.dirname(__file__)+sol_files_folder+sol_file_name,"r")
        line_num=1
        for line in sol_file:
            if line_num==6:
                line=line.strip()
                result=re.sub("\D", "", line)
                result=int(result)
                break
            else:
                line_num+=1
    else:
        return 0
    return result

