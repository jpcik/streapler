# streapler


StreamQR provides query answering over ontologies for RSP, through a novel approach that combines query rewriting techniques and an RDF stream query processing. 

While most of the focus on query rewriting has been on OBDA for relational databases, we show that it is possible to use it for rewriting continuous queries over streams of RDF data. RDF streams, understood as potentially infinite flows of timestamped triples, require reactive processing of queries, and therefore most RSP engines have focused on efficiency and high throughput. StreamQR demonstrates that these engines can be coupled with a query rewriter, and still be efficient for a large range of scenarios. StreamQR is implemented extending the CQELS RDF query processor, with the kyrie rewriting engine.  