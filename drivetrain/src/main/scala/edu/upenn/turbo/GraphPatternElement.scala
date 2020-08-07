package edu.upenn.turbo

import scala.collection.mutable.HashSet

trait GraphPatternElement 
{
    var value: String = null
    
    val oneToOneConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    val oneToManyConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    val manyToOneConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
}

class Instance extends GraphPatternElement
{
    var isUntyped: Option[Boolean] = None
    var isSingleton: Option[Boolean] = None
    var isSuperSingleton: Option[Boolean] = None 
}

class Term extends GraphPatternElement
{
    var isResourceList: Option[Boolean] = None
}

class Literal extends GraphPatternElement
{
    var isResourceList: Option[Boolean] = None
}