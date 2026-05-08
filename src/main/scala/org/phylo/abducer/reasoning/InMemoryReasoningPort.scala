package org.phylo.abducer.reasoning

import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId

/**
  * A small learning-focused stand-in for a real OWL reasoner.
  *
  * It models character-state exclusivity by rejecting any assertion that
  * conflicts with already known assertions for the same taxon and character.
  */
final class InMemoryReasoningPort(
    knownAssertions: Set[PhenotypeAssertion],
    forbiddenStates: Set[(TaxonId, CharacterId, StateId)] = Set.empty
) extends OwlReasoningPort {

  override def isConsistentWith(assertion: PhenotypeAssertion): Boolean = {
    val explicitlyForbidden = forbiddenStates.contains((assertion.taxon, assertion.character, assertion.state))

    val conflictsExisting = knownAssertions.exists { existing =>
      existing.taxon == assertion.taxon &&
      existing.character == assertion.character &&
      existing.state != assertion.state
    }

    !explicitlyForbidden && !conflictsExisting
  }

  override def entailedAssertionsFor(taxon: TaxonId): Set[PhenotypeAssertion] = {
    knownAssertions.filter(_.taxon == taxon)
  }
}
