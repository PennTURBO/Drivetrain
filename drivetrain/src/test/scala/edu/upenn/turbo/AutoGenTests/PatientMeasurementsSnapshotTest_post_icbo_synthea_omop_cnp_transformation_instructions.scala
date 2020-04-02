package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class PatientMeasurementsSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
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
<http://api.stardog.com/measurement_1> rdf:type <http://api.stardog.com/measurement> .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_source_concept_id> "914375085"^^xsd:Integer .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_datetime> "2020-04-02T13:50:51.157"^^xsd:Date .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_id> "463207488"^^xsd:Integer .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_source_value> "755924451abc"^^xsd:String .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#value_as_number> "33737254abc"^^xsd:String .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#vocabulary_id> "774008303abc"^^xsd:String .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/MeasClassList_1> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/IAO_0000109> .
<https://github.com/PennTURBO/Drivetrain/MeasClassList_1> <http://transformunify.org/ontologies/TURBO_0010147> "914375085"^^xsd:Integer .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/PatientMeasurements")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/IAO_0000219","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/IAO_0000219","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/BFO_0000051","http://transformunify.org/ontologies/TURBO_0010094",
"http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
"http://purl.obolibrary.org/obo/IAO_0000004","http://purl.obolibrary.org/obo/IAO_0000581",
"http://purl.obolibrary.org/obo/IAO_0000142","http://purl.obolibrary.org/obo/OBI_0001938",
"http://purl.obolibrary.org/obo/OBI_0002135","http://www.w3.org/2002/07/owl#annotatedSource",
"http://www.w3.org/2002/07/owl#annotatedSource","http://www.w3.org/2002/07/owl#annotatedProperty",
"http://www.w3.org/2002/07/owl#annotatedProperty","http://www.w3.org/2002/07/owl#annotatedTarget",
"http://www.w3.org/2002/07/owl#annotatedTarget","http://www.geneontology.org/formats/oboInOwl#hasDbXref",
"http://www.geneontology.org/formats/oboInOwl#hasDbXref"
)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }}