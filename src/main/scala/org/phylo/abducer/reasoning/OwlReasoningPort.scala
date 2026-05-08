package org.phylo.abducer.reasoning

import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.TaxonId

trait OwlReasoningPort {
  def isConsistentWith(assertion: PhenotypeAssertion): Boolean
  def entailedAssertionsFor(taxon: TaxonId): Set[PhenotypeAssertion]
}
