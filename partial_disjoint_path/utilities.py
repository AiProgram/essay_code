import GraphRandGen as grg
import networkx as nx
from networkx.readwrite import json_graph
import numpy as np
import json
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

def save_graph_to_json(graph,graph_file_name):
    """convert graph to json format to store it"""
    file=open(graph_file_name,"w+")
    graph_data=json_graph.node_link_data(graph)
    json_data=json.dumps(graph_data)
    file.write(json_data)
    file.close()