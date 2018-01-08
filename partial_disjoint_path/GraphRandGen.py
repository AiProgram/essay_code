#author:zeshanhu
#date:2018-1-8
import networkx as nx
import random
import matplotlib.pyplot as plt

def generate_graph_random(node_num=100,edge_num=400,debug=False):
    graph=nx.gnm_random_graph(node_num,edge_num,directed=True)
    for s,t in graph.edges():
        graph.add_edge(s,t,weight=random.randint(10,1000))
    if debug is True:
        plt.figure( figsize=(10,10),dpi=80)
        plt.subplot(111)
        nx.draw(graph, with_labels=True, font_weight='light')
        plt.show()
    
    return graph

