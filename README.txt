You can change the maxDepth variable to control the search depth,
but it will take more time to go through all nodes. Basically with depth two, it already works well.

I run it with -Xms512m -Xmx1024m. If with low memory, may spend too much time on GC.

For further improvement, I think I can build some lookup table to improve search speed, then we can increase
maxDepth to get a better result.