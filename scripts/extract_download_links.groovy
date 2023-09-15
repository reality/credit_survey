import groovy.json.*

def ol = new JsonSlurper().parseText(new File('data/ontologies.jsonld').text).ontologies
def no_products = 0
def out = []

ol.each { o ->
  if(o.containsKey('products')) {
    def product = o.products.find { it.id == o.id + '.owl' }
    if(!product) {
      product = o.products[0]
    }

    out << [o.id, product.ontology_purl].join('\t')
  } else {
    no_products++ 
  }
}

println "Total entries: ${ol.size()}"
println "No products defined: ${no_products}"

new File('data/download_links.tsv').text = out.join('\n')
