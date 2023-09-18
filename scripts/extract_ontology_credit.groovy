@Grapes([
    @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.3'),
    @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='5.1.14'),
    @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='5.1.14'),
    @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='5.1.14'),
    @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='5.1.14'),
    @Grab(group='net.sourceforge.owlapi', module='owlapi-distribution', version='5.1.14'),
    @GrabConfig(systemClassLoader=true)
])

import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.elk.owlapi.*
import org.semanticweb.elk.reasoner.config.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.io.*
import org.semanticweb.owlapi.owllink.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.owlapi.search.*
import org.semanticweb.owlapi.manchestersyntax.renderer.*
import org.semanticweb.owlapi.reasoner.structural.*

def PROPS = new File('data/all_orcid_properties.txt').text.split('\n') 

def oWriter = new BufferedWriter(new FileWriter('data/ontology_credit_annotations.tsv'))
oWriter.write("oid\tproperty\tvalue\torcid\n")

def manager = OWLManager.createOWLOntologyManager()
def fac = manager.getOWLDataFactory()
def processOntology = { id, filename ->
  def file = new File(filename)
  if(!file.exists()) { println "File not found $id" ; return }

  def ont
  try { 
    ont = manager.loadOntologyFromOntologyDocument(file)
  } catch(e) {
    println "Could not load $id with OWLAPI"
  }
  if(!ont) { return; }

  ont.getAnnotations().each { anno ->
    def property = anno.getProperty().toString()
    if(PROPS.contains(property)) {
      OWLAnnotationValue val = anno.getValue()
      if(val instanceof OWLLiteral) {
        def literal = val.getLiteral().replaceAll('\t','').replaceAll('\n','')
        def orcid = false
        if(literal =~ /\d\d\d\d-\d\d\d\d-\d\d\d\d-\d\d\d\d/) {
          orcid = true  
        }
        oWriter.write([id, property, literal, orcid].join('\t') + '\n')
      }
    }
  }

    /*EntitySearcher.getAnnotationAssertionAxioms(ont, ont).each { annoAxiom ->
    println annoAxiom.getAnnotation()
    }*/
}

def i = 0
new File('data/download_links.tsv').splitEachLine('\t') {
  println i
  def path = 'data/ontologies/' + it[0] + '.owl'
  println "Processing ${it[0]}"
  processOntology(it[0], path)
i++
}

oWriter.flush()
oWriter.close()
println 'done'
