#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
import  utilities as util
import random
import time
import math
import networkx as nx
import json
def test():#用于测试bug
    graph_file=open("D:\PythonProject\Essays\code\internship_fzu\graph_data\\19.json","r")
    json_graph=json.load(graph_file)
    graph=nx.node_link_graph(json_graph)
    lp_graph=am.get_graph_for_LP(graph,0,10,2)
    util.save_graph_to_json(lp_graph,"lp_test.json")

if __name__=="__main__":
    max_com_vertex=2
    node_number=40
    #SCALE=int(math.sqrt(node_number))
    edge_number=int(math.pow(node_number,2)/10)
    start_point_num=0
    des_point_num=10
    repeate_time=1
    graph_num=5
    #util.generate_graph_group(node_number,edge_number,max_com_vertex,graph_num,start_point_num,des_point_num,repeate_time)
    #util.run_new_alg_group()
    #util.run_mwld_group()
    test()

