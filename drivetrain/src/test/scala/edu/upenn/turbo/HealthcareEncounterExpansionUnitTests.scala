package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID

class HealthcareEncounterExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val expand = new Expander
    val objectOrientedExpander = new ObjectOrientedExpander
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
    
    val healthcareEncounterShortcutGraphs: String = 
    """
        <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts> 
        <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts1> 
        <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts2>
        <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts3>
        <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts4>
    """
    
    val instantiationAndDataset: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
            pmbb:test_instantiation_1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "enc_expand.csv"^^xsd:string .
       }}"""
    
    val healthcareEncounterMinimum: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
            ?encounter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OGMS_0000097 .
        		?encounter turbo:TURBO_0006601 "http://www.itmat.upenn.edu/biobank/hcenc1" .
        		?encounterCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000508 .
        		?encounterCrid obo:IAO_0000219 ?encounter .
        		
        		?encounterCrid obo:BFO_0000051 ?encounterSymb .
        		?encounterCrid obo:BFO_0000051 ?encounterRegDen .
        		?encounterSymb obo:BFO_0000050 ?encounterCrid .
        		?encounterRegDen obo:BFO_0000050 ?encounterCrid .
        		
        		?encounterSymb a turbo:TURBO_0000509 .
        		?encounterRegDen a turbo:TURBO_0000510 .
        		
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?encounterDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encounterDate obo:IAO_0000136 ?encStart .
       }}
      """
    
    val healthcareSymbolAndRegistry: String = """
      ASK {GRAPH pmbb:postExpansionCheck {
          ?encounter a obo:OGMS_0000097 .
          ?encounterCrid a turbo:TURBO_0000508 .
          ?encounterCrid obo:IAO_0000219 ?encounter .
          ?encounterCrid obo:BFO_0000051 ?encsymb .
      		?encounterCrid obo:BFO_0000051 ?encregden .
      		?encsymb a turbo:TURBO_0000509 .
      		?encregden a turbo:TURBO_0000510 .
      		?encsymb turbo:TURBO_0006510 '20' .
      		?encregden obo:IAO_0000219 turbo:TURBO_0000440 .
      		turbo:TURBO_0000440 a turbo:TURBO_0000513 .
      		?encSymb obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?encSymb .
      		?encregden obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?encregden .
      		?dataset a obo:IAO_0000100 .
      }}
      """
   
    val healthcareDiagnosis: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
          
                ?dataset a obo:IAO_0000100 .
                ?encounter a obo:OGMS_0000097 .
            		?encounter obo:RO_0002234 ?diagnosis .
            		?diagnosis a obo:OGMS_0000073 .
            		?diagnosis turbo:TURBO_0006512 "401.9" .
            		?diagnosis turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
            		?diagnosis turbo:TURBO_0006515 "ICD-9" .
            		?diagnosis turbo:TURBO_0000306 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
            		?diagnosis obo:BFO_0000050 ?dataset .
            		?dataset obo:BFO_0000051 ?diagnosis .
            		
            		?diagnosis turbo:TURBO_0010013 "true"^^xsd:Boolean .
            		?diagnosis turbo:TURBO_0010014 "1"^^xsd:Integer .
        	}}
          """
    
    val healthcareMedications: String = """
      ask {graph pmbb:postExpansionCheck {
          ?dataset a obo:IAO_0000100 .
          ?encounter a obo:OGMS_0000097 .
          ?encounter obo:RO_0002234 ?drugPrescript .
      		?drugPrescript a obo:PDRO_0000001 .
      		?drugPrescript turbo:TURBO_0006512 "holistic soil from the ganges" .
      		?medCrid obo:IAO_0000219 ?drugPrescript .
      		?medCrid a turbo:TURBO_0000561 .
      		?medCrid obo:BFO_0000051 ?medCridSymbol .
      		?medCridSymbol a turbo:TURBO_0000562 .
      		?medCridSymbol turbo:TURBO_0006510 "3" .
      		
      		?drugPrescript obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?drugPrescript .
      		?medCridSymbol obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?medCridSymbol .
      		}}
      """
    
    val healthcareHeightWeightAndBMI: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
          
                ?encounter obo:RO_0002234 ?BMI .
                ?encounter a obo:OGMS_0000097 .
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:OBI_0001938 ?BMIvalspec .
            		?BMIvalspec a obo:OBI_0001933 .
            		?BMIvalspec obo:OBI_0002135 "26.2577659792"^^xsd:float .
            		?BMI obo:IAO_0000581 ?encDate .
            		?encDate a turbo:TURBO_0000512 .
            		?BMI obo:BFO_0000050 ?dataset .
            		?dataset a obo:IAO_0000100 .
            		?heightValSpec rdf:type obo:OBI_0001931 ;
            		               obo:IAO_0000039 obo:UO_0000015 ;
            		               obo:OBI_0002135 "177.8"^^xsd:float .
            	    ?heightAssay rdf:type turbo:TURBO_0001511 ;
            	                 obo:BFO_0000050 ?encounter ;
            	                 obo:OBI_0000299 ?heightDatum .
            	    ?heightDatum rdf:type obo:IAO_0000408 ;
            	                 obo:OBI_0001938 ?heightValSpec ;
            	                 obo:BFO_0000050 ?dataset .
            	    ?weightAssay rdf:type obo:OBI_0000445 ;
            	                 obo:BFO_0000050 ?encounter ;
            	                 obo:OBI_0000299 ?weightDatum .
            	    ?weightDatum rdf:type obo:IAO_0000414 ;
            	                 obo:OBI_0001938 ?weightValSpec ;
            	                 obo:BFO_0000050 ?dataset .
            	    ?weightValSpec rdf:type obo:OBI_0001931 ;
            	                   obo:IAO_0000039 obo:UO_0000009 ;
            	                   obo:OBI_0002135 "83.0082554658"^^xsd:float .
            	                   
            	    ?dataset obo:BFO_0000051 ?BMI .
            		  ?dataset obo:BFO_0000051 ?weightDatum .
            		  ?dataset obo:BFO_0000051 ?heightDatum .
        	}}
        	"""
    
    val healthcareEncounterDate: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
            ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encDate turbo:TURBO_0006512 "15/Jan/2017" .
        		?encDate turbo:TURBO_0006511 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?dataset obo:BFO_0000051 ?encDate .
        		?encDate obo:BFO_0000050 ?dataset .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?dataset a obo:IAO_0000100 .
        		?encounter a obo:OGMS_0000097 .
           }}"""
    
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
    
    test("hc encounter with all fields")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          
          <http://www.itmat.upenn.edu/biobank/hcenc1>
          turbo:TURBO_0000655 "26.2577659792"^^xsd:float ;
          turbo:TURBO_0000644 "15/Jan/2017" ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0000647 "83.0082554658"^^xsd:float ;
          turbo:TURBO_0000646 "177.8"^^xsd:float ;
          turbo:TURBO_0000645 "2017-01-15"^^xsd:date ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0000643 "enc_expand.csv" ;
          
          obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 ;
          turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0004602 "ICD-9" ;
          turbo:TURBO_0004601 "401.9" ;
          turbo:TURBO_0010013 "true"^^xsd:Boolean ;
          turbo:TURBO_0010014 "1"^^xsd:Integer .
          
          pmbb:hcenc1 obo:RO_0002234 turbo:prescription1 .
          turbo:prescription1 a obo:PDRO_0000001 ;
          turbo:TURBO_0005601 "3" ;
          turbo:TURBO_0005611 "holistic soil from the ganges" .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006601",
            "http://purl.obolibrary.org/obo/RO_0002234", "http://purl.obolibrary.org/obo/RO_0002234", 
            "http://purl.obolibrary.org/obo/RO_0002234", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0000306",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://transformunify.org/ontologies/TURBO_0000703",
            "http://purl.obolibrary.org/obo/RO_0002223", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006512",
            "http://transformunify.org/ontologies/TURBO_0006511", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0006512", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0001938", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://purl.obolibrary.org/obo/IAO_0000581", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006512",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010013", "http://transformunify.org/ontologies/TURBO_0010014"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (82)
    }
    
    test("hc encounter with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006601",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (25)
    }
    
    test("hc encounter without ID")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (22)
    }
   
    test("hc encounter without registry")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000648 "20" .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (21)
    }
    
    test("hc encounter without dataset")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000648 "20" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("hc encounter with text but not xsd values and only diag code without reg info")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000655 "26.2577659792"^^xsd:float ;
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000644 "15/Jan/2017" ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0000647 "83.0082554658"^^xsd:float ;
          turbo:TURBO_0000646 "177.8"^^xsd:float ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          a obo:OGMS_0000097 ;
          obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 ;
              turbo:TURBO_0004602 "ICD-9" .
              
          pmbb:hcenc1 obo:RO_0002234 turbo:prescription1 .
          turbo:prescription1 a obo:PDRO_0000001 ;
          turbo:TURBO_0005601 "3" ;
          turbo:TURBO_0005611 "holistic soil from the ganges" .
          
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        val diagnosisNoXsd: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
                ?encounter a obo:OGMS_0000097 .
                ?dataset a obo:IAO_0000100 .
        		?encounter obo:RO_0002234 ?diagnosis.
        		?diagnosis a obo:OGMS_0000073 .
        		?diagnosis turbo:TURBO_0006515 "ICD-9" .
        		?diagnosis obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?diagnosis .
        	}}
          """
        
        val dateNoXsd: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
                ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encDate turbo:TURBO_0006512 "15/Jan/2017" .
        		# ?encDate turbo:TURBO_0006511 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?dataset obo:BFO_0000051 ?encDate .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?dataset a obo:IAO_0000100 .
        		?encounter a obo:OGMS_0000097 .
           }}"""
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + diagnosisNoXsd).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + dateNoXsd).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006601",
            "http://purl.obolibrary.org/obo/RO_0002234", "http://purl.obolibrary.org/obo/RO_0002234", 
            "http://purl.obolibrary.org/obo/RO_0002234", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006512",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://transformunify.org/ontologies/TURBO_0006512", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0001938", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://purl.obolibrary.org/obo/IAO_0000581", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (76)
    }
    
    test("ensure diagnosis info stays together with duplicate hc enc URI")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 ;
          turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0004602 "ICD-9" ;
          turbo:TURBO_0004601 "401.9" .
          
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          obo:RO_0002234 turbo:diagnosis2 .
          turbo:diagnosis2 a obo:OGMS_0000073 ;
          turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0004602 "ICD-10" ;
          turbo:TURBO_0004601 "177.8" . 
          }}"""
        
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        val checkDiag: String = """
          Ask
          {
              Graph pmbb:postExpansionCheck
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:RO_0002234 ?diagnosis1 .
                  ?diagnosis1 a obo:OGMS_0000073 .
                  ?diagnosis2 a obo:OGMS_0000073 .
                  
                  ?diagnosis1 turbo:TURBO_0006515 "ICD-9" .
                  ?diagnosis1 turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
                  ?diagnosis1 turbo:TURBO_0006512 "401.9" .
                  ?diagnosis1 turbo:TURBO_0000306 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
                  
                  ?diagnosis2 turbo:TURBO_0006515 "ICD-10" .
                  ?diagnosis2 turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
                  ?diagnosis2 turbo:TURBO_0006512 "177.8" .
                  ?diagnosis2 turbo:TURBO_0000306 <http://purl.bioontology.org/ontology/ICD10CM/177.8> .
                  
                  Filter (?diagnosis1 != ?diagnosis2) 
              }
          }
          """
        
        val countDiag: String = """
          Select (count (distinct ?diagnosis) as ?diagnosisCount)
          {
              Graph pmbb:postExpansionCheck
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:RO_0002234 ?diagnosis .
                  ?diagnosis a obo:OGMS_0000073 .
              }
          }
          """
         update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
         update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (true)
         update.querySparqlBoolean(cxn, sparqlPrefixes + checkDiag).get should be (true)
         update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countDiag, "diagnosisCount")(0) should startWith ("\"2")
         update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (true)
    }
    
    /*test("ensure medication info stays together with duplicate hc enc URI")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          obo:RO_0002234 turbo:prescription1 .
          turbo:prescription1 a obo:PDRO_0000001 ;
          turbo:TURBO_0005601 "3" ;
          turbo:TURBO_0005611 "holistic soil from the ganges" .
          
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          obo:RO_0002234 turbo:prescription2 .
          turbo:prescription2 a obo:PDRO_0000001 ;
          turbo:TURBO_0005601 "4" ;
          turbo:TURBO_0005611 "medicinal purple kush" . 
          }}"""
        
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.expandHealthcareEncounterShortcuts(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), healthcareEncounterShortcutGraphs, randomUUID)
        
        val checkDiag: String = """
          Ask
          {
              Graph pmbb:postExpansionCheck
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:RO_0002234 ?prescription1 .
                  ?prescription1 a obo:PDRO_0000001 .
                  ?prescription1 turbo:TURBO_0006512 "holistic soil from the ganges" .
                  ?medCrid1 obo:IAO_0000219 ?prescription1 .
                  ?medCrid1 a turbo:TURBO_0000561 .
                  ?enc obo:RO_0002234 ?prescription2 .
                  ?prescription2 a obo:PDRO_0000001 .
                  ?prescription2 turbo:TURBO_0006512 "medicinal purple kush" .
                  ?medCrid2 obo:IAO_0000219 ?prescription2 .
                  ?medCrid2 a turbo:TURBO_0000561 .
                  
                  ?medCrid1 obo:BFO_0000051 ?medCridSymbol1 .
                  ?medCridSymbol1 a turbo:TURBO_0000562 .
                  ?medCridSymbol1 turbo:TURBO_0006510 "3" .
                  
                  ?medCrid2 obo:BFO_0000051 ?medCridSymbol2 .
                  ?medCridSymbol2 a turbo:TURBO_0000562 .
                  ?medCridSymbol2 turbo:TURBO_0006510 "4" .
                  
                  Filter (?medCrid1 != ?medCrid2) 
                  Filter (?prescription1 != ?prescription2) 
              }
          }
          """
        
        val countDiag: String = """
          Select (count (distinct ?prescription) as ?prescriptCount) (count (distinct ?medCrid) as ?medCridCount)
          {
              Graph pmbb:postExpansionCheck
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:RO_0002234 ?prescription .
                  ?prescription a obo:PDRO_0000001 .
                  ?medCrid obo:IAO_0000219 ?prescription .
              }
          }
          """
         update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
         update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (true)
         update.querySparqlBoolean(cxn, sparqlPrefixes + checkDiag).get should be (true)
         update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countDiag, "prescriptCount")(0) should startWith ("\"2")
         update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countDiag, "medCridCount")(0) should startWith ("\"2")
         update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (true)
    }
    
    test("expand hc encs over multiple named graphs")
    {
        val insert1: String = """
          INSERT DATA
          {
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 ;
                      turbo:TURBO_0000643 'identifierAndRegistry.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0000650 'http://transformunify.org/ontologies/TURBO_0000440'^^xsd:anyURI .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts1
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 ;
                      turbo:TURBO_0000643 'diagnosis.csv' ;
                      obo:RO_0002234 pmbb:diagCridSC .
                  pmbb:diagCridSC a obo:OGMS_0000073 ;
                      turbo:TURBO_0004602 'ICD-9' ;
                      turbo:TURBO_0004603 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890'^^xsd:anyURI ;
                      turbo:TURBO_0004601 '401.9' ;
                      turbo:TURBO_0010013 "true"^^xsd:Boolean ;
                      turbo:TURBO_0010014 "1"^^xsd:Integer .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts2
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 ;
                      turbo:TURBO_0000643 'meds.csv' ;
                      obo:RO_0002234 pmbb:prescription .
                      pmbb:prescription a obo:PDRO_0000001 ;
                      turbo:TURBO_0005611 "holistic soil from the ganges" ;
                      turbo:TURBO_0005601 "3" .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts3
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 ;
                      turbo:TURBO_0000643 'bmiAndHeightWeight.csv' ;
                      turbo:TURBO_0000646 '177.8'^^xsd:float ;
                      turbo:TURBO_0000647 '83.0082554658'^^xsd:float ;
                      turbo:TURBO_0000655 '26.2577659792'^^xsd:float .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts4
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 ;
                      turbo:TURBO_0000643 'date.csv' ;
                      turbo:TURBO_0000644 '15/Jan/2017' ;
                      turbo:TURBO_0000645 '2017-01-15'^^xsd:date .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert1)
        expand.expandHealthcareEncounterShortcuts(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), healthcareEncounterShortcutGraphs, randomUUID)
        
        val datasetCheck1: String = """
          ASK
          {
              GRAPH pmbb:postExpansionCheck
              {
                  ?encounter a obo:OGMS_0000097 .
                  ?encounterCrid a turbo:TURBO_0000508 .
                  ?encounterCrid obo:IAO_0000219 ?encounter .
                  ?encounterCrid obo:BFO_0000051 ?encsymb .
              		?encounterCrid obo:BFO_0000051 ?encregden .
              		?encsymb a turbo:TURBO_0000509 .
              		?encregden a turbo:TURBO_0000510 .
              		
              		?encSymb obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?encSymb .
              		?encregden obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?encregden .
              		
              		?dataset a obo:IAO_0000100 .
              		?dataset dc11:title 'identifierAndRegistry.csv'^^xsd:string .
              		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
              		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
              }
          }
          """
        
        val datasetCheck2: String = """
          ASK
          {
              GRAPH pmbb:postExpansionCheck
              {
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title 'diagnosis.csv'^^xsd:string .
                  pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
                  
                  ?encounter a obo:OGMS_0000097 .
              		?encounter obo:RO_0002234 ?diagnosis .
              		?diagnosis a obo:OGMS_0000073 .
              		?diagnosis turbo:TURBO_0006512 "401.9" .
            		  ?diagnosis turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
            		  ?diagnosis turbo:TURBO_0006515 "ICD-9" .
            		  ?diagnosis turbo:TURBO_0000306 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
              		
              		?dataset obo:BFO_0000051 ?diagnosis .
              		?diagnosis obo:BFO_0000050 ?dataset .
              }
          }
          """
        
        val datasetCheck3: String = """
          ASK
          {
              GRAPH pmbb:postExpansionCheck
              {
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title 'meds.csv'^^xsd:string .
                  pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
                  
                  ?encounter a obo:OGMS_0000097 .
              		?encounter obo:RO_0002234 ?drugPrescript .
              		?drugPrescript a obo:PDRO_0000001 .
              		?medCrid obo:IAO_0000219 ?drugPrescript .
              		?medCrid a turbo:TURBO_0000561 .
              		?medCrid obo:BFO_0000051 ?medCridSymbol .
              		?medCridSymbol a turbo:TURBO_0000562 .
              		
              		?drugPrescript obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?drugPrescript .
              		?medCridSymbol obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?medCridSymbol .
              }
          }
          """
        
        val datasetCheck4: String = """
          ASK
          {
              GRAPH pmbb:postExpansionCheck
              {
                ?dataset a obo:IAO_0000100 .
                ?dataset dc11:title 'bmiAndHeightWeight.csv'^^xsd:string .
                pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
              
                ?encounter a obo:OGMS_0000097 .
                ?encounter obo:RO_0002234 ?BMI .
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:OBI_0001938 ?BMIvalspec .
            		?BMIvalspec a obo:OBI_0001933 .
            		
            		?heightValSpec rdf:type obo:OBI_0001931 .
          	    ?heightAssay rdf:type turbo:TURBO_0001511 ;
          	                 obo:BFO_0000050 ?encounter ;
          	                 obo:OBI_0000299 ?heightDatum .
          	    ?heightDatum rdf:type obo:IAO_0000408 ;
          	                 obo:OBI_0001938 ?heightValSpec .
          	    ?weightAssay rdf:type obo:OBI_0000445 ;
          	                 obo:BFO_0000050 ?encounter ;
          	                 obo:OBI_0000299 ?weightDatum .
          	    ?weightDatum rdf:type obo:IAO_0000414 ;
          	                 obo:OBI_0001938 ?weightValSpec .
          	    ?weightValSpec rdf:type obo:OBI_0001931 .
            	                   
          	    ?BMI obo:BFO_0000050 ?dataset .   
          	    ?weightDatum obo:BFO_0000050 ?dataset .
          	    ?heightDatum obo:BFO_0000050 ?dataset .            
          	    ?dataset obo:BFO_0000051 ?BMI .
          		  ?dataset obo:BFO_0000051 ?weightDatum .
          		  ?dataset obo:BFO_0000051 ?heightDatum .
              }
          }
          """
        
        val datasetCheck5: String = """
          ASK
          {
              GRAPH pmbb:postExpansionCheck
              {
                ?dataset a obo:IAO_0000100 .
                ?dataset dc11:title 'date.csv' .
                pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
              
                ?encounter a obo:OGMS_0000097 .
                ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
            		?encDate obo:IAO_0000136 ?encStart .
            		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
            		?encStart obo:RO_0002223 ?encounter .
            		
            		?dataset obo:BFO_0000051 ?encDate .
            		?encDate obo:BFO_0000050 ?dataset .
              }
          }
          """
        
        val thereShouldOnlyBeOneEncounter: String = """
          Select ?enc Where
          {
              Graph pmbb:postExpansionCheck{
              ?enc a obo:OGMS_0000097 .}
          }
          """
        
        val thereShouldBeFiveDatasets: String = """
          Select ?dataset Where
          {
              Graph pmbb:postExpansionCheck {
              ?dataset a obo:IAO_0000100 .}
          }
          """
        
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + thereShouldOnlyBeOneEncounter, "enc").size should be (1)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + thereShouldBeFiveDatasets, "dataset").size should be (5)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareDiagnosis).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareMedications).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareEncounterDate).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSymbolAndRegistry).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + datasetCheck1).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + datasetCheck2).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + datasetCheck3).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + datasetCheck4).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + datasetCheck5).get should be (true)
    }*/
}