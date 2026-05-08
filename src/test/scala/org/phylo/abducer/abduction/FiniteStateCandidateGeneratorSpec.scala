package org.phylo.abducer.abduction

import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId
import org.scalatest.funsuite.AnyFunSuite

final class FiniteStateCandidateGeneratorSpec extends AnyFunSuite {

  test("generate returns one candidate per state in character state space") {
    val taxon = TaxonId("Species_A")
    val character = CharacterId("mandible_shape")
    val s1 = StateId("narrow")
    val s2 = StateId("wide")

    val query = MissingCellQuery(taxon = taxon, character = character)
    val context = CompletionContext(
      characterStateSpace = Map(character -> Vector(s1, s2)),
      knownAssertions = Set.empty[PhenotypeAssertion],
      relatedTaxa = Map.empty
    )

    val generator = new FiniteStateCandidateGenerator
    val candidates = generator.generate(query, context)

    assert(candidates.map(_.candidateState) == Vector(s1, s2))
    assert(candidates.forall(_.query == query))
  }

  test("generate returns empty when character has no configured states") {
    val query = MissingCellQuery(
      taxon = TaxonId("Species_A"),
      character = CharacterId("absent_character")
    )

    val context = CompletionContext(
      characterStateSpace = Map.empty,
      knownAssertions = Set.empty,
      relatedTaxa = Map.empty
    )

    val generator = new FiniteStateCandidateGenerator

    assert(generator.generate(query, context).isEmpty)
  }
}
