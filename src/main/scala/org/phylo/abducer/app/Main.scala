package org.phylo.abducer.app

import org.phylo.abducer.abduction.FiniteStateCandidateGenerator
import org.phylo.abducer.abduction.LightweightSingleCellCompleter
import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId
import org.phylo.abducer.reasoning.InMemoryReasoningPort
import org.phylo.abducer.reasoning.OwlConsistencyFilter
import org.phylo.abducer.scoring.WeightedLinearScorer
import org.phylo.abducer.scoring.features.AssumptionPenaltyFeature
import org.phylo.abducer.scoring.features.PriorProbabilityFeature
import org.phylo.abducer.scoring.features.RelatedTaxaSupportFeature

object Main {
  def main(args: Array[String]): Unit = {
    val cMandible = CharacterId("mandible_shape")
    val sNarrow = StateId("narrow")
    val sWide = StateId("wide")
    val sIntermediate = StateId("intermediate")

    val speciesA = TaxonId("Species_A")
    val speciesB = TaxonId("Species_B")
    val speciesC = TaxonId("Species_C")

    val knownAssertions = Set(
      PhenotypeAssertion(speciesB, cMandible, sWide),
      PhenotypeAssertion(speciesC, cMandible, sWide)
    )

    val context = CompletionContext(
      characterStateSpace = Map(cMandible -> Vector(sNarrow, sWide, sIntermediate)),
      knownAssertions = knownAssertions,
      relatedTaxa = Map(speciesA -> Vector(speciesB, speciesC)),
      priors = Map(
        (cMandible, sNarrow) -> 0.20,
        (cMandible, sWide) -> 0.55,
        (cMandible, sIntermediate) -> 0.25
      )
    )

    val query = MissingCellQuery(
      taxon = speciesA,
      character = cMandible,
      topK = 3
    )

    val reasoner = new InMemoryReasoningPort(knownAssertions = knownAssertions)

    val scorer = new WeightedLinearScorer(
      features = Vector(
        new PriorProbabilityFeature(),
        new RelatedTaxaSupportFeature(),
        new AssumptionPenaltyFeature()
      ),
      weights = Map(
        "prior" -> 0.7,
        "related_taxa_support" -> 1.2,
        "assumption_penalty" -> 1.0
      )
    )

    val completer = new LightweightSingleCellCompleter(
      candidateGenerator = new FiniteStateCandidateGenerator,
      consistencyFilter = new OwlConsistencyFilter(reasoner),
      hypothesisScorer = scorer
    )

    val result = completer.complete(query, context)

    println(s"Missing cell: taxon=${query.taxon.value}, character=${query.character.value}")
    println("Ranked candidate states:")

    result.ranked.zipWithIndex.foreach { case (scored, idx) =>
      val rank = idx + 1
      println(f"  $rank%2d. ${scored.hypothesis.candidateState.value}%-15s score=${scored.score}%.3f")
      scored.rationale.foreach(reason => println(s"      - $reason"))
    }

    result.bestOption match {
      case Some(best) =>
        println(f"Best explanation: ${best.hypothesis.candidateState.value} (score=${best.score}%.3f)")
      case None =>
        println("No consistent candidates found for this missing cell.")
    }
  }
}
