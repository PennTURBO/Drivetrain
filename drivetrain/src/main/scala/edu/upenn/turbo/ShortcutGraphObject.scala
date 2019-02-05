package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

abstract class ShortcutGraphObject extends GraphObject with IRIConstructionRules
{
    val variableExpansions: LinkedHashMap[String, Array[String]]
    val instantiation: String
    val expandedVariableShortcutDependencies: Map[String, String]
}