#author:zeshanhu
#date:2018-1-8
import GraphRandGen as grg
import AlgorithmMain as am
if __name__=="__main__":
    graph=grg.generate_graph_random(node_num=20,edge_num=100)
    residual_graph=am.get_residual_graph(start_point_num=0,des_point_num=10,graph=graph,debug=False)