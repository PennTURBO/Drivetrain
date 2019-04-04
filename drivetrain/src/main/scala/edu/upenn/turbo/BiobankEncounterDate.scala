package edu.upenn.turbo

class BiobankEncounterDate extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterDate.pattern
    val baseVariableName = BiobankEncounterDate.baseVariableName
    val typeURI = BiobankEncounterDate.typeURI
    val variablesToSelect = BiobankEncounterDate.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterDate extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterDate =
    {
        new BiobankEncounterDate(optional)
    }
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000532"
    
    val baseVariableName = "biobankEncounterDate"
    
    val encounterDate = baseVariableName
    val encounterStart = "biobankEncounterStart"
    
    val dateOfBiobankEncounterStringValue = "biobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "biobankEncounterDateDateValue"
    
    val biobankEncounter = BiobankEncounter.baseVariableName

    val pattern = s"""
              		
      ?$biobankEncounter a turbo:TURBO_0000527 .
  		
  		?$encounterStart a turbo:TURBO_0000531 .
  		?$encounterStart obo:RO_0002223 ?$biobankEncounter .
  		?$encounterDate a <$typeURI> .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .
  		
  		?$encounterDate turbo:TURBO_0006511 ?$dateOfBiobankEncounterDateValue .
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfBiobankEncounterStringValue .

      """

    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(dateOfBiobankEncounterStringValue, dateOfBiobankEncounterDateValue)
}