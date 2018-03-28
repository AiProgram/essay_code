#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
import  utilities as util
import random
import time
import math
if __name__=="__main__":
    max_com_vertex=4
    node_number=40
    #SCALE=int(math.sqrt(node_number))
    edge_number=int(math.pow(node_number,2)/10)
    start_point_num=0
    des_point_num=20
    repeate_time=1
    graph_num=100
    #util.generate_graph_group(node_number,edge_number,max_com_vertex,graph_num,start_point_num,des_point_num,repeate_time)
    #util.run_new_alg_group()
    util.run_mwld_group()