package rsp.rspql.parser;

import org.apache.jena.sparql.lang.SPARQLParserBase;
import rsp.rspql.StreamQuery;

class RspqlParserBase extends SPARQLParserBase implements RspqlParserConstants{
  public StreamQuery getStreamQuery(){
	return (StreamQuery)query;
  }
}

