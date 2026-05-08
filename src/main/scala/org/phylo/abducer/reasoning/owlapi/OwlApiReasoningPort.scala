package org.phylo.abducer.reasoning.owlapi

import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.TaxonId
import org.phylo.abducer.reasoning.OwlReasoningPort

/**
  * Placeholder adapter for a real OWLAPI-backed reasoner.
  *
  * Suggested next step:
  * 1) Load ontology + ABox via OWLManager.
  * 2) Initialize reasoner (e.g. ELK for EL profile).
  * 3) Implement consistency checks and entailment lookup.
  */
final class OwlApiReasoningPort extends OwlReasoningPort {
  override def isConsistentWith(assertion: PhenotypeAssertion): Boolean = {
    throw new NotImplementedError("Implement OWLAPI consistency check here")
  }

  override def entailedAssertionsFor(taxon: TaxonId): Set[PhenotypeAssertion] = {
    throw new NotImplementedError("Implement OWLAPI entailment lookup here")
  }
}
