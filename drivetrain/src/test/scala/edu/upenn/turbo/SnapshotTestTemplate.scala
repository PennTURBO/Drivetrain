package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import java.io.File
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.collection.mutable.HashMap
import java.util.Arrays
import org.slf4j.LoggerFactory

case class SnapshotTestData(
    val instructionSetFile: String,
    val iriCreationSeed: String,
    val updateSpecificationURI: String,
    val allInputTriples: String,
    val minimumInputTriples: String,
    val allOutputTriples: String,
    val minimumOutputTriples: String
)

class SnapshotTest extends FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
  
val logger = LoggerFactory.getLogger(getClass)  
val clearTestingRepositoryAfterRun: Boolean = false

implicit val formats = DefaultFormats

var testsToRun: Array[File] = null
var allTests: Array[File] = null
var testSearchString: Option[String] = None
var graphDBMaterials: TurboGraphConnection = null
  
    override def run(testName: Option[String], args: org.scalatest.Args): Status =
    {
        if (args.configMap.contains("findTest")) testSearchString = Option(args.configMap.getRequired[String]("findTest"))
        findTestsFromSearchString(testSearchString)
        val superRes = super.run(testName, args)
        superRes
    }

    before
    {
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    def findTestsFromSearchString(searchStringOptional: Option[String])
    {
        if (!searchStringOptional.isDefined) 
        {
            logger.info("No specific test search given; running all Snapshot tests")
            testsToRun = allTests
        }
        else
        {
            val searchString: String = searchStringOptional.get
            logger.info("Searching for Snapshot tests containing string " + searchString)
            val regexSearch: scala.util.matching.Regex = searchString.toLowerCase().r
            var testsToRunBuffer = new ArrayBuffer[File]
            for (singleTest <- allTests) if (regexSearch.findFirstIn(singleTest.toString.toLowerCase()) != None) testsToRunBuffer += singleTest
            testsToRun = testsToRunBuffer.toArray
        }
        logger.info("The following tests will be run (list size:" + testsToRun.size+")")
        testsToRun.foreach{ test => logger.info(test.toString)}
        println()
    }
    
    def getAllTests()
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraph()
        Globals.cxn = graphDBMaterials.getConnection()
        Globals.gmCxn = graphDBMaterials.getGmConnection()
        
        RunDrivetrainProcess.setInputNamedGraphsCache(false)
        
        val directory = new File("src//test//scala//edu//upenn//turbo//AutoGenTests")
        allTests = directory.listFiles.filter(_.isFile).sorted
    }
    
    getAllTests()
    var prevInstructionSet = ""
    allTests.foreach{ testName =>

        //for ((k,v) <- fileMap) println("key: " + k + " value: " + v)
        val snapshotTestData = parse(io.Source.fromFile(testName).mkString).extract[SnapshotTestData]
        
        test(s"all fields test on $testName")
        {
            assume(testsToRun.contains(testName))
            RunDrivetrainProcess.setGlobalUUID(snapshotTestData.iriCreationSeed)
            
            if (snapshotTestData.instructionSetFile != prevInstructionSet) 
            {
                DrivetrainDriver.updateModel(graphDBMaterials, snapshotTestData.instructionSetFile + ".tis")
                OntologyLoader.addOntologyFromUrl(Globals.gmCxn)
                prevInstructionSet = snapshotTestData.instructionSetFile
            }
            
            val inputTriples = snapshotTestData.allInputTriples
            SparqlUpdater.updateSparql(Globals.cxn, s"INSERT DATA { $inputTriples \n}")
            val queryResMap = RunDrivetrainProcess.runProcess(snapshotTestData.updateSpecificationURI)
            val outputNamedGraph = Utilities.checkAndConvertPropertiesReferenceToNamedGraph(queryResMap(snapshotTestData.updateSpecificationURI).defaultOutputGraph)
            val outputTriples = snapshotTestData.allOutputTriples.split("\n")
            
            val getAllTriples: String = "SELECT * WHERE {GRAPH <"+outputNamedGraph+"> {?s ?p ?o .}}"
            val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, getAllTriples, Array("s", "p", "o"))
            
            var resultsArray = new ArrayBuffer[String]
            for (index <- 0 to result.size-1) 
            {
                if (!result(index)(2).isInstanceOf[org.eclipse.rdf4j.model.Literal]) resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
                else resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
            }
            Utilities.checkStringArraysForEquivalency(outputTriples, resultsArray.toArray)("equivalent").asInstanceOf[String] should be ("true")

        }
        test(s"minimum fields test on $testName")
        {
            assume(testsToRun.contains(testName))
            RunDrivetrainProcess.setGlobalUUID(snapshotTestData.iriCreationSeed)
            
            if (snapshotTestData.instructionSetFile != prevInstructionSet) 
            {
                DrivetrainDriver.updateModel(graphDBMaterials, snapshotTestData.instructionSetFile + ".tis")
                OntologyLoader.addOntologyFromUrl(Globals.gmCxn)
                prevInstructionSet = snapshotTestData.instructionSetFile
            }
            
            val inputTriples = snapshotTestData.minimumInputTriples
            SparqlUpdater.updateSparql(Globals.cxn, s"INSERT DATA { $inputTriples \n}")
            val queryResMap = RunDrivetrainProcess.runProcess(snapshotTestData.updateSpecificationURI)
            val outputNamedGraph = Utilities.checkAndConvertPropertiesReferenceToNamedGraph(queryResMap(snapshotTestData.updateSpecificationURI).defaultOutputGraph)
            val outputTriples = snapshotTestData.minimumOutputTriples.split("\n")
            
            val getAllTriples: String = "SELECT * WHERE {GRAPH <"+outputNamedGraph+"> {?s ?p ?o .}}"
            val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, getAllTriples, Array("s", "p", "o"))
            
            var resultsArray = new ArrayBuffer[String]
            for (index <- 0 to result.size-1) 
            {
                if (!result(index)(2).isInstanceOf[org.eclipse.rdf4j.model.Literal]) resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
                else resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
            }
            Utilities.checkStringArraysForEquivalency(outputTriples, resultsArray.toArray)("equivalent").asInstanceOf[String] should be ("true")
        }
    }
}