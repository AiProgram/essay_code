# δV-2EDSP算法java实现版
## 包 *myGraph*
+ ### 类 *MyGraph*
+ `JGraphT`所提供的图最多只能在边上联系一种属性，本算法中图的边有多个属性，将`JGraphT`提供的图包装到*MyGraph*类中来保存属性信息，同时也可以储存算法的相关参数以及结果信息，方便算法的编写.
+ **属性**
    + *graph*：默认使用`JGraphT`中提供的`DefaultDirectedWeightedGraph`也就是带有weight的有向非重复图
    + *costMap*:使用Map数据结构储存每一条边对应的cost
    + *nodeNum*:图的顶点数
    + *edgeNum*:图的边数
    + *startPoint*:算法运行时给定的起点
    + *sinkPoint*:算法运行给定的终点
    + *maxComVertex*:算法给定的最大相交点数
    + *mutiGraph*:标记图是否允许平行边
    + *directed*:标记图是否有向