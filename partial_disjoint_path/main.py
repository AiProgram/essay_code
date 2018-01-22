#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
import utilities as util
import random
import time
import math
SCALE=10
if __name__=="__main__":
    max_com_vertex=10
    node_number=95
    SCALE=int(math.sqrt(node_number))
    edge_number=int(math.pow(node_number,2)/10)
    start_point_num=0
    des_point_num=10
    repeate_time=1
    graph_num=20
    util.generate_graph_group(node_number,edge_number,max_com_vertex,graph_num,start_point_num,des_point_num,repeate_time)
    util.run_mwld_group()
    util.run_new_alg_group(SCALE)
