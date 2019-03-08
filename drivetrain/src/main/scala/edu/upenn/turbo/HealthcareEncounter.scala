package edu.upenn.turbo

class HealthcareEncounter extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounter.pattern
    val baseVariableName = HealthcareEncounter.baseVariableName
    val typeURI = HealthcareEncounter.typeURI
    val variablesToSelect = HealthcareEncounter.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounter extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounter =
    {
        new HealthcareEncounter(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val baseVariableName = "healthcareEncounter"
    val encounterDate = "healthcareEncounterDate"
    
    val shortcutName = "shortcutHealthcareEncounterName"
    
    val encounterStart = "healthcareEncounterStart"
    val dateOfHealthcareEncounterStringValue = "healthcareEncounterDateStringValue"
    val dateOfHealthcareEncounterDateValue = "healthcareEncounterDateDateValue"
    
    val dateDataset = "dateDataset"
    val dataset = "healthcareEncounterDataset"
    
    val pattern = s"""
  	
  		?$baseVariableName a <$typeURI> .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		"""
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(baseVariableName)
}