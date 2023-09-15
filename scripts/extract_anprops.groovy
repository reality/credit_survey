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

/*def PROPS = [
"<http://purl.org/dc/elements/1.1/contributor>",
"<http://www.geneontology.org/formats/oboInOwl#editor>",
"<http://www.geneontology.org/formats/oboInOwl#created_by>",
"<http://purl.obolibrary.org/obo/cl#created_by>",
"<http://purl.org/dc/elements/1.1/creator>",
"<http://purl.org/pav/curatedBy>",
"<http://purl.org/pav/createdBy>",
"<http://purl.org/net/OCRe/statistics.owl#curator>",
"<http://purl.obolibrary.org/obo/IAO_0000117>",
]*/

def allOrcidProperties = [:]
def manager = OWLManager.createOWLOntologyManager()
def fac = manager.getOWLDataFactory()
def notLoadable = []
def processOntology = { id, filename ->
  def file = new File(filename)
  if(!file.exists()) { println "File not found $id" ; return }

  def ont
  try { 
    ont = manager.loadOntologyFromOntologyDocument(file)
  } catch(e) {
    println "Could not load $id with OWLAPI"
    notLoadable << id
  }
  if(!ont) { return; }
  
  ont.getClassesInSignature(true).each { cl ->
    def iri = cl.getIRI().toString()
   EntitySearcher.getAnnotationAssertionAxioms(cl, ont).each { annoAxiom ->
      def anno = annoAxiom.getAnnotation()
      def property = anno.getProperty()
        OWLAnnotationValue val = anno.getValue()
        if(val instanceof OWLLiteral) {
          def literal = val.getLiteral()
          if(literal =~ /\d\d\d\d-\d\d\d\d-\d\d\d\d-\d\d\d\d/) {
            allOrcidProperties[property.toString()] = true
          }
        }
    }
  }
}

def i = 0
new File('data/download_links.tsv').splitEachLine('\t') {
  println i
  def path = 'data/ontologies/' + it[0] + '.owl'
  println "Processing ${it[0]}"
  processOntology(it[0], path)
i++
}

println 'done'

new File('data/all_orcid_properties.txt').text = allOrcidProperties.keySet().toList().join('\n')
new File('data/not_loadable.txt').text = notLoadable.join('\n')
