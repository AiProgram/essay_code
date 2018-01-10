#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
import utilities as util
import random
if __name__=="__main__":
    max_com_vertex=7
    node_number=7
    edge_number=9
    start_point_num=0
    des_point_num=6

    graph=grg.generate_graph_from_src()
    path_p,residual_graph=am.get_residual_graph(start_point_num,des_point_num,graph=graph,debug=False)
    path_Q,dist=am.constrained_shortest_path(start_point_num,des_point_num,residual_graph,max_com_vertex,debug=False)
    print("Find a shortest path from %d to %d"%(start_point_num,des_point_num))
    print("P path "+str(path_p))
    print("Q path "+str(path_Q))
    print("the sum of path Q 's weight is %f"%(dist))
    print("validate path Q: "+str(util.verify_RSP_result(residual_graph,path_Q,max_com_vertex)))