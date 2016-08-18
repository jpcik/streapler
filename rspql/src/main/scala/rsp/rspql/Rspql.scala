package rsp.rspql

import org.apache.jena.query.QueryFactory
import org.apache.jena.query.Syntax
import org.apache.jena.sparql.lang.SPARQLParser
import org.apache.jena.sparql.lang.SPARQLParserFactory
import org.apache.jena.sparql.lang.SPARQLParserRegistry
import rsp.rspql.parser.ParserRspql

object Rspql {
  def parse(queryString:String)=RspqlQueryFactory.create(queryString)
}

object RspqlSyntax extends Syntax("http://w3c.org/query/RSP-QL"){
  Syntax.querySyntaxNames.put("rspql",RspqlSyntax)
}

object RspqlQueryFactory extends QueryFactory{
  def create(queryString:String):StreamQuery=	
	  create(queryString,null,RspqlSyntax)
	
  def create(queryString:String, baseURI:String, querySyntax:Syntax)={
	  val query = new StreamQuery
	  if (!SPARQLParserRegistry.containsParserFactory(querySyntax)){
	    SPARQLParserRegistry.addFactory(RspqlSyntax,                      
	      new SPARQLParserFactory(){
	        def accept(syntax:Syntax):Boolean=RspqlSyntax.equals(syntax) 	      
	        def create(syntax:Syntax):SPARQLParser=new ParserRspql 
	      }) 
	  }
	  QueryFactory.parse(query, queryString, baseURI, querySyntax)
	  query 	       
  }	   
}
