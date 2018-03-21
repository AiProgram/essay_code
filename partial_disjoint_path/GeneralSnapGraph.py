import networkx as nx
import csv
import os
import time
import random
import utilities as util
import AlgorithmMain as am
#this folder path is relative to our folder that stores this python file
snap_graph_folder="\\snap_graph\\"

def read_txt_graph(txt_file_name,is_direct_graph=True):
    """read graph in txt files which come from snap knonect"""
    full_path=os.path.dirname(__file__)+snap_graph_folder+txt_file_name
    txt_file=open(full_path,"r")
    graph=nx.DiGraph()
    node_set=set()
    for line in txt_file:
        tmp=line.strip()
        if len(tmp)>0 and tmp[0]!='#':
            part=line.split()
            u=int(part[0])
            v=int(part[1])
            if u not in node_set:
                graph.add_node(u)
                node_set.add(u)
            if v not in node_set:
                graph.add_node(v)
                node_set.add(v)
            graph.add_edge(u,v,weight=1)
            if not is_direct_graph:
                graph.add_edge(v,u,weight=1)
    return graph

def run_new_alg_repeat(txt_file_name,max_com_vertex=1,repeat_time=1,is_direct_graph=True):
    """run our new algorithm on one graph in snap graph folder for repeat_time times"""
    csv_header=["id","start_point","dest_point","result","time"]
    csv_data=[]

    os.chdir(os.path.dirname(__file__)+snap_graph_folder)
    graph=read_txt_graph(txt_file_name,is_direct_graph)
    print(txt_file_name)
    node_num=graph.number_of_nodes()

    i=0
    old_i=0
    while i<repeat_time:
        start_point_num=random.randint(0,node_num)
        des_point_num=random.randint(0,node_num)
        try:
            start_time=time.time()
            path_P,residual_graph=am.get_residual_graph(start_point_num,des_point_num,graph=graph)
            dist,pathQ=am.constrained_shortest_path(start_point_num,des_point_num,residual_graph,max_com_vertex)
            result=dist+util.get_SP_weight(graph,path_P)
            end_time=time.time()
            total_time=end_time-start_time

            #record the running infomation into a csv file
            rec={}
            rec["id"]=i
            rec["start_point"]=start_point_num
            rec["dest_point"]=des_point_num
            rec["result"]=result
            rec["time"]=total_time
            csv_data.append(rec)

            #the random generated s and t point may be unreachable 
            #and this case is useless when measuring time
            i+=1

            #this is to show how much times has completed
            if i%5 is 0 :
                print(str(i)+" times complete")
        except BaseException  as e:
            #print("there is an error:\n"+format(e))
            pass

    csv_file_name=txt_file_name.split(".")[0]+".csv"
    rec_info_to_csv(csv_file_name,csv_header,csv_data)

def rec_info_to_csv(csv_file_name,csv_header,csv_data):
    """record some information into a csv file"""
    with open(csv_file_name,"w+",newline='') as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=csv_header)
        writer.writeheader()
        writer.writerows(csv_data)

def run_new_alg_group(max_com_vertex=1,repeat_time=1,is_direct_graph=True):
    """run our new algorithm on every graph in snap graph folder"""
    os.chdir(os.path.dirname(__file__)+snap_graph_folder)
    files=os.listdir()
    for file in files:
        if os.path.isfile(file):
            if os.path.splitext(file)[1]==".txt":
                run_new_alg_repeat(file,max_com_vertex,repeat_time,is_direct_graph)
