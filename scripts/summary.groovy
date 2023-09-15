def allClasses = new File('data/all_classes.txt').text.split('\n').collectEntries { it = it.tokenize('\t') ; [(it[0]): [ ontologies: it[1].tokenize(','), annotations: [:]]] }
def creditProps = new File('data/credit_orcid_properties.txt').text.split('\n')

// extract all unique annotations
new File('data/credit_annotations.tsv').splitEachLine('\t') {
  if(it[1] == 'class') { return; }
  if(!allClasses[it[1]].annotations.containsKey(it[2])) {
    allClasses[it[1]].annotations[it[2]] = [:] 
  }
  allClasses[it[1]].annotations[it[2]][it[3]] = true
}

def uniqueAnnotations = 0
def uniqueCreditAnnotations = 0
def anyCreditAnnotation = 0
def orcidCreditAnnotation = 0
allClasses.each { iri, it ->
  it.annotations = it.annotations.collectEntries { aProp, literals ->
    [(aProp): literals.keySet().toList()] 
  }
  if(it.annotations.size() > 0) {
    uniqueAnnotations += it.annotations.collect { aProp, literals -> literals.size() }.sum()
    uniqueCreditAnnotations += it.annotations.collect { aProp, literals -> creditProps.contains(aProp) ? literals.size() : 0 }.sum()
    if(it.annotations.find { aProp, literals -> creditProps.contains(aProp) }) { anyCreditAnnotation++ }
    if(it.annotations.find { aProp, literals -> creditProps.contains(aProp) && literals.any { it =~ /\d\d\d\d-\d\d\d\d-\d\d\d\d-\d\d\d\d/ } }) { orcidCreditAnnotation++ }
  }
}

println "Unique annotations: $uniqueAnnotations"
println "Unique credit annotations: $uniqueCreditAnnotations"

def anyCreditPercentage = (anyCreditAnnotation / allClasses.size()) * 100
println "Percentage of classes with any credit annotation: $anyCreditPercentage"

def orcidCreditPercentage = (orcidCreditAnnotation / allClasses.size()) * 100
println "Percentage of classes with an ORCID-containing credit annotation: $orcidCreditPercentage"
