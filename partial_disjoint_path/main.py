#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
if __name__=="__main__":
    graph=grg.generate_graph_random(node_num=10,edge_num=50)
    residual_graph=am.get_residual_graph(start_point_num=0,des_point_num=5,graph=graph,debug=True)
    path=am.constrained_shortest_path(0,5,residual_graph,1,False)