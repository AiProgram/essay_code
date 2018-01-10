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

def generate_graph_from_src():
    graph=nx.DiGraph(nx.Graph())
    graph.add_node(0)
    graph.add_node(1)
    graph.add_node(2)
    graph.add_node(3)
    graph.add_node(4)
    graph.add_node(5)
    graph.add_node(6)
    
    graph.add_edge(0,1,weight=1)
    graph.add_edge(0,2,weight=3)
    graph.add_edge(1,2,weight=1)
    graph.add_edge(1,4,weight=4)
    graph.add_edge(2,3,weight=1)
    graph.add_edge(3,4,weight=1)
    graph.add_edge(4,5,weight=1)
    graph.add_edge(4,6,weight=3)
    graph.add_edge(5,6,weight=1)

    return graph