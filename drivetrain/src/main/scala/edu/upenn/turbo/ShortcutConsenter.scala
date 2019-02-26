package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenter(instantiation: String, namedGraph: String, optional: Boolean) 
extends ShortcutGraphObject(instantiation: String, namedGraph: String, optional: Boolean)
{

}

object ShortcutConsenter extends ShortcutGraphObject()
{    
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutConsenter =
    {
        new ShortcutConsenter(instantiation, namedGraph, optional)
    }
    
    val instantiationKey = "instantiation"
    baseVariableName = "shortcutPart"
    
    val shortcutName = "shortcutPart"
    val valuesKey = "consenterSymbolValue"
    val registryKey = "consenterRegistryString"
    
    val cridKey = "shortcutCrid"
    val datasetTitle = "shortcutDatasetTitle"
    
    val dateOfBirthString = "dateOfBirthStringValue"
    val dateOfBirthDate = "dateOfBirthDateValue"
    
    val genderIdentityValue = "genderIdentityDatumValue"
    val genderIdentityType = "genderIdentityTypeString"
    
    val raceIdentityValue = "raceIdentityDatumValue"
    val raceIdentityType = "raceIdentityTypeString"
    
    pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?$cridKey a turbo:TURBO_0000503 .
          ?$cridKey obo:IAO_0000219 ?$baseVariableName .
          ?$cridKey turbo:TURBO_0003603 ?$datasetTitle .
          ?$cridKey turbo:TURBO_0003610 ?$registryKey .
          ?$cridKey turbo:TURBO_0003608 ?$valuesKey .

          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000604  ?$dateOfBirthString .
          }
          OPTIONAL
          {
           ?$baseVariableName turbo:TURBO_0000605   ?$dateOfBirthDate .
          }
          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000606  ?$genderIdentityValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000607   ?$genderIdentityType .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000614 ?$raceIdentityType .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000615 ?$raceIdentityValue .
          }
      """
    
    typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    variablesToSelect = Array(baseVariableName, registryKey, valuesKey)
    
    expansionRules = Array(
        
        ExpansionFromShortcutValue.create(RaceIdentityDatum.raceIdentityValue, raceIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(DateOfBirthDatum.dateOfBirthString, dateOfBirthString, BindAs),
        ExpansionFromShortcutValue.create(DateOfBirthDatum.dateOfBirthDate, dateOfBirthDate, BindAs),
        ExpansionFromShortcutValue.create(ConsenterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(ConsenterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(GenderIdentityDatum.genderIdentityValue, genderIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(GenderIdentityDatum.genderIdentityValue, genderIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(RaceIdentityDatum.raceIdentityType, raceIdentityType, StringToURI),
        ExpansionFromShortcutValue.create(Consenter.shortcutName, shortcutName, URIToString),
        ExpansionFromShortcutValue.create(ConsenterIdentifier.instantiation, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(ConsenterIdentifier.datasetTitle, datasetTitle, BindAs),
        
        ExpansionOfIntermediateNode.create(Consenter.biosexKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(Consenter.birthVariableName, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(Consenter.heightKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(Consenter.weightKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(Consenter.adiposeKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(Consenter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(ConsenterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(ConsenterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(ConsenterIdentifier.consenterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(ConsenterIdentifier.consenterRegistry, RandomUUID),
        ExpansionOfIntermediateNode.create(GenderIdentityDatum.baseVariableName, BindIfBoundMD5LocalRandom, genderIdentityValue),
        ExpansionOfIntermediateNode.create(RaceIdentityDatum.baseVariableName, BindIfBoundMD5LocalRandom, raceIdentityType),
        ExpansionOfIntermediateNode.create(RaceIdentityDatum.raceIdentificationProcess, BindIfBoundMD5LocalRandom, raceIdentityType),
        ExpansionOfIntermediateNode.create(DateOfBirthDatum.baseVariableName, BindIfBoundMD5LocalRandom, dateOfBirthString),
        ExpansionOfIntermediateNode.create(GenderIdentityDatum.genderIdentityType, BiologicalSexIRI, genderIdentityType),
        ExpansionOfIntermediateNode.create(DateOfBirthDatum.dataset, BindIfBoundDataset, dateOfBirthString),
        ExpansionOfIntermediateNode.create(RaceIdentityDatum.dataset, BindIfBoundDataset, raceIdentityType),
        ExpansionOfIntermediateNode.create(GenderIdentityDatum.dataset, BindIfBoundDataset, genderIdentityValue)
        
        )
}