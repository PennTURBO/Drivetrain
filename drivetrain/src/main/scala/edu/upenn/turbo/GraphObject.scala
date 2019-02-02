package edu.upenn.turbo

abstract class GraphObject 
{
    val pattern: String
    val connections: Map[String, String]
    val namedGraph: String
    val baseVariableName: String
    val typeURI: String
    val variablesToSelect: Array[String]
}