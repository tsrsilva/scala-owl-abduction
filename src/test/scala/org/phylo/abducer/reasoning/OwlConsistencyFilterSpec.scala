package org.phylo.abducer.reasoning

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId
import org.scalatest.funsuite.AnyFunSuite

final class OwlConsistencyFilterSpec extends AnyFunSuite {

  private val taxon = TaxonId("Species_A")
  private val character = CharacterId("mandible_shape")
  private val state = StateId("wide")

  private val baseQuery = MissingCellQuery(taxon = taxon, character = character)
  private val baseContext = CompletionContext(
    characterStateSpace = Map.empty,
    knownAssertions = Set.empty,
    relatedTaxa = Map.empty
  )

  test("check marks hypothesis consistent when reasoning port accepts assertion") {
    val reasoningPort = new OwlReasoningPort {
      override def isConsistentWith(assertion: PhenotypeAssertion): Boolean = {
        assertion == PhenotypeAssertion(taxon, character, state)
      }

      override def entailedAssertionsFor(taxon: TaxonId): Set[PhenotypeAssertion] = Set.empty
    }

    val filter = new OwlConsistencyFilter(reasoningPort)
    val hypothesis = CandidateHypothesis(baseQuery, state)

    val result = filter.check(hypothesis, baseContext)

    assert(result.isConsistent)
    assert(result.reason.isEmpty)
  }

  test("check marks hypothesis inconsistent with rejection reason when reasoning port rejects") {
    val reasoningPort = new OwlReasoningPort {
      override def isConsistentWith(assertion: PhenotypeAssertion): Boolean = false

      override def entailedAssertionsFor(taxon: TaxonId): Set[PhenotypeAssertion] = Set.empty
    }

    val filter = new OwlConsistencyFilter(reasoningPort)
    val hypothesis = CandidateHypothesis(baseQuery, state)

    val result = filter.check(hypothesis, baseContext)

    assert(!result.isConsistent)
    assert(result.reason.contains("Rejected by OWL consistency filter"))
  }
}
