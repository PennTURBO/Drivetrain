package edu.upenn.turbo

class HealthcareEncounterWeight extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterWeight.pattern
    val baseVariableName = HealthcareEncounterWeight.baseVariableName
    val typeURI = HealthcareEncounterWeight.typeURI
    val variablesToSelect = HealthcareEncounterWeight.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterWeight extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounterWeight =
    {
        new HealthcareEncounterWeight(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/OBI_0000445"
    
    val baseVariableName = "HealthcareEncounterWeightAssay"
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "HealthcareEncounterWeightValue"
    val datumKey = "HealthcareEncounterWeightDatum"
    
    val weightValue = valuesKey
    
    val valueSpecification = "HealthcareEncounterWeightValSpec"
    
    val pattern = s"""
      
        ?$baseVariableName a <$typeURI> ;
  	                 obo:OBI_0000299 ?$datumKey.

  	    ?$datumKey a obo:IAO_0000414 ;
  	                 obo:OBI_0001938 ?$valueSpecification .

  	    ?$valueSpecification a obo:OBI_0001931 ;
  	                   obo:IAO_0000039 obo:UO_0000009 ;
  	                   obo:OBI_0002135 ?$valuesKey .
  	                  
  	    ?$encounterVariableName obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$encounterVariableName .
    		
      """
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}