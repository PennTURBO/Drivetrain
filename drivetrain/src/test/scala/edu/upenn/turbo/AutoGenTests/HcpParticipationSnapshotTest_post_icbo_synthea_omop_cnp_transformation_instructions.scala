package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class HcpParticipationSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    RunDrivetrainProcess.setInputNamedGraphsCache(false)
}

override def afterAll()
{
    ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
}

before
{
    helper.deleteAllTriplesInDatabase(cxn)
}


test("all fields test")
{

val insertInputDataset = 
"""
            INSERT DATA {
                   # Required triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://api.stardog.com/procedure_occurrence_1> <http://api.stardog.com/procedure_occurrence#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/procedure_occurrence_1> rdf:type <http://api.stardog.com/procedure_occurrence> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000093> .
<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/person_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://api.stardog.com/procedure_occurrence_1> rdf:type <http://api.stardog.com/procedure_occurrence> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<http://api.stardog.com/procedure_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_1> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://api.stardog.com/person_1> rdf:type <http://api.stardog.com/person> .
<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/HcpParticipation")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://transformunify.org/ontologies/TURBO_0010113",
"http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/IAO_0000219","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/BFO_0000051","http://transformunify.org/ontologies/TURBO_0010094",
"http://transformunify.org/ontologies/TURBO_0010094","http://purl.obolibrary.org/obo/BFO_0000055",
"http://purl.obolibrary.org/obo/RO_0000087","http://www.w3.org/2002/07/owl#annotatedSource",
"http://www.w3.org/2002/07/owl#annotatedProperty","http://www.w3.org/2002/07/owl#annotatedTarget",
"http://www.geneontology.org/formats/oboInOwl#hasDbXref"
)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }}