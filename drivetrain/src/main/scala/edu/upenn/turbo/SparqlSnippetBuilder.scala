package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class SparqlSnippetBuilder extends ProjectwideGlobals
{
    def buildSnippet(recipe: ConnectionRecipe): String =
    {
        helper.validateURI(recipe.subject.value)
        val subject = buildSubject(recipe)
        val predicate = buildPredicate(recipe)
        val crObject = buildObject(recipe)
        subject + " " + predicate + " " + crObject
    }
    
    def buildSubject(recipe: ConnectionRecipe): String =
    {
        var subject = "<" + recipe.subject.value + ">"
        if (recipe.isInstanceOf[Instance] || (recipe.isInstanceOf[Term] && recipe.asInstanceOf[Term].isResourceList.get)) subject = helper.convertTypeToSparqlVariable(recipe.subject.value, true)
        subject
    }

    def buildPredicate(recipe: ConnectionRecipe): String =
    {
        var predicate = recipe.predicate
        var formattedSuffixOperator = ""
        if (recipe.predicateSuffixOperator != None)
        {
            val allowedOperators = Array("*", "+")
            formattedSuffixOperator = recipe.predicateSuffixOperator.get.split("\\^")(0).replaceAll("\"", "")
            if (!allowedOperators.contains(formattedSuffixOperator)) formattedSuffixOperator = ""
            if (predicate == "a" || predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") predicate = "rdf:type" + formattedSuffixOperator
        }
            helper.validateURI(predicate)
            "<" + predicate + ">" + formattedSuffixOperator
    }
    
    def buildObject(recipe: ConnectionRecipe): String =
    {
        var crObject = ""
        if (recipe.isInstanceOf[Literal])
        {
            if (!recipe.asInstanceOf[Literal].isResourceList.get) crObject = recipe.crObject.value
            else crObject = helper.convertTypeToSparqlVariable(recipe.crObject.value, true)
        }
        else
        {
            var crObject = "<" + recipe.crObject.value + ">"
            if (recipe.isInstanceOf[Instance] || (recipe.isInstanceOf[Term] && recipe.asInstanceOf[Term].isResourceList.get)) crObject = helper.convertTypeToSparqlVariable(recipe.crObject.value, true) 
        }
        crObject
    }
}