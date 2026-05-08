package org.phylo.abducer.scoring

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId
import org.scalatest.funsuite.AnyFunSuite

final class WeightedLinearScorerSpec extends AnyFunSuite {

  private val query = MissingCellQuery(
    taxon = TaxonId("Species_A"),
    character = CharacterId("mandible_shape")
  )

  private val hypothesis = CandidateHypothesis(query = query, candidateState = StateId("wide"))

  private val context = CompletionContext(
    characterStateSpace = Map.empty,
    knownAssertions = Set.empty[PhenotypeAssertion],
    relatedTaxa = Map.empty
  )

  test("score computes weighted sum and stores per-feature contributions") {
    val f1 = new ScoringFeature {
      override val name: String = "f1"
      override def compute(h: CandidateHypothesis, c: CompletionContext): Double = 2.0
    }

    val f2 = new ScoringFeature {
      override val name: String = "f2"
      override def compute(h: CandidateHypothesis, c: CompletionContext): Double = 3.0
    }

    val scorer = new WeightedLinearScorer(
      features = Vector(f1, f2),
      weights = Map("f1" -> 0.5, "f2" -> 2.0)
    )

    val scored = scorer.score(hypothesis, context)

    assert(scored.featureContributions("f1") == 1.0)
    assert(scored.featureContributions("f2") == 6.0)
    assert(scored.score == 7.0)
  }

  test("score uses default weight 1.0 when feature weight is absent") {
    val f = new ScoringFeature {
      override val name: String = "unweighted"
      override def compute(h: CandidateHypothesis, c: CompletionContext): Double = 1.75
    }

    val scorer = new WeightedLinearScorer(
      features = Vector(f),
      weights = Map.empty
    )

    val scored = scorer.score(hypothesis, context)

    assert(scored.featureContributions("unweighted") == 1.75)
    assert(scored.score == 1.75)
  }

  test("rationale is sorted descending by contribution") {
    val low = new ScoringFeature {
      override val name: String = "low"
      override def compute(h: CandidateHypothesis, c: CompletionContext): Double = 0.1
    }

    val high = new ScoringFeature {
      override val name: String = "high"
      override def compute(h: CandidateHypothesis, c: CompletionContext): Double = 0.9
    }

    val scorer = new WeightedLinearScorer(
      features = Vector(low, high),
      weights = Map.empty
    )

    val scored = scorer.score(hypothesis, context)

    assert(scored.rationale.head.startsWith("high contributed"))
    assert(scored.rationale.last.startsWith("low contributed"))
  }
}
