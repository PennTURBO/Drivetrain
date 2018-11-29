package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class MultiuseMethodsUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    val ontLoad: OntologyLoader = new OntologyLoader
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = false
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("get postfix from uri")
    {
        helper.getPostfixfromURI("http://biobank/uuid") should be ("uuid")
        helper.getPostfixfromURI("hello") should be ("hello")
        helper.getPostfixfromURI("http://biobank/") should be ("")
    }
    
    test("remove quotes from string")
    {
        update.removeQuotesFromString("\"hello\"") should be ("hello")
        update.removeQuotesFromString("hello") should be ("hello")
        update.removeQuotesFromString("\"hello") should be ("\"hello")
        update.removeQuotesFromString("hello\"") should be ("hello\"")
    }
    
    test("remove angle brackets from string")
    {
        helper.removeAngleBracketsFromString("<hello>") should be ("hello")
        helper.removeAngleBracketsFromString("hello") should be ("hello")
        helper.removeAngleBracketsFromString("<hello") should be ("<hello")
        helper.removeAngleBracketsFromString("hello>") should be ("hello>")
    }
    
    test("move data from one named graph to another")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:testgraph1 {
          pmbb:entity1 a pmbb:type1 .
          pmbb:entity2 a pmbb:type2 .}}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        helper.moveDataFromOneNamedGraphToAnother(cxn,
            "http://www.itmat.upenn.edu/biobank/testgraph1", 
            "http://www.itmat.upenn.edu/biobank/testgraph2")
            
        val ask: String = """
          ASK {GRAPH pmbb:testgraph2 {
          pmbb:entity1 a pmbb:type1 .
          pmbb:entity2 a pmbb:type2 .}}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("is there data in named graph")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:testgraph1 {
          pmbb:entity1 a pmbb:type1 .
          pmbb:entity2 a pmbb:type2 .}}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        helper.isThereDataInNamedGraph(cxn, cxn.getValueFactory.createIRI(
            "http://www.itmat.upenn.edu/biobank/testgraph1")) should be (true) 
        
        helper.isThereDataInNamedGraph(cxn, cxn.getValueFactory.createIRI(
            "http://www.itmat.upenn.edu/biobank/testgraph2")) should be (false) 
    }
    
    test("apply symmetrical properties - specified named graphs in input to method")
    {
        //this defaults to adding the TURBO ontology into the pmbb:ontology named graph if no arguments are given
        ontLoad.addOntologyFromUrl(cxn)
        
        //as of 5/4/2018, there is only one symmetric property in the TURBO ontology: TURBO_0000302 (shares row with)
        val insert: String = """
          INSERT DATA {
          
            GRAPH pmbb:testgraph1 {
            pmbb:entity1 a pmbb:type1 .
            pmbb:entity2 a pmbb:type2 .
            pmbb:entity1 turbo:TURBO_0000302 pmbb:entity2 .}
            
            GRAPH pmbb:testgraph2 {
            pmbb:entity3 a pmbb:type1 .
            pmbb:entity4 a pmbb:type2 .
            pmbb:entity4 turbo:TURBO_0000302 pmbb:entity3 .}
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        helper.applySymmetricalProperties(cxn, "http://www.itmat.upenn.edu/biobank/testgraph1")
        
        val ask1: String = """
          ASK {GRAPH pmbb:testgraph1 {
          pmbb:entity2 turbo:TURBO_0000302 pmbb:entity1 .}}
          """
        val ask2: String = """
          ASK {GRAPH pmbb:testgraph2 {
          pmbb:entity3 turbo:TURBO_0000302 pmbb:entity4 .}}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    test("apply symmetrical properties - operate over all named graphs")
    {
        //this defaults to adding the TURBO ontology into the pmbb:ontology named graph if no arguments are given
        ontLoad.addOntologyFromUrl(cxn)
        
        //as of 5/4/2018, there is only one symmetric property in the TURBO ontology: TURBO_0000302 (shares row with)
        val insert: String = """
          INSERT DATA {
          
            GRAPH pmbb:testgraph1 {
            pmbb:entity1 a pmbb:type1 .
            pmbb:entity2 a pmbb:type2 .
            pmbb:entity1 turbo:TURBO_0000302 pmbb:entity2 .}
            
            GRAPH pmbb:testgraph2 {
            pmbb:entity3 a pmbb:type1 .
            pmbb:entity4 a pmbb:type2 .
            pmbb:entity4 turbo:TURBO_0000302 pmbb:entity3 .}
          
            GRAPH pmbb:mondoOntology {
            pmbb:entity5 a pmbb:type1 .
            pmbb:entity6 a pmbb:type2 .
            pmbb:entity5 turbo:TURBO_0000302 pmbb:entity6 .}
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        //operate over all named graphs by default
        helper.applySymmetricalProperties(cxn)
        
        val ask1: String = """
          ASK {GRAPH pmbb:testgraph1 {
          pmbb:entity2 turbo:TURBO_0000302 pmbb:entity1 .}}
          """
        val ask2: String = """
          ASK {GRAPH pmbb:testgraph2 {
          pmbb:entity3 turbo:TURBO_0000302 pmbb:entity4 .}}
          """
        val ask3: String = """
          ASK {GRAPH pmbb:mondoOntology {
          pmbb:entity6 turbo:TURBO_0000302 pmbb:entity5 .}}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (false)
    }
    
    test("apply inverses")
    {
        //this defaults to adding the TURBO ontology into the pmbb:ontology named graph if no arguments are given
        ontLoad.addOntologyFromUrl(cxn)
        
        //as of 5/4/2018, there is only one symmetric property in the TURBO ontology: TURBO_0000302 (shares row with)
        val insert: String = """
          INSERT DATA {
          
            GRAPH pmbb:expanded {
            pmbb:entity1 a pmbb:type1 .
            pmbb:entity2 a pmbb:type2 .
            pmbb:entity1 obo:BFO_0000050 pmbb:entity2 .}
            
            GRAPH <http://www.itmat.upenn.edu/biobank/someGraph> {
            pmbb:entity3 a pmbb:type1 .
            pmbb:entity4 a pmbb:type2 .
            pmbb:entity4 obo:BFO_0000054 pmbb:entity3 .}
            
            GRAPH pmbb:inverses {
            pmbb:entity5 a pmbb:type1 .
            pmbb:entity6 a pmbb:type2 .
            pmbb:entity5 obo:BFO_0000051 pmbb:entity6 .}
            
            GRAPH pmbb:ontology {
            pmbb:entity7 a pmbb:type1 .
            pmbb:entity8 a pmbb:type2 .
            pmbb:entity7 obo:BFO_0000055 pmbb:entity8 .}
          
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        helper.applyInverses(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/someGraph"))
        
        val ask1: String = """
          ASK {GRAPH pmbb:inverses {
          pmbb:entity2 obo:BFO_0000051 pmbb:entity1 .}}
          """
        val ask2: String = """
          ASK {GRAPH pmbb:inverses {
          pmbb:entity3 obo:BFO_0000055 pmbb:entity4 .}}
          """
        val ask3: String = """
          ASK {GRAPH pmbb:inverses {
          pmbb:entity6 obo:BFO_0000050 pmbb:entity5 .}}
          """
        val ask4: String = """
          ASK {GRAPH pmbb:inverses {
          pmbb:entity8 obo:BFO_0000054 pmbb:entity7 .}}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask4).get should be (false)
    }
    
    test("add temp subject to all nodes to be reftracked")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
            
            pmbb:node1 graphBuilder:willBeCombinedWith pmbb:newNode1 .
            pmbb:node1 graphBuilder:placeholderDemotionType pmbb:retiredType1 .
            pmbb:subject1 pmbb:predicate1 pmbb:node1 .
            
            pmbb:node2 graphBuilder:willBeCombinedWith pmbb:newNode2 .
            pmbb:node2 graphBuilder:placeholderDemotionType pmbb:retiredType2 .
            }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        helper.addTempSubjectToAllNodesToBeReftracked(cxn)
        
        val ask1: String = """
          ASK {
          graphBuilder:tempSubj graphBuilder:tempPred pmbb:node1 .}
          """
        val ask2: String = """
          ASK {
          graphBuilder:tempSubj graphBuilder:tempPred pmbb:node2 .}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
    }
    
    test("remove temporary predicates")
    {
        val insert: String = """
          INSERT DATA {
            
            # GraphDB would weirdly not accept the prefixed version of this statement in this context.
            <http://graphBuilder.org/tempSubj> <http://graphBuilder.org/tempPred> pmbb:node1 .
            
            Graph pmbb:expanded
            {
                graphBuilder:tempSubj graphBuilder:tempPred pmbb:node2 .
            }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        helper.cleanupAfterCompletingReftrackProcess(cxn)
        
        val ask1: String = """
          ASK {
          graphBuilder:tempSubj graphBuilder:tempPred pmbb:node1 .}
          """
        val ask2: String = """
          ASK {
          graphBuilder:tempSubj graphBuilder:tempPred pmbb:node2 .}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    test("remove reftrack pointers")
    {
        val insert: String = """
          INSERT DATA {
              pmbb:node1 graphBuilder:willBeCombinedWith pmbb:reftrackednode .
              pmbb:node1 graphBuilder:placeholderDemotionType pmbb:retiredtype .
              Graph pmbb:expanded
              {
                  pmbb:node2 graphBuilder:willBeCombinedWith pmbb:reftrackednode .
                  pmbb:node2 graphBuilder:placeholderDemotionType pmbb:retiredtype .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        helper.cleanupAfterCompletingReftrackProcess(cxn)
        
        val ask1: String = """
          ASK {
          pmbb:node1 graphBuilder:willBeCombinedWith pmbb:reftrackednode .
          pmbb:node1 graphBuilder:placeholderDemotionType pmbb:retiredtype .}
          """
        val ask2: String = """
          ASK {
          pmbb:node2 graphBuilder:willBeCombinedWith pmbb:reftrackednode .
          pmbb:node2 graphBuilder:placeholderDemotionType pmbb:retiredtype .}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    test("finalize process test")
    {   
        val insert: String = """
          INSERT DATA {GRAPH pmbb:expanded {
          
              turbo:thing1 graphBuilder:willBeCombinedWith turbo:reftrackedthing .
              turbo:thing2 graphBuilder:willBeCombinedWith turbo:reftrackedthing .
              turbo:thing1 graphBuilder:placeholderDemotionType turbo:retiredtype .
              turbo:thing2 graphBuilder:placeholderDemotionType turbo:retiredtype .
              
              turbo:thing1 a turbo:type1 .
              turbo:thing2 a turbo:type2 .
              turbo:thing1 turbo:someProperty turbo:object1 .
              turbo:thing2 turbo:someProperty turbo:object2 .
              
              turbo:subject1 turbo:someProperty2 turbo:thing1 .
              turbo:subject2 turbo:someProperty2 turbo:thing1 .
              turbo:subject3 turbo:someProperty2 turbo:thing2 .
              turbo:subject4 turbo:someProperty2 turbo:thing2 .
              
          }}
          """
        //logger.info("starting complete reftrack process")
        update.updateSparql(cxn, sparqlPrefixes + insert)
        helper.completeReftrackProcess(cxn)
        
        val check1: String = """
             ASK {
                   ?thing1 a turbo:retiredtype .
                   ?thing2 a turbo:retiredtype .
                   ?thing1 turbo:TURBO_0001700 turbo:reftrackedthing .
                   ?thing2 turbo:TURBO_0001700 turbo:reftrackedthing .
                   ?thing1 obo:IAO_0000225 obo:IAO_0000226 .
                   ?thing2 obo:IAO_0000225 obo:IAO_0000226 .
                   ?thing1 turbo:TURBO_0006602 'http://transformunify.org/ontologies/thing1' .
                   ?thing2 turbo:TURBO_0006602 'http://transformunify.org/ontologies/thing2' .
                 }
          """
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
        
        val check2: String = """
             ASK {
                   turbo:reftrackedthing a turbo:type1 .
                   turbo:reftrackedthing a turbo:type2 .
                   turbo:reftrackedthing turbo:someProperty turbo:object1 .
                   turbo:reftrackedthing turbo:someProperty turbo:object2 .
                   turbo:subject1 turbo:someProperty2 turbo:reftrackedthing .
                   turbo:subject2 turbo:someProperty2 turbo:reftrackedthing .
                   turbo:subject3 turbo:someProperty2 turbo:reftrackedthing .
                   turbo:subject4 turbo:someProperty2 turbo:reftrackedthing .
                 }
          """
        val bool2: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check2).get
        bool2 should be (true)
        
        val check3: String = """
             ASK {
                     ?s graphBuilder:willBeCombinedWith ?o .
                 }
          """
        val bool3: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check3).get
        bool3 should be (false)
        
        val check4: String = """
             ASK {
                     ?s graphBuilder:placeholderDemotionType ?o .
                 }
          """
        val bool4: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check4).get
        bool4 should be (false)
        
         val check5: String = """
             ASK {
                         turbo:thing1 a turbo:retiredtype .
                 }
          """
        val bool5: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check5).get
        bool5 should be (false)
        
        val check6: String = """
             ASK {
                     turbo:thing2 a turbo:retiredtype .
                 }
          """
        val bool6: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check6).get
        bool6 should be (false)
        
        val check7: String = """
             ASK {
                     turbo:thing1 turbo:someProperty turbo:object1 .
                 }
          """
        val bool7: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check7).get
        bool7 should be (false)
        
        val check8: String = """
             ASK {
                     turbo:thing1 turbo:someProperty turbo:object2 .
                 }
          """
        val bool8: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check8).get
        bool8 should be (false)
        
        val check9: String = """
             ASK {
                     turbo:subject1 turbo:someProperty2 turbo:thing1 .
                 }
          """
        val bool9: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check9).get
        bool9 should be (false)
        
        val check10: String = """
             ASK {
                     turbo:subject2 turbo:someProperty2 turbo:thing1 .
                 }
          """
        val bool10: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check10).get
        bool10 should be (false)
        
        val check11: String = """
             ASK {
                     turbo:subject3 turbo:someProperty2 turbo:thing2 .
                 }
          """
        val bool11: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check11).get
        bool11 should be (false)
        
        val check12: String = """
             ASK {
                     turbo:subject4 turbo:someProperty2 turbo:thing2 .
                 }
          """
        val bool12: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check12).get
        bool12 should be (false)  
     
    val check13: String = """
          SELECT * WHERE 
          {
              ?thing a turbo:retiredtype .
          }
      """
    val result: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check13, Array("thing"))
    for (a <- result)
    {
      logger.info("found a result")
      logger.info(a(0).toString)
    }
    result.size should be (2)
    
    val node1: IRI = result(0)(0).asInstanceOf[IRI]
    val node2: IRI = result(1)(0).asInstanceOf[IRI]
    
    val check14: String = """
          SELECT * WHERE 
          {
              <"""+node1+"""> ?p ?o .
          }
      """
    val result2: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check14, Array("o"))
    result2.size should be (4)
    
    val check15: String = """
          SELECT * WHERE 
          {
              <"""+node2+"""> ?p ?o .
          }
      """
    val result3: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check15, Array("o"))
    result3.size should be (4)
    
    val check16: String = """
          SELECT * WHERE 
          {
              ?s ?p <"""+node2+"""> .
          }
      """
    val result4: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check16, Array("s"))
    result4.size should be (0)
    
    val check17: String = """
          SELECT * WHERE 
          {
              ?s ?p <"""+node1+"""> .
          }
      """
    val result5: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check17, Array("s"))
    result5.size should be (0)
    }
    
    test("can we reftrack something that doesn't exist as the object of a triple?")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:expanded {
          
              turbo:thing1 graphBuilder:willBeCombinedWith turbo:reftrackedthing .
              turbo:thing2 graphBuilder:willBeCombinedWith turbo:reftrackedthing .
              turbo:thing1 graphBuilder:placeholderDemotionType turbo:retiredtype .
              turbo:thing2 graphBuilder:placeholderDemotionType turbo:retiredtype .
              
              turbo:thing1 a turbo:type1 .
              turbo:thing2 a turbo:type2 .
              turbo:thing1 turbo:someProperty turbo:object1 .
              turbo:thing2 turbo:someProperty turbo:object2 .
              
              # turbo:subject1 turbo:someProperty2 turbo:thing1 .
              # turbo:subject2 turbo:someProperty2 turbo:thing1 .
              turbo:subject3 turbo:someProperty2 turbo:thing2 .
              turbo:subject4 turbo:someProperty2 turbo:thing2 .
              
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        helper.completeReftrackProcess(cxn)
        
        val check1: String = """
             ASK {
                   ?thing1 a turbo:retiredtype .
                   ?thing2 a turbo:retiredtype .
                   ?thing1 turbo:TURBO_0001700 turbo:reftrackedthing .
                   ?thing2 turbo:TURBO_0001700 turbo:reftrackedthing .
                   ?thing1 obo:IAO_0000225 obo:IAO_0000226 .
                   ?thing2 obo:IAO_0000225 obo:IAO_0000226 .
                   ?thing1 turbo:TURBO_0006602 'http://transformunify.org/ontologies/thing1' .
                   ?thing2 turbo:TURBO_0006602 'http://transformunify.org/ontologies/thing2' .
                 }
          """
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
        
        val check2: String = """
             ASK {
                   turbo:reftrackedthing a turbo:type1 .
                   turbo:reftrackedthing a turbo:type2 .
                   turbo:reftrackedthing turbo:someProperty turbo:object1 .
                   turbo:reftrackedthing turbo:someProperty turbo:object2 .

                   turbo:subject3 turbo:someProperty2 turbo:reftrackedthing .
                   turbo:subject4 turbo:someProperty2 turbo:reftrackedthing .
                 }
          """
        val bool2: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check2).get
        bool2 should be (true)
    }
    
    test("consolidate Lof Shortcut Graphs")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:LOFShortcuts_1
              {
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
              }
              Graph pmbb:LOFShortcuts_2
              {
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
              }
              Graph pmbb:LOFShortcuts_3
              {
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  # <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
              }
              Graph pmbb:LOFShortcuts_4
              {
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  # <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
              }
          }
          """
          update.updateSparql(cxn, sparqlPrefixes + insert)
          
          helper.consolidateLOFShortcutGraphs(cxn)
          
          val ask1 = """
            ASK
            {
                values ?g
                {
                    pmbb:LOFShortcuts_1
                    pmbb:LOFShortcuts_2
                    pmbb:LOFShortcuts_3
                    pmbb:LOFShortcuts_4
                }
                graph ?g
                {
                    ?s ?p ?o .
                }
            }
            """
          
          update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (false)
        
        val ask2 = """
          ASK
          {
              graph ?g
              {
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/f1d153d747ef47e1a2eedd25c60731ce> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .   
              
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # <http://localhost:8080/source/alleleInfo/e2d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
                  
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  # <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # <http://localhost:8080/source/alleleInfo/d3d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
                  
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007605> "gene:SCARF2(ENSG00000244486);zygosity:2" .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007601> "00058060-3736-4033-b1c4-2e78e1840311" .
                  # <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_Q96GP6"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN3705_7879e5d1-28ad-4f5b-aa63-83d6b456f91c" .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000422"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  <http://localhost:8080/source/alleleInfo/c4d153d747ef47e1a2eedd25c60731cd> <http://transformunify.org/ontologies/TURBO_0007610> "some value" .
              }
              filter (strStarts(str(?g), "http://www.itmat.upenn.edu/biobank/LOFShortcuts_consolidated_"))
          }
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
    }
}