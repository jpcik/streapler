package rsp.engine.rewrite

import scala.collection.JavaConversions._
import scala.language.implicitConversions

import org.deri.cqels.lang.cqels.ElementStreamGraph
import org.oxford.comlab.requiem.rewriter._
import org.slf4j.LoggerFactory

import com.hp.hpl.jena.graph._
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.sparql.core.TriplePath
import com.hp.hpl.jena.sparql.core.Var
import com.hp.hpl.jena.sparql.syntax._

import com.hp.hpl.jena.vocabulary.RDF

import rsp.util.JenaTools.toJenaRes

class DatalogXpr(query:Query) {
  private val tf=new TermFactory
  private val logger=LoggerFactory.getLogger(this.getClass)
  
  private val mappedVars=vars(query.getQueryPattern).toSet.zipWithIndex.toMap
  private val sparqlVars=mappedVars.map(m=>"?"+m._2->m._1)
  
  /** Datalog clauses for the query */
  lazy val clausifiedQuery:Clause={
    val queryClauses=clausify(query.getQueryPattern).toArray
    //val projVars=query.getProjectVars.map(_.getVarName).toSeq
    //  .filter(v=>mappedVars.contains(v))
    val projVars=query.getConstructTemplate.getTriples.map(vars).flatten
      .filter(v=>mappedVars.contains(v))
    val head=new FunctionalTerm("Q",projVars.map{v=>
      tf.getVariable(mappedVars(v))
    }.toArray)
    logger.info(head.toString())
    val cq=new Clause(queryClauses,head)
    logger.info("Clausified Query: "+cq)
    cq
  }

  def generateSparqlQueries(clauses:Seq[Clause])={    
    val replacedClauses=clauses.map(c=>replaceVars(c))
    
    val blocks=replacedClauses.map{c=>
      val pb=new ElementPathBlock      
      c.getBody.foreach{t=>
        val triple=triplify(t,null,sparqlVars)
        pb.addTriple(triple)
      }
      pb
    }
    //val union = new ElementUnion
    blocks.map{b=>
      
      val rep=replaceElems(query.getQueryPattern, b)
      logger.debug("class of query "+query.getClass())
      val newQuery=query.cloneQuery
      newQuery.setQueryPattern(rep)
      newQuery
    }
  }

  
  def generateSparql(clauses:Seq[Clause])={    
    val replacedClauses=clauses.map(c=>replaceVars(c))
    
    val blocks=replacedClauses.map{c=>
      val pb=new ElementPathBlock      
      c.getBody.foreach{t=>
        val triple=triplify(t,null,sparqlVars)
        pb.addTriple(triple)
      }
      pb
    }
    val union = new ElementUnion
    blocks.foreach(b=>union.addElement(b))
    val rep=replaceElems(query.getQueryPattern, union)
    logger.debug("class of query "+query.getClass())
    val newQuery=query.cloneQuery
    newQuery.setQueryPattern(rep)
    newQuery
  }

  private def replaceVars(c:Clause)={
    val head=clausifiedQuery.getHead
    val headVars:Array[Int]=head.getArguments.map(toInt)
    val clauseHeadVars=c.getHead.getArguments.map(toInt)    
    logger.debug((clauseHeadVars zip headVars).mkString("--"))
    val replaceHead=(clauseHeadVars zip headVars).toMap
    
    val bodyVars=clausifiedQuery.getBody.map{t=>
      t.getName->t.getArguments.filter(a=>a.isInstanceOf[Variable]).map(toInt)      
    }.toMap
    val replaceBody=c.getBody.map{t=>
      val tt=bodyVars.getOrElse(t.getName, Array())
      val tt2=t.getArguments.filter(a=>a.isInstanceOf[Variable]).map(toInt)
      tt2.zip(tt).filter{case (k,v)=> !replaceHead.contains(k)}
    }.flatten.toMap
              
    logger.debug("replacing vars: "+(replaceHead++replaceBody).mkString(","))
    new Clause(c.getBody.map(t=>replaceVar(replaceHead++replaceBody,t)),head)
  }
  
  private def replaceVar(vars:Map[Int,Int],t:Term):Term={
    logger.trace("replace vars in term: "+t)
    val args=t.getArguments.map{
      case v:Variable=>
        val vInt=toInt(v)
        logger.debug("Now replacing var: "+ vInt)
        if (vars.contains(vInt)) _var(vars(vInt))
        else _var(vInt+1000)
      case fTerm:FunctionalTerm=>replaceVar(vars,fTerm)
      case default => default
    }
    new FunctionalTerm(t.getName,args)
  }
    
  //TODO: generalize for more union replacements
  private def replaceElems(e:Element,union:ElementUnion):Element=e match{
    case group:ElementGroup=>
      val ng=new ElementGroup()      
      ng.getElements.addAll(group.getElements.map(el=>replaceElems(el,union)).toList)
      ng
    case pathblock:ElementPathBlock=>union
    case s:ElementStreamGraph=>
     
     new ElementStreamGraph(s.getGraphNameNode(),s.getWindow(), replaceElems(s.getElement(),union))
    case ng:ElementNamedGraph=>
      
      val newNg= new ElementNamedGraph(ng.getGraphNameNode(),replaceElems(ng.getElement(),union))
      newNg
    case _=>e
  }  

  private def replaceElems(e:Element,bgp:ElementPathBlock):Element=e match{
    case group:ElementGroup=>
      val ng=new ElementGroup      
      ng.getElements.addAll(group.getElements.map(el=>replaceElems(el,bgp)).toList)
      ng
    case pathblock:ElementPathBlock=>bgp
    case s:ElementStreamGraph=>     
     new ElementStreamGraph(s.getGraphNameNode,s.getWindow, replaceElems(s.getElement,bgp))
    case ng:ElementNamedGraph=>      
      new ElementNamedGraph(ng.getGraphNameNode,replaceElems(ng.getElement,bgp))      
    case _=>e
  }  
  
  
  private def triplify(t:Term,vocab:Map[String,Node_URI],vars:Map[String,String])={
    def sparqlVar(key:String)=
      if (key startsWith "?")
      Var.alloc(vars.getOrElse(key,key.replace("?", "")))
      else toJenaRes(key).asNode
    //TODO keep original namespaces
    logger.info("triplif Term: "+t)
    val pref=""//"http://purl.oclc.org/NET/ssnx/ssn#"
    val predName=
      if (t.getArity==1) RDF.`type` 
      else toJenaRes(pref+t.getName)
    val subj=t.getArgument(0).getName
    val obj= 
      if (t.getArity==1) toJenaRes(pref+t.getName).asNode
      else if (t.getArgument(1).getName.startsWith("?")){
        val varname=t.getArgument(1).getName
        logger.debug("thevar is "+vars.get(varname))
        sparqlVar(varname)
      }
      else toJenaRes(pref+t.getArgument(1).getName).asNode
    logger.debug("triple created "+subj+ " "+predName+" "+obj)
    
    new Triple( sparqlVar(subj),predName.asNode, obj)        
  }

  
  private def clausify(el:Element):Seq[Term]=el match{
    case group:ElementGroup=>
      group.getElements.map(e=>clausify(e)).flatten
    case triples:ElementPathBlock=>
      logger.debug(triples.toString)
      clausify(triples.getPattern.getList)
    case gr:ElementNamedGraph=>
      clausify(gr.getElement)
    case _ =>Seq()
  }
  
  private def clausify(triples:Seq[TriplePath])=triples.map{t=>
    if (t.getPredicate.getURI.equals(RDF.`type`.getURI))
      new FunctionalTerm(t.getObject.getURI,
          Array(term(t.getSubject)))  
    else 
      new FunctionalTerm(t.getPredicate.getURI,
          Array(term(t.getSubject),term(t.getObject)))
    }.toArray       
  
  /** Create term */
  private def term(node:Node)={
    if (node.isVariable) 
      _var(mappedVars(node.getName))
    else if (node.isLiteral) 
      const(node.getLiteralValue.toString)
    else 
      const(node.getURI)    
  }
    
  /** Create a variable */
  private def _var(i:Int)=tf.getVariable(i)

  /** Create a constant */
  private def const(s:String)=tf.getConstant(s)
  
  private implicit def toInt(t:Term)=
    t.getName.replace("?","").toInt

  private def vars(el:Element):Seq[String]=el match{
    case group:ElementGroup=>
      group.getElements.map(vars(_)).flatten
    case path:ElementPathBlock=>
      path.getPattern.map(t=>vars(t)).toSeq.flatten
    case gr:ElementNamedGraph=>
      vars(gr.getElement)
    case _ => Seq()
  }
  
  private def vars(t:TriplePath)={
    def vars(n:Node)=n match {
      case v:Var=>Seq(v.getVarName)
      case _ => Seq()
    }
    vars(t.getSubject)++vars(t.getObject)
  }
  
  
  private def vars(t:Triple)={
    def vars(n:Node)=n match {
      case v:Var=>Seq(v.getVarName)
      case _ => Seq()
    }
    vars(t.getSubject)++vars(t.getObject)
  }
  
}