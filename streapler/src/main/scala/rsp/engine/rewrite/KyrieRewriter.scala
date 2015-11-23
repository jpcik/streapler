package rsp.engine.rewrite

import java.util.ArrayList
import scala.collection.JavaConversions._
import org.antlr.runtime.ANTLRStringStream
import org.antlr.runtime.CommonTokenStream
import org.oxford.comlab.requiem.parser.ELHIOParser
import org.oxford.comlab.requiem.rewriter.Clause
import org.oxford.comlab.requiem.rewriter.PreprocessRewriter
import org.oxford.comlab.requiem.rewriter.TermFactory
import com.hp.hpl.jena.query.Query
import es.upm.fi.dia.oeg.newrqr.DatalogSPARQLConversor
import es.upm.fi.dia.oeg.newrqr.ISI2RQRLexer
import es.upm.fi.dia.oeg.newrqr.ISI2RQRParser
import org.slf4j.LoggerFactory

class KyrieRewriter(ontologyFile:String) {
  private val logger=LoggerFactory.getLogger(this.getClass)

  private val pw = {
    val dsc = new DatalogSPARQLConversor
    val m_parser = new ELHIOParser(new TermFactory, true)
    val ontoClauses=m_parser.getClauses(ontologyFile)
    new PreprocessRewriter(new ArrayList[Clause](ontoClauses), "F")
  }
  //def clausify(q:Query)=dsc.sparqlToDatalog(q).toSeq  
  
  def rewriteDatalogString(datalogQuery:String)= {
    val lexer = new ISI2RQRLexer(new ANTLRStringStream(datalogQuery))
    val parser = new ISI2RQRParser(new CommonTokenStream(lexer))
    rewriteDatalogClauses(parser.program)
  }
  
  def rewriteDatalogClauses(clauses:Seq[Clause])=
    pw.rewrite(new ArrayList(clauses)).toSeq    
    
  def rewrite(query:Query)={   
    val datalog=new DatalogXpr(query)   
    val clauseQuery=datalog.clausifiedQuery
    println(clauseQuery)
    logger.debug("The clausified query: "+clauseQuery)
    val clauses=rewriteDatalogClauses(Seq(clauseQuery))
    println("datalog result"+clauses.mkString("\n"))
    val q=datalog.generateSparql(clauses)

    println("Expanded query: "+q.toString)
    //val reordered=QueryReordering.reorder(query)
    //    logger.debug("reordered query: "+reordered.toString)
    q
  }
  
  def rewriteInUcq(query:Query)={   
    val datalog=new DatalogXpr(query)   
    val clauseQuery=datalog.clausifiedQuery
    logger.debug("The clausified query: "+clauseQuery)
    val clauses=rewriteDatalogClauses(Seq(clauseQuery))
    println("datalog result"+clauses.mkString("\n"))
    val q=datalog.generateSparqlQueries(clauses)

    //println("Expanded queryta: "+q.toString)
    //val reordered=QueryReordering.reorder(query)
    //    logger.debug("reordered query: "+reordered.toString)
    q
  }
  

}