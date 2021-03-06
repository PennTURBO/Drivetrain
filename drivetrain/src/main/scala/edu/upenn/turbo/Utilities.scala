package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.util.Properties
import java.io.FileInputStream
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.util.Calendar
import java.text.SimpleDateFormat
import java.net.URL
import java.net.ConnectException
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.OpenRDFException
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.query.BooleanQuery
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.Reader
import java.io.BufferedInputStream
import java.io.InputStream
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.Model
import org.scalatest._
import java.math.BigInteger

/**
 * The Utilities class contains methods whose functionality is repeatedly used by some component of the Drivetrain application. A few of the methods
 * in this class may be used by the Drivetrain test suite as well. The functions are here to be used and prevent repetitive development.
 */
//change name to something more relevant to the methods inside the class
object Utilities extends Matchers {
  val logger = LoggerFactory.getLogger(getClass)

  /**
   * Deletes all triples in the entire database, including all named graphs.
   */
  def deleteAllTriplesInDatabase(cxn: RepositoryConnection) {
    val deleteAll: String = "DELETE {?s ?p ?o} WHERE {?s ?p ?o .} "
    val tupleDelete = cxn.prepareUpdate(QueryLanguage.SPARQL, deleteAll)
    tupleDelete.execute()
  }

  /**
   * Deletes all triples in a specified named graph.
   */
  def clearNamedGraph(cxn: RepositoryConnection, namedGraph: String) {
    //val deleteAll: String = "DELETE { GRAPH <" + namedGraph + "> { ?s ?p ?o }} WHERE {?s ?p ?o .}"

    //Clear Graph seems to give better performance than deleting a triple pattern
    val deleteAll: String = "CLEAR GRAPH <" + namedGraph + ">"
    SparqlUpdater.updateSparql(cxn, deleteAll)
  }

  def genTurboIRI(seed: String = ""): String =
    {
      if (seed == "") Globals.defaultPrefix + UUID.randomUUID().toString().replaceAll("-", "")
      else Globals.defaultPrefix + seed.hashCode()
    }

  /**
   * Parse string URI of input to remove the prefix. It's mostly meant for URI's which are composed of a prefix and then a UUID. If there are '/' characters
   * in the input that are not part of the prefix, this method may not work as expected.
   *
   * @return a string which contains the input string, minus everything before and including the last '/' character in the input string
   */
  def getPostfixfromURI(someObject: String): String =
    {
      var uuidOfSomeObject: String = ""
      for (i <- 0 to someObject.length - 1) {
        uuidOfSomeObject += someObject.charAt(i).toString()
        if (someObject.charAt(i) == '/') uuidOfSomeObject = ""
      }
      uuidOfSomeObject
    }

  /**
   * Overloaded method which creates a string representation of the current time in 'yyyyMMdd' format, using a default separator of "".
   *
   * @return a string representation of the current time with default separator
   */
  def getCurrentTimestamp(): String =
    {
      val date = Calendar.getInstance()
      var hour: String = date.get(Calendar.HOUR_OF_DAY).toString
      var minute: String = date.get(Calendar.MINUTE).toString
      var second: String = date.get(Calendar.SECOND).toString
      val format = new SimpleDateFormat("yyyyMMdd")
      val thisDate = format.format(Calendar.getInstance().getTime())
      if (hour.length == 1) hour = "0" + hour
      if (minute.length == 1) minute = "0" + minute
      if (second.length == 1) second = "0" + second
      val datetimeStamp: String = thisDate.toString + hour.toString + minute.toString + second.toString
      /*println("PRINTING DATESTAMP")
        println(datetimeStamp)
        println(thisDate)*/
      datetimeStamp
    }

  /**
   * Overloaded method which creates a string representation of the current time in 'yyyyMMdd' format, using a custom separator received as input.
   *
   * @return a string representation of the current time with custom separator
   */
  def getCurrentTimestamp(separator: String): String =
    {
      val date = Calendar.getInstance()
      var hour: String = date.get(Calendar.HOUR_OF_DAY).toString
      var minute: String = date.get(Calendar.MINUTE).toString
      var second: String = date.get(Calendar.SECOND).toString
      val format = new SimpleDateFormat("yyyy" + separator + "MM" + separator + "dd")
      val thisDate = format.format(Calendar.getInstance().getTime())
      if (hour.length == 1) hour = "0" + hour
      if (minute.length == 1) minute = "0" + minute
      if (second.length == 1) second = "0" + second
      val datetimeStamp: String = thisDate.toString + separator + hour.toString + separator + minute.toString + separator + second.toString
      //println("PRINTING DATESTAMP")
      //println(datetimeStamp)
      datetimeStamp
    }

  def removeAngleBracketsFromString(input: String): String =
    {
      var result: String = input
      if (result.length > 0) {
        if (result.charAt(0) == '<' && result.charAt(result.length - 1) == '>') {
          result = result.substring(1, result.length - 1)
        }
      }
      result
    }

  def removeQuotesFromString(input: String): String =
    {
      var result: String = input
      if (result.length > 0) {
        if (result.charAt(0) == '"' && result.charAt(result.length - 1) == '"') {
          result = result.substring(1, result.length - 1)
        }
      }
      result
    }

  /**
   * Moves all RDF data in a specified Graph DB repository to another specified Graph DB repository.  This method is somewhat inefficient.
   */
  def moveDataFromOneRepositoryToAnother(from: String, to: RepositoryConnection) {
    val query: String = "INSERT DATA {CONSTRUCT ?s ?p ?o FROM <" + from + "> WHERE 	{?s ?p ?o .}}"
    val triplesToMove = to.prepareGraphQuery(QueryLanguage.SPARQL, query)
  }

  /**
   * Moves all RDF data in a specified named graph to another specified named graph.
   */
  def moveDataFromOneNamedGraphToAnother(cxn: RepositoryConnection, fromGraph: String, toGraph: String) {
    val moveTriples: String =
      """
               ADD <""" + fromGraph + """> TO <""" + toGraph + """>
            """
    SparqlUpdater.updateSparql(cxn, moveTriples)
  }

  /**
   * Overloaded method which prints all triples in all named graphs to the console.
   */
  def printAllInDatabase(cxn: RepositoryConnection) {
    val queryAll: String = "SELECT ?s ?p ?o WHERE {?s ?p ?o .}"
    val results = SparqlUpdater.querySparqlAndUnpackTuple(cxn, queryAll, Array("s", "p", "o"))
    logger.info("Number of statements: " + results.size.toString)
    for (result <- results) {
      val result0 = "<" + result(0).toString + ">"
      val result1 = "<" + result(1).toString + ">"
      var result2 = result(2).toString
      if (result2.charAt(0) != '"') result2 = "<" + result2 + ">"
      println(result0 + " " + result1 + " " + result2 + " .")
    }
  }

  /**
   * Overloaded method which prints all triples in a single named graph given as input to the console.
   */
  def printAllInNamedGraph(cxn: RepositoryConnection, namedGraph: String) {
    val queryAll: String = "SELECT ?s ?p ?o WHERE { GRAPH <" + namedGraph + "> {?s ?p ?o .}}"
    val results = SparqlUpdater.querySparqlAndUnpackTuple(cxn, queryAll, Array("s", "p", "o"))
    logger.info("Number of statements: " + results.size.toString)
    for (result <- results) {
      val result0 = "<" + result(0).toString + ">"
      val result1 = "<" + result(1).toString + ">"
      var result2 = result(2).toString
      if (result2.charAt(0) != '"') result2 = "<" + result2 + ">"
      println(result0 + " " + result1 + " " + result2 + " .")
    }
  }

  /**
   * Receives string variables describing the state of the error and adds them to a hashmap which is processed by
   * the logErrorMessage method.  This method was specifically designed to work with the DrivetrainSparqlChecks
   * class to perform data validation on the contents of the triplestore.
   */
  def writeErrorLog(process: String, error: String, variables: String = "") {
    val map: HashMap[String, String] = new HashMap[String, String]
    map += "process" -> process
    map += "cause" -> error
    map += "variables" -> variables
    logErrorMessage(map)
  }

  /**
   * Writes an error message to the file specified in the TURBO property 'errorLogFile' based on the information
   * in the hashmap received as input.
   */
  def logErrorMessage(map: HashMap[String, String]) {
    val file: File = new File(retrievePropertyFromFile("errorLogFile"))
    val fw: FileWriter = new FileWriter(file, true)
    val process: Option[String] = map.get("process")
    val cause: Option[String] = map.get("cause")
    val dataset: Option[String] = map.get("dataset")
    val result: Option[String] = map.get("result")
    val variables: Option[String] = map.get("variables")

    var write: String =
      "\n" +
        "New Error Log " + getCurrentTimestamp() + " \n"
    if (process != None) write += "Occurred during process: " + process.get + " \n"
    if (cause != None) write += "Caused by: " + cause.get + " \n"
    //if (variables != None && variables != "") write += "Variables returned: " + variables.get + " \n"
    if (dataset != None) write += "Error found in dataset: " + dataset.get + " \n"
    if (result != None) write += "Resulting action taken: " + result.get + " \n"

    fw.write(write)
    fw.flush()
    fw.close()
  }

  /**
   * Launches a boolean query to determine whether there is one or more triples in the named graph received as input.
   *
   * @return a Boolean true if one or more triples was found, false if otherwise
   */
  def isThereDataInNamedGraph(cxn: RepositoryConnection, namedGraph: IRI): Boolean =
    {
      val sparql: String = "ASK {GRAPH <" + namedGraph + "> {?s ?p ?o .}}"
      SparqlUpdater.querySparqlBoolean(cxn, sparql).get
    }

  /**
   * Overloaded method to load triples from a file into the triplestore. Triples are loaded into default named graph.
   */
  def loadDataFromFile(cxn: RepositoryConnection, resource: String, dataFormat: RDFFormat) {
    cxn.begin()
    val file: File = new File(resource)
    if (!(file.exists)) logger.info("Specified file " + resource + " does not exist in the necessary location.")
    val is: InputStream = new FileInputStream(resource)
    val reader: Reader = new InputStreamReader(new BufferedInputStream(is))
    logger.info("adding " + resource)
    try cxn.add(reader, "", dataFormat)
    finally reader.close()
    cxn.commit()
  }

  /**
   * Overloaded method to load triples from a file into the triplestore. Triples are loaded into named graph received as input.
   */
  def loadDataFromFile(cxn: RepositoryConnection, resource: String, dataFormat: RDFFormat, namedGraph: String) {
    cxn.begin()
    val f: ValueFactory = cxn.getValueFactory()
    val file: File = new File(resource)
    if (!(file.exists)) logger.info("Specified file " + resource + " does not exist in the necessary location.")
    val is: InputStream = new FileInputStream(resource)
    val reader: Reader = new InputStreamReader(new BufferedInputStream(is))
    logger.info("adding " + resource)
    try cxn.add(reader, "", dataFormat, f.createIRI(namedGraph))
    finally reader.close()
    cxn.commit()
  }

  /**
   * Searches through a specified named graph to find symmetrical properties as defined by the TURBO ontology, and adds
   * the corresponding inverse triple to the same named graph as the original triple. Note that default named graph argument
   * "?g" specifies that all named graphs should be searched for symmetrical properties, with the exception of ontology named
   * graphs which are FILTERED.
   */
  def applySymmetricalProperties(cxn: RepositoryConnection, namedGraph: String = "?g") {
    var namedGraph_1 = ""
    if (namedGraph != "?g") namedGraph_1 = "<" + namedGraph + ">"
    else namedGraph_1 = namedGraph

    val insert: String = """
        INSERT {
            graph """ + namedGraph_1 + """ 
              {
                    ?o ?p ?s .
        }} 
        WHERE 
        {
            graph """ + namedGraph_1 + """ 
              {
                    ?s ?p ?o . 
              }
        graph <""" + Globals.ontologyURL + """> {
            ?p a <http://www.w3.org/2002/07/owl#SymmetricProperty> .
            }
            FILTER (""" + namedGraph_1 + """ != <""" + Globals.ontologyURL + """>)
        }
        """

    SparqlUpdater.updateSparql(cxn, insert)
  }

  /**
   * This method executes SPARQL to search through a specified named graph for nodes which do not have labels. It then searches the TURBO
   * ontology to find the label of that node's type and concatanates this label with the first 4 digits of the node's UUID postfix to create
   * an instance label, which is applied to the node as a single triple using the "rdfs:label" predicate in the same named graph as the node.
   * Note that labels are not guaranteed to be unique, and it is possible that a node may be assigned multiple labels if its type has multiple
   * labels in the ontology, or if it itself has multiple types.  Also note that default named graph argument "?g" specifies that all named
   * graphs should be searched for symmetrical properties, with the exception of ontology named graphs which are FILTERED.
   */
  def addLabelsToEverything(cxn: RepositoryConnection, namedGraph: String = "?g") {
    var namedGraph_1 = ""
    if (namedGraph != "?g") namedGraph_1 = "<" + namedGraph + ">"
    else namedGraph_1 = namedGraph

    val addLabelsToEverything: String = """
          Insert 
          {
              Graph """ + namedGraph_1 + """
              {
                  ?node rdfs:label ?label .
              }
          }
          Where
          {
              Graph """ + namedGraph_1 + """
              {
                  ?node a ?nodetype .
              }
              Graph <""" + Globals.ontologyURL + """>
              {
                  ?nodetype rdfs:label ?ontologylabel .
              }
              
              Minus
              {
                  ?node rdfs:label ?somelabel .
              }
              FILTER (?nodetype != turbo:TURBO_0000506)
              FILTER (?nodetype != turbo:TURBO_0000513)
              FILTER (?nodetype != turbo:TURBO_0000543)
              FILTER (""" + namedGraph_1 + """ != <""" + Globals.ontologyURL + """>)
              FILTER (""" + namedGraph_1 + """ != pmbb:ICD9Ontology)
              FILTER (""" + namedGraph_1 + """ != pmbb:ICD10Ontology)
              FILTER (""" + namedGraph_1 + """ != pmbb:mondoOntology)
              BIND (CONCAT(REPLACE(?ontologylabel, " ", ""), "/", substr(str(?node), 38, 4)) AS ?label)
          }
          """

    SparqlUpdater.updateSparql(cxn, addLabelsToEverything)
  }

  /**
   * Reads a Java properties file and searches for a specific property given by the propertyID string variable. Default file input
   * is the TURBO properties file. This is the main method responsible for pulling in property values from the TURBO properties file.
   *
   * @return a String holding the value of the requested property
   */
  def retrievePropertyFromFile(propertyID: String, file: String = "..//turbo_properties.properties"): String =
    {
      val input: FileInputStream = new FileInputStream(file)
      val props: Properties = new Properties()
      props.load(input)
      input.close()
      val property = props.getProperty(propertyID)
      assert(property != null, s"Could not find property $propertyID")
      property
    }

  def retrieveUriPropertyFromFile(propertyID: String, file: String = "..//turbo_properties.properties"): String =
    {
      var property = retrievePropertyFromFile(propertyID, file)
      assert(property.contains("/"), s"Invalid URI for property $propertyID")
      if (propertyID == "defaultPrefix" && !property.endsWith("/")) property += "/"
      property
    }

  /**
   * Creates an MD5 representation of a given input string. This is guaranteed to be unique to that string.
   *
   * @return a String holding the MD5 representation
   */
  def md5Hash(text: String): String =
    {
      java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
    }

  /**
   * This method's creation was necessitated by the fact that some labels in the TURBO ontology are represented in a format other than
   * rdf:langString. In order to be properly read by the addLabelsToEverything method, labels must be in the rdf:langString format. This
   * method searches through the TURBO ontology for all non-rdf:langString-formatted labels and adds a string label with the same value
   * as the original label back into the ontology named graph.
   */
  def addStringLabelsToOntology(cxn: RepositoryConnection) {
    val updateQuery: String = """
          Insert 
          {
              Graph <""" + Globals.ontologyURL + """>
              {
                  ?class rdfs:label ?stringLabel .
              }
          }
          Where
          {
               Graph <""" + Globals.ontologyURL + """>
               {
                   ?class rdfs:label ?label .
                   FILTER (datatype(?label) = rdf:langString)
               }
               BIND (str(?label) AS ?stringLabel)   
          }
          """

    SparqlUpdater.updateSparql(cxn, updateQuery)
  }

  //stores queries completed from generate named graphs methods
  val completedQueriesMap = new HashMap[String, ArrayBuffer[String]]

  /**
   * Generates list of all Shortcut named graphs by issuing a Sparql command to retrieve all named graphs which start with "Shortcuts"
   *
   * @return a list representation of all shortcut named graphs for expansion
   */
  def generateNamedGraphsListFromPrefix(cxn: RepositoryConnection, graphsPrefix: String, whereClause: String): ArrayBuffer[String] =
    {
      assert(whereClause.contains("GRAPH") && whereClause.contains("WHERE"))

      val graphVar = "g"
      var filterClause = "filter "
      if (graphsPrefix.charAt(graphsPrefix.size - 1) == '_') filterClause += s"(strStarts(str(?$graphVar), str(<$graphsPrefix>)))"
      else filterClause += s"(?$graphVar = <$graphsPrefix>)"
      // remove last bracket of where clause
      val whereClauseNoFinalBracket = whereClause.substring(0, whereClause.length - 1)
      // replace input named graph with sparql variable
      val graphsPrefixWithBrackets = "<" + graphsPrefix + ">"
      val replacementGraphVariable = s"?$graphVar"
      val whereClauseWithGraphReplacement = whereClauseNoFinalBracket.replaceAll(graphsPrefixWithBrackets, replacementGraphVariable)

      val getGraphs: String = s"""
        select distinct ?$graphVar
        $whereClauseWithGraphReplacement
        $filterClause
        }"""
      //println(getGraphs)
      if (completedQueriesMap.contains(getGraphs)) completedQueriesMap(getGraphs)
      else {
        val res = SparqlUpdater.querySparqlAndUnpackTuple(cxn, getGraphs, graphVar)
        if (res.size > 0) completedQueriesMap += getGraphs -> res
        res
      }
    }

  def generateSimpleNamedGraphsListFromPrefix(cxn: RepositoryConnection, graphsPrefix: String, useCache: Boolean = true): ArrayBuffer[String] =
    {
      val graphVar = "g"
      var filterClause = "filter "
      if (graphsPrefix.charAt(graphsPrefix.size - 1) == '_') {
        filterClause += s"(strStarts(str(?$graphVar), str(<$graphsPrefix>)))"
        val getGraphs: String = s"""
            select distinct ?$graphVar
            Where { GRAPH ?$graphVar { ?s ?p ?o . }
            $filterClause
            }"""
        //println(getGraphs)
        if (useCache) {
          if (completedQueriesMap.contains(getGraphs)) completedQueriesMap(getGraphs)
          else {
            val res = SparqlUpdater.querySparqlAndUnpackTuple(cxn, getGraphs, graphVar)
            if (res.size > 0) completedQueriesMap += getGraphs -> res
            res
          }
        } else SparqlUpdater.querySparqlAndUnpackTuple(cxn, getGraphs, graphVar)
      } else {
        val ask: String = s"ASK {Graph <$graphsPrefix> { ?s ?p ?o .}}"
        //logger.info(ask)
        val bool = SparqlUpdater.querySparqlBoolean(cxn, ask)
        if (bool.get) ArrayBuffer(graphsPrefix)
        else ArrayBuffer()
      }
    }

  def checkOrderedStringArraysForEquivalency(arr1: Array[String], arr2: Array[String]): Boolean =
    {
      var boolToReturn = true
      val newArr1 = new ArrayBuffer[String]
      val newArr2 = new ArrayBuffer[String]
      for (a <- arr1) if (a.length() != 0) newArr1 += a
      for (a <- arr2) if (a.length() != 0) newArr2 += a
      if (newArr1.size != newArr2.size) {
        logger.info("arrays are not the same size")
        checkStringArraysForEquivalency(arr1, arr2)
        boolToReturn = false
      } else {
        for (a <- 0 to newArr1.size - 1) {
          try {
            newArr1(a) should be(newArr2(a))
          } catch {
            case e: AssertionError => {
              logger.info(e.toString)
              boolToReturn = false
            }
          }
        }
      }
      boolToReturn
    }

  //These 2 globals are associated with the two methods below
  private var nonMatchesArr1: ArrayBuffer[String] = new ArrayBuffer[String]
  private var nonMatchesArr2: ArrayBuffer[String] = new ArrayBuffer[String]

  /**
   * This method was developed for use with the suite of Expansion tests, in order to determine if the predicates created by the Expander
   * match a set of expected predicates specified in the tests. Sorts input arrays and passes them to findSortedArrayDifferences to be
   * recursively analyzed.
   *
   * @return a Boolean true if sorted arrays are equivalent, false otherwise
   */
  def checkStringArraysForEquivalency(arr1: Array[String], arr2: Array[String]): HashMap[String, Object] =
    {
      logger.info("about to check string arrays for equivalency")

      logger.info("size of expected predicates list: " + arr1.size)
      logger.info("size of actual results received: " + arr2.size)

      //first, sort each array in alphabetical order
      scala.util.Sorting.quickSort(arr1)
      scala.util.Sorting.quickSort(arr2)
      logger.info("finished sorting arrays")
      //search line by line for differences in array
      findSortedArrayDifferences(arr1, 0, arr2, 0)

      logger.info("nonMatches expected predicates: " + nonMatchesArr1.size)
      for (a <- nonMatchesArr1) logger.info(a)
      logger.info("nonMatches actual results: " + nonMatchesArr2.size)
      for (a <- nonMatchesArr2) logger.info(a)

      var boolToReturn: Boolean = false
      if (nonMatchesArr1.size == 0 && nonMatchesArr2.size == 0) boolToReturn = true

      var res1return = nonMatchesArr1
      var res2return = nonMatchesArr2

      nonMatchesArr1 = new ArrayBuffer[String]
      nonMatchesArr2 = new ArrayBuffer[String]

      HashMap("results" -> Array(res1return, res2return), "equivalent" -> boolToReturn.toString)
    }

  /**
   * This is a recursive method which checks two given arrays of strings against each other and returns the differences.
   * Keeps track of differences using global variables nonMatchesArr1 and nonMatchesArr2.
   *
   * Do you even recurse bro? (I hope you do it better than me)
   */
  private def findSortedArrayDifferences(arr1: Array[String], index1: Int, arr2: Array[String], index2: Int) {
    if (arr1.size - 1 >= index1 && arr2.size - 1 >= index2) {
      // blank lines should be ignored
      if (arr1(index1).size == 0) findSortedArrayDifferences(arr1, index1 + 1, arr2, index2)
      else if (arr2(index2).size == 0) findSortedArrayDifferences(arr1, index1, arr2, index2 + 1)
      else {
        val compare: Int = arr1(index1).replaceAll("\\r\\n", "").replaceAll("\\r", "").compareTo(arr2(index2).replaceAll("\\r\\n", "").replaceAll("\\r", ""))
        if (compare == 0) findSortedArrayDifferences(arr1, index1 + 1, arr2, index2 + 1)
        else if (compare > 0) {
          nonMatchesArr2 += arr2(index2)
          findSortedArrayDifferences(arr1, index1, arr2, index2 + 1)
        } else if (compare < 0) {
          nonMatchesArr1 += arr1(index1)
          findSortedArrayDifferences(arr1, index1 + 1, arr2, index2)
        }
      }
    } else {
      if (arr1.size - 1 < index1) {
        for (a <- index2 to arr2.size - 1) if (arr2(a).size > 0) nonMatchesArr2 += arr2(a)
      }
      if (arr2.size - 1 < index2) {
        for (a <- index1 to arr1.size - 1) if (arr1(a).size > 0) nonMatchesArr1 += arr1(a)
      }
    }
  }

  def convertSparqlResultToStringArray(sparqlResult: ArrayBuffer[ArrayBuffer[Value]]): ArrayBuffer[ArrayBuffer[String]] =
    {
      var arrToReturn: ArrayBuffer[ArrayBuffer[String]] = new ArrayBuffer[ArrayBuffer[String]]
      for (a <- sparqlResult) {
        var singleLine: ArrayBuffer[String] = new ArrayBuffer[String]
        for (b <- a) singleLine += b.toString
        arrToReturn += singleLine
      }
      arrToReturn
    }

  def removeInferredStatements(cxn: RepositoryConnection) {
    var model: Model = new LinkedHashModel()
    val f: ValueFactory = cxn.getValueFactory()
    val select: String =
      """
            Select * FROM <http://www.ontotext.com/implicit> Where {?s ?p ?o .}
          """
    val result: ArrayBuffer[ArrayBuffer[org.eclipse.rdf4j.model.Value]] = SparqlUpdater.querySparqlAndUnpackTuple(cxn, select, Array("s", "p", "o"))
    for (row <- result) {
      model.add(row(0).asInstanceOf[IRI], row(1).asInstanceOf[IRI], row(2).asInstanceOf[IRI])
    }
    cxn.remove(model, f.createIRI("http://www.ontotext.com/implicit"))
  }

  def countTriplesInDatabase(cxn: RepositoryConnection): BigInteger =
    {
      val query: String = """
          select (count (?s) as ?tripcount) where
          {
              ?s ?p ?o .
          }
          """
      new BigInteger(SparqlUpdater.querySparqlAndUnpackTuple(cxn, query, "tripcount")(0).split("\"")(1))
    }

  def countTriplesInNamedGraph(cxn: RepositoryConnection, namedGraph: String): Int =
    {
      val query: String = s"""
          select (count (?s) as ?tripcount) where
          {
              Graph <$namedGraph>
              {
                  ?s ?p ?o .
              }
          }
          """
      SparqlUpdater.querySparqlAndUnpackTuple(cxn, query, "tripcount")(0).split("\"")(1).toInt
    }

  def getDatasetNames(cxn: RepositoryConnection): ArrayBuffer[String] =
    {
      val query: String = """
          select ?dsTitle where
          {
              ?ds a obo:IAO_0000100 .
              ?ds dc11:title ?dsTitle .
          }
          """
      SparqlUpdater.querySparqlAndUnpackTuple(cxn, query, "dsTitle")
    }

  def convertTypeToSparqlVariable(input: org.eclipse.rdf4j.model.Value, withQuestionMark: Boolean): String =
    {
      convertTypeToSparqlVariable(input.toString, withQuestionMark)
    }

  def convertTypeToSparqlVariable(input: String, withQuestionMark: Boolean = true): String =
    {
      if (input != "" && input != null) {
        val splitTypeToVar = input.split("\\/")
        var qm = ""
        if (withQuestionMark) qm = "?"
        qm + splitTypeToVar(splitTypeToVar.size - 1).replaceAll("\\/", "_").replaceAll("\\:", "").replaceAll("\\.", "_")
          .replaceAll("\\>", "").replaceAll("\\<", "").replaceAll("\\#", "_")
      } else ""
    }

  def validateURI(uri: String) {
    val requiredCharacters: ArrayBuffer[Char] = ArrayBuffer(':')
    val illegalCharacters: ArrayBuffer[Char] = ArrayBuffer('"', ' ', '<', '>')
    for (char <- requiredCharacters) assert(uri.contains(char), s"The URI $uri is missing required character $char. Make sure this is actually a URI.")
    for (char <- illegalCharacters) assert(!uri.contains(char), s"The URI $uri contains illegal character $char. Make sure this is actually a URI.")
  }

  def buildProcessMetaQuery(process: String, inputNamedGraphs: Array[String] = Array(Globals.expandedNamedGraph)): String =
    {
      var str = s"""
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?processBoundary obo:RO_0002223 ?updateProcess .
                ?processBoundary a obo:BFO_0000035 .
                ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
                ?timeMeasDatum a obo:IAO_0000416 .
                ?timeMeasDatum obo:IAO_0000004 ?someDateTime .
                
                ?updateProcess
                    a turbo:TURBO_0010347 ;
                    turbo:TURBO_0010107 ?someRuntime ;
                    turbo:TURBO_0010108 ?someNumberOfTriples;
                    turbo:TURBO_0010186 <${Globals.expandedNamedGraph}> ;
                    obo:BFO_0000055 ?updatePlan .
                
                ?updatePlan a turbo:TURBO_0010373 ;
                    obo:RO_0000059 <$process> .
                
                <$process> a turbo:TURBO_0010354 ;
                    turbo:TURBO_0010106 ?query . 
            """

      for (inputNamedGraph <- inputNamedGraphs) {
        str += s"?updateProcess turbo:TURBO_0010187 <$inputNamedGraph> .\n"
      }

      str += "}}"
      //logger.info(str)
      str
    }

  def wasThisProcessRun(cxn: RepositoryConnection, process: String): Boolean =
    {
      val ask: String = s"""
        ASK 
          { 
            Graph pmbb:processes
            {   
                <$process> ?p ?o .
            }
          }

        """
      SparqlUpdater.querySparqlBoolean(cxn, ask).get
    }

  def getDescriberRangesAsString(cxn: RepositoryConnection, describer: String): String =
    {
      val sparqlResults = getDescriberRangesAsList(cxn, describer)
      if (sparqlResults == None) ""
      else {
        val describerAsVar = convertTypeToSparqlVariable(describer, true)
        var res = s"VALUES $describerAsVar {"
        for (item <- sparqlResults.get) res += "<" + item + ">"
        res + "}"
      }
    }

  def getDescriberRangesAsList(cxn: RepositoryConnection, describer: String): Option[ArrayBuffer[String]] =
    {
      val sparql: String = s"""
          Select * Where
          {
              <$describer> drivetrain:range ?range .
              <$describer> a drivetrain:ClassResourceList .
          }
          """
      val res = SparqlUpdater.querySparqlAndUnpackTuple(cxn, sparql, "range")
      if (res.size == 0) None
      else Some(res)
    }

  def buildFromNamedGraphsClauseFromList(graphs: ArrayBuffer[String]): String =
    {
      var res = ""
      for (graph <- graphs) {
        res += s"FROM <$graph>\n"
      }
      res
    }

  def getProcessNameAsUri(process: String): String =
    {
      var thisProcess = process
      if (!process.contains("/") && process.contains(":")) {
        val processSplit = thisProcess.split("\\:",2)
        assert(Globals.prefixMap.contains(processSplit(0)), "Undefined prefix: " + processSplit(0))
        thisProcess = Globals.prefixMap(processSplit(0)) + processSplit(1)
      }
      thisProcess
    }

  def checkAndConvertPropertiesReferenceToNamedGraph(graph: String): String =
    {
      var newGraphString = graph
      if (Globals.prefixMap.contains("properties") && graph.startsWith(Globals.prefixMap("properties"))) {
        newGraphString = retrieveUriPropertyFromFile(graph.replace(Globals.prefixMap("properties"), ""))
      }
      newGraphString
    }

  def checkGeneratedQueryAgainstMatchedQuery(processSpec: String, expectedQuery: String, printQuery: Boolean = false): Boolean =
    {
      var expectedQueryListBuffer = new ArrayBuffer[String]
      val processQueryMap = RunDrivetrainProcess.runProcess(processSpec, "None", false)
      val query = processQueryMap(processSpec)
      if (printQuery) logger.info(query.getQuery())
      val queryText = query.getQuery().replaceAll(" ", "").replaceAll("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "rdf:type").split("\\n")
      var queryTextValueStatementsRemoved = new ArrayBuffer[String]
      for (line <- queryText) if (!line.startsWith("VALUES") && line.length() != 0 && line.charAt(0) != '\r') queryTextValueStatementsRemoved += line
      val process = query.process
      for (a <- expectedQuery.replaceAll(" ", "").replaceAll("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "rdf:type").split("\\n")) {
        if (a.length() != 0 && a.charAt(0) != '\r') {
          val replacement = a.substring(0, a.length()).replace("localUUID", RunDrivetrainProcess.localUUID).replace("processURI", process)
          expectedQueryListBuffer += replacement
        }
      }
      var expectedQueryList = expectedQueryListBuffer.toArray
      val boolAsString = checkStringArraysForEquivalency(queryTextValueStatementsRemoved.toArray, expectedQueryList)("equivalent").asInstanceOf[String]
      if (boolAsString == "true") true
      else false
    }

  def getAllProcessInInstructionSet(gmCxn: RepositoryConnection): ArrayBuffer[String] =
    {
      val query: String = s"""
          select * where
          {
              ?instSet a turbo:TURBO_0010354 .
          }
          """
      SparqlUpdater.querySparqlAndUnpackTuple(gmCxn, query, "instSet")
    }

  /**
   * Searches the model graph and returns all processes in the order that they should be run
   *
   * @return ArrayBuffer[String] where each string represents a process and the index represents where each should be run in a sequence
   */
  def getAllProcessesInOrder(gmCxn: RepositoryConnection): ArrayBuffer[String] =
    {
      val getFirstProcess: String = s"""
          select ?firstProcess where
          {
              Graph <""" + Globals.defaultPrefix + s"""instructionSet>
              {
                  ?firstProcess a turbo:TURBO_0010354 .
                  Minus
                  {
                      ?someOtherProcess drivetrain:precedes ?firstProcess .
                      ?someOtherProcess a turbo:TURBO_0010354 .
                  }
              }
          }
        """

      val getProcesses: String = s"""
          select ?precedingProcess ?succeedingProcess where
          {
              Graph <""" + Globals.defaultPrefix + s"""instructionSet>
              {
                  ?precedingProcess drivetrain:precedes ?succeedingProcess .
                  ?precedingProcess a turbo:TURBO_0010354 .
                  ?succeedingProcess a turbo:TURBO_0010354 .
              }
          }
        """

      val firstProcessRes = SparqlUpdater.querySparqlAndUnpackTuple(gmCxn, getFirstProcess, "firstProcess")
      if (firstProcessRes.size > 1) throw new RuntimeException("Multiple starting processes discovered in graph model")
      if (firstProcessRes.size == 0) throw new RuntimeException("No starting process discovered in graph model")
      val res = SparqlUpdater.querySparqlAndUnpackTuple(gmCxn, getProcesses, Array("precedingProcess", "succeedingProcess"))
      var currProcess: String = firstProcessRes(0)
      var processesInOrder: ArrayBuffer[String] = new ArrayBuffer[String]
      var processMap: HashMap[String, String] = new HashMap[String, String]

      for (a <- res) processMap += a(0).toString -> a(1).toString

      while (currProcess != null) {
        processesInOrder += currProcess
        if (processMap.contains(currProcess)) currProcess = processMap(currProcess)
        else currProcess = null
      }
      processesInOrder
    }
}