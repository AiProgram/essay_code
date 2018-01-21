#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
import utilities as util
import random
import time
SCALE=5
if __name__=="__main__":
    max_com_vertex=10
    node_number=50
    edge_number=250
    start_point_num=0
    des_point_num=10
    repeate_time=1
    graph_num=20
    util.generate_graph_group(node_number,edge_number,max_com_vertex,graph_num,start_point_num,des_point_num,repeate_time)
    util.run_code_group(SCALE)
if __name__!="__main__":
    max_com_vertex=2
    node_number=800
    edge_number=60000
    start_point_num=0
    des_point_num=6
    json_graph_file_name="partial_disjoint_path\\java_code\\graph_data\\json_graph.json"

    #generate random graph
    graph=grg.generate_graph_random(node_number,edge_number)

    #save graph into a json file 
    LP_graph=am.get_graph_for_LP(graph,start_point_num,des_point_num,max_com_vertex)
    util.save_graph_to_json(LP_graph,json_graph_file_name)

    time_start=time.time()
    path_p,residual_graph=am.get_residual_graph(start_point_num,des_point_num,graph=graph,debug=False)
    #path_Q,dist=am.constrained_shortest_path(start_point_num,des_point_num,residual_graph,max_com_vertex,debug=True)
    dist,path_Q=am.RSP_with_recursion(residual_graph,start_point_num,des_point_num,max_com_vertex*SCALE)
    time_end=time.time()

    print("Find a shortest path from %d to %d"%(start_point_num,des_point_num))
    print("P path "+str(path_p))
    print("Q path "+str(path_Q))
    print("the sum of path Q 's weight is %f"%(util.get_SP_weight(graph,path_p)))
    print("the sum of path Q 's weight is %f"%(dist))
    print("the total weight is %f"%(dist+util.get_SP_weight(graph,path_p)))
    print("-----------")
    print("validate path Q: "+str(util.verify_RSP_result(residual_graph,path_Q,max_com_vertex*SCALE)))
    path_p1,path_p2=am.path_XOR(graph,residual_graph,path_p,path_Q)
    print("path_p1: "+str(path_p1))
    print("path_p2: "+str(path_p2))

    time_used=(time_end-time_start)/1000
    print("time used %fs"%(time_used))