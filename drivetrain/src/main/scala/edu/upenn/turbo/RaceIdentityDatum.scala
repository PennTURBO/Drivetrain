package edu.upenn.turbo

object RaceIdentityDatum extends GraphObject
{
    baseVariableName = "rid"
    val consenterBvn = Consenter.baseVariableName
    val raceIdentityValue = "ridValue"
    val raceIdentityType = "ridType"
    
    val dataset = "raceDataset"
    val raceIdentificationProcess = "raceIdentificationProcess"

    pattern = s"""
          
          ?$raceIdentificationProcess a obo:OMRSE_00000099 .
          ?$raceIdentificationProcess obo:OBI_0000299 ?$baseVariableName .
          ?$baseVariableName turbo:TURBO_0006512 ?$raceIdentityValue .
          ?$baseVariableName a ?$raceIdentityType .
          ?$baseVariableName obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$consenterBvn .
          ?$consenterBvn a turbo:TURBO_0000502 .
          ?$dataset a obo:IAO_0000100 .
          
      """

    namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    typeURI = s"""values ?raceIdentityType {}"""
    
    variablesToSelect = Array(consenterBvn, raceIdentityType, raceIdentityValue)
}