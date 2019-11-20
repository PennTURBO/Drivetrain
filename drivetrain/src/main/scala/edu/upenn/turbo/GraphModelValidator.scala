package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

object GraphModelValidator extends ProjectwideGlobals 
{   
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    
    def checkAcornFilesForMissingTypes()
    {
        val checkSubjects = s"""
          Select ?s
          where
          {
              Values ?graphList { <$defaultPrefix""" + s"""instructionSet> 
                                   <$defaultPrefix""" + s"""graphSpecification>
                                   <$defaultPrefix""" + s"""acornOntology>}
              Graph ?graphList
              {
                  ?s ?p ?o .
              }
              Minus
              {
                  Graph ?g
                  {
                      ?s a ?type .
                  }
              }
              filter (?s != owl:Class)
          }
          """
        //logger.info(checkSubjects)  
                              
        val checkPredicates = s"""
          Select ?p
          where
          {
              Values ?graphList { <$defaultPrefix""" + s"""instructionSet> 
                                  <$defaultPrefix""" + s"""graphSpecification>
                                  <$defaultPrefix""" + s"""acornOntology>}
              Graph ?graphList
              {
                  ?s ?p ?o .
              }
              Minus
              {
                  Graph ?g
                  {
                      ?p a ?type .
                  }
              }
              filter (?p != rdfs:subClassOf)
          }
          """
        //logger.info(checkPredicates)
        
        val checkObjects = s"""
          Select ?o
          where
          {
              {
                  Values ?graphList { <$defaultPrefix""" + s"""instructionSet> 
                                      <$defaultPrefix""" + s"""acornOntology>}
                  Graph ?graphList
                  {
                      ?s ?p ?o .
                  }
                  Minus
                  {
                      Graph ?g
                      {
                          ?o a ?type .
                      }
                  }
                  filter (?p != drivetrain:inputNamedGraph)
                  filter (?p != drivetrain:outputNamedGraph)
                  filter (?p != drivetrain:referencedInGraph)
                  filter (!isLiteral(?o))
                  filter (?o != owl:Class)
                  filter (?p != drivetrain:predicate)
                  filter (?o != rdf:Property)
              }
              UNION
              {
                  Values ?graphList { <$defaultPrefix""" + s"""graphSpecification> 
                                      <$defaultPrefix""" + s"""acornOntology>}
                  Graph ?graphList
                  {
                      ?s ?p ?o .
                  }
                  Minus
                  {
                      Graph ?g
                      {
                          ?o a ?type .
                      }
                  }
                  filter (?p != drivetrain:inputNamedGraph)
                  filter (?p != drivetrain:outputNamedGraph)
                  filter (?p != drivetrain:referencedInGraph)
                  filter (!isLiteral(?o))
                  filter (?o != owl:Class)
                  filter (?o != rdf:Property)
              }
              
          }
          """
          //logger.info(checkObjects)
                                      
          var firstRes = ""
          
          val subjectRes = update.querySparqlAndUnpackTuple(gmCxn, checkSubjects, "s")
          if (subjectRes.size != 0) firstRes = subjectRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
          
          val predRes = update.querySparqlAndUnpackTuple(gmCxn, checkPredicates, "p")
          if (predRes.size != 0) firstRes = predRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
          
          val objectRes = update.querySparqlAndUnpackTuple(gmCxn, checkObjects, "o")
          if (objectRes.size != 0) firstRes = objectRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
    }
    
    def validateProcessSpecification(process: String)
    {
       helper.validateURI(process)
       
       val select: String = s"""
          Select * Where {
            <$process> a turbo:TURBO_0010354 .
            <$process> drivetrain:inputNamedGraph ?inputNamedGraph .
            <$process> drivetrain:outputNamedGraph ?outputNamedGraph .
          }
          """
        //logger.info(select)
        val res = update.querySparqlAndUnpackTuple(gmCxn, select, Array("inputNamedGraph", "outputNamedGraph"))
        assert (res.size == 1, (if (res.size == 0) s"Process $process does not exist, ensure required input and output graphs are present" else s"Process $process has duplicate properties"))
    }
    
    def validateAcornResults(results: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        var scannedConnections = new HashMap[String, String]
        var multiplicityMap = new HashMap[String, String]
        for (row <- results)
        {
            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            val thisGraph = (if (row.contains(GRAPHOFORIGIN.toString) && row(GRAPHOFORIGIN.toString) != null) row(GRAPHOFORIGIN.toString).toString else "")
            val subjectContext = (if (row.contains(SUBJECTCONTEXT.toString) && row(SUBJECTCONTEXT.toString) != null) row(SUBJECTCONTEXT.toString).toString else "")
            val objectContext = (if (row.contains(OBJECTCONTEXT.toString) && row(OBJECTCONTEXT.toString) != null) row(OBJECTCONTEXT.toString).toString else "")
            val requirement = (if (row.contains(REQUIREMENT.toString) && row(REQUIREMENT.toString) != null) row(REQUIREMENT.toString).toString else "")
            val suffixOperator = (if (row.contains(SUFFIXOPERATOR.toString) && row(SUFFIXOPERATOR.toString) != null) row(SUFFIXOPERATOR.toString).toString else "")
            val subjectRule = (if (row.contains(SUBJECTRULE.toString) && row(SUBJECTRULE.toString) != null) row(SUBJECTRULE.toString).toString else "")
            val objectRule = (if (row.contains(OBJECTRULE.toString) && row(OBJECTRULE.toString) != null) row(OBJECTRULE.toString).toString else "")
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            val subjectObjectString = subjectString + objectString
            if (multiplicityMap.contains(subjectObjectString)) assert(multiplicityMap(subjectObjectString) == 
              thisMultiplicity, s"Error in graph model: There are multiple connections between $subjectString and $objectString with non-matching multiplicities")
            else multiplicityMap += subjectObjectString -> thisMultiplicity
            
            val fullConnectionString = subjectObjectString + row(PREDICATE.toString).toString + thisMultiplicity + thisGraph +
                                       subjectContext + objectContext + requirement + suffixOperator + subjectRule + objectRule
            if (scannedConnections.contains(connectionName)) assert(scannedConnections(connectionName) == fullConnectionString, s"Error in graph model: recipe $connectionName may have duplicate properties")
            else scannedConnections += connectionName -> fullConnectionString 
        }
    }
    
    def validateGraphSpecificationAgainstOntology()
    {
        val rangeQuery: String = s"""
          select * where
          {
              graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe drivetrain:object ?object .
                  ?recipe drivetrain:predicate ?predicate .
                  minus
                  {
                      ?object a drivetrain:MultiObjectDescriber .
                  }
              }
              graph <$defaultPrefix"""+s"""acornOntology>
              {
                  ?CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              }
              graph <$ontologyURL>
              {
                  ?predicate rdfs:subPropertyOf* ?superPredicate .
                  ?superPredicate rdfs:range ?range .
                  minus
                  {
                      ?object rdfs:subClassOf* ?range .
                  }
              }
              Minus
              {
                  ?object a ?resourceList .
                  ?resourceList rdfs:subClassOf* drivetrain:ResourceList .
              }
          }
          """
        //logger.info(rangeQuery)
        var res = update.querySparqlAndUnpackTuple(gmCxn, rangeQuery, "recipe")
        var allRes = ""
        for (singleRes <- res)
        {
            allRes += singleRes+"\n"
        }
        assert(allRes == "", s"The objects of the following recipes are not within the ranges allowed by their predicates: \n$allRes")

        val domainQuery: String = s"""
          select * where
          {
              graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe drivetrain:subject ?subject .
                  ?recipe drivetrain:predicate ?predicate .
                  minus
                  {
                      ?subject a drivetrain:ClassResourceList .
                  }
              }
              graph <$defaultPrefix"""+s"""acornOntology>
              {
                  ?CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              }
              graph <$ontologyURL>
              {
                  ?predicate rdfs:subPropertyOf* ?superPredicate .
                  ?superPredicate rdfs:domain ?domain .
                  minus
                  {
                      ?subject rdfs:subClassOf* ?domain .
                  }
              }
          }
          """
        //logger.info(domainQuery)
        res = update.querySparqlAndUnpackTuple(gmCxn, domainQuery, "recipe")
        allRes = ""
        for (singleRes <- res)
        {
            allRes += singleRes+"\n"
        }
        assert(allRes == "", s"The subjects of the following recipes are not within the domains allowed by their predicates: \n$allRes")
    }
    
    def validateConnectionRecipesInProcess(process: String)
    {
        val checkRecipes = s"""
            Select ?recipe Where
            {
                Values ?hasRecipe {drivetrain:hasRequiredInput drivetrain:hasOptionalInput drivetrain:hasOutput}
                <$process> ?hasRecipe ?recipe .
                Minus
                {
                    ?recipe a ?recipeType .
                    ?recipe drivetrain:subject ?subject .
                    ?recipe drivetrain:predicate ?predicate .
                    ?recipe drivetrain:object ?object .
                    ?recipe drivetrain:multiplicity ?multiplicity .
                    
                    Filter (?recipeType IN (drivetrain:InstanceToInstanceRecipe,
                                            drivetrain:InstanceToTermRecipe,
                                            drivetrain:TermToInstanceRecipe,
                                            drivetrain:InstanceToLiteralRecipe,
                                            drivetrain:TermToTermRecipe,
                                            drivetrain:TermToLiteralRecipe))
                }
            }
          """
        val res = update.querySparqlAndUnpackTuple(gmCxn, checkRecipes, "recipe")
        var firstRes = ""
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Process $process references undefined recipe $firstRes")
    }
    
    def validateProcessesAgainstGraphSpecification(processList: ArrayBuffer[String])
    {
        var processListAsString = ""
        for (process <- processList)
        {
            processListAsString += " <" + process + ">,"
        }
        processListAsString = processListAsString.substring(0, processListAsString.size-1)
        assert (processListAsString != "")
        
        findRequiredAndUnqueuedRecipes(processListAsString)
        findQueuedAndUnrequiredRecipes(processList, processListAsString)
    }
    
    def findRequiredAndUnqueuedRecipes(processListAsString: String)
    {   
        for (singleClass <- getAllSubjectsAndObjectsInQueuedRecipesWithContext(processListAsString))
        {
            val findRequiredButUncreatedRecipes = s"""
              Select ?recipe Where
              {
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject ?subject .
                          ?recipe drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists .
                          Optional 
                          {
                              ?recipe drivetrain:subjectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?subject drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?subject), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject ?subject .
                          ?recipe drivetrain:mustExistIf drivetrain:subjectExists .
                          Optional 
                          {
                              ?recipe drivetrain:subjectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?subject drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?subject), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object ?object .
                          ?recipe drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists .
                          Optional 
                          {
                              ?recipe drivetrain:objectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?object drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?object), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object ?object .
                          ?recipe drivetrain:mustExistIf drivetrain:objectExists .
                          Optional 
                          {
                              ?recipe drivetrain:objectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?object drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?object), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  MINUS
                  {
                      Graph <$defaultPrefix"""+s"""instructionSet>
                      {
                          ?process drivetrain:hasOutput ?recipe .
                          filter (?process IN ($processListAsString))
                      }
                  }
              }
            """
            //logger.info(findRequiredButUncreatedRecipes)      
            var firstRes = ""
            val res = update.querySparqlAndUnpackTuple(gmCxn, findRequiredButUncreatedRecipes, "recipe")
            if (res.size > 0) firstRes = res(0)
            val singleClassCleaned = helper.removeQuotesFromString(singleClass.split("\\^")(0)).split(("__"))
            val singleClassCleaned1 = singleClassCleaned(0)
            var errMsg = s"Error in graph model: connection recipe $firstRes in the Graph Specification is required due to the existence of $singleClassCleaned1 "
            if (singleClassCleaned.size > 1) errMsg += "with context " + singleClassCleaned(1) + " "
            errMsg += "but is not the output of a queued process in the Instruction Set"
            assert(firstRes == "", errMsg)
        }
    }
    
    def getAllSubjectsAndObjectsInQueuedRecipesWithContext(processListAsString: String): ArrayBuffer[String] =
    {
        val getSubjectAndObjectOutputs = s"""
          Select distinct ?classWithContext Where
          {
              {
                  Graph <$defaultPrefix"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?connection .
                  }
                  Graph <$defaultPrefix"""+s"""graphSpecification>
                  {
                      ?connection drivetrain:subject ?class .
                      Minus
                      {
                          ?connection a drivetrain:TermToInstanceRecipe ;
                      }
                  }
                  Optional 
                  {
                      ?connection drivetrain:subjectUsesContext ?context .
                      ?context a drivetrain:TurboGraphContext .
                      ?class drivetrain:hasPossibleContext ?context .
                  }
                  Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                  Bind(Concat(str(?class), "__", str(?contextOrBlank)) as ?classWithContext)
                  ?class a owl:Class .
                  filter (?process IN ($processListAsString))
              }
              UNION
              {
                  Graph <$defaultPrefix"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?connection .
                  }
                  Graph <$defaultPrefix"""+s"""graphSpecification>
                  {
                      ?connection drivetrain:object ?class .
                      Minus
                      {
                          ?connection a drivetrain:InstanceToTermRecipe ;
                      }
                  }
                  Optional 
                  {
                      ?connection drivetrain:objectUsesContext ?context .
                      ?context a drivetrain:TurboGraphContext .
                      ?class drivetrain:hasPossibleContext ?context .
                  }
                  Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                  Bind(Concat(str(?class), "__", str(?contextOrBlank)) as ?classWithContext)
                  ?class a owl:Class .
                  filter (?process IN ($processListAsString))
              }
          }
          """
        //logger.info(getSubjectAndObjectOutputs)
        update.querySparqlAndUnpackTuple(gmCxn, getSubjectAndObjectOutputs, "classWithContext")
    }
    
    def findQueuedAndUnrequiredRecipes(processList: ArrayBuffer[String], processListAsString: String)
    {
        var filterMultipleProcesses = ""
        if (processList.size > 1)
        {
             filterMultipleProcesses = s"""
                Filter Not Exists
                {
                    ?someOtherProcess drivetrain:removes ?recipe .
                }
                filter (?process != ?someOtherProcess)
                filter (?someOtherProcess IN ($processListAsString))
              """
        }
        
        val getOutputsOfAllProcesses = s"""
          Select ?recipe Where
          {
              Graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  Values ?CONNECTIONRECIPETYPE {drivetrain:InstanceToTermRecipe 
                                            drivetrain:InstanceToInstanceRecipe
                                            drivetrain:InstanceToLiteralRecipe
                                            drivetrain:TermToInstanceRecipe
                                            drivetrain:TermToTermRecipe
                                            drivetrain:TermToLiteralRecipe}
                  ?recipe a ?CONNECTIONRECIPETYPE .
              }
              Minus
              {
                  Graph <$defaultPrefix"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?recipe .
                      $filterMultipleProcesses
                      filter (?process IN ($processListAsString))
                  }
              }
          }
          """
        //println(getOutputsOfAllProcesses)
        var firstRes = ""
        val res = update.querySparqlAndUnpackTuple(gmCxn, getOutputsOfAllProcesses, "recipe")
        for (recipe <- res) logger.warn(s"Connection recipe $recipe in the Graph Specification is not the output of a queued process in the Instruction Set, but it is not a required recipe.")
    }
}