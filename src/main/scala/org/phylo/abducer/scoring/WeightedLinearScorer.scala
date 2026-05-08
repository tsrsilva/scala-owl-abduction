package org.phylo.abducer.scoring

import org.phylo.abducer.abduction.HypothesisScorer
import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.ScoredHypothesis

final class WeightedLinearScorer(
    features: Vector[ScoringFeature],
    weights: Map[String, Double]
) extends HypothesisScorer {

  override def score(hypothesis: CandidateHypothesis, context: CompletionContext): ScoredHypothesis = {
    val contributions = features.map { feature =>
      val rawValue = feature.compute(hypothesis, context)
      val weight = weights.getOrElse(feature.name, 1.0)
      feature.name -> (rawValue * weight)
    }.toMap

    val totalScore = contributions.values.sum

    val rationale = contributions.toVector
      .sortBy { case (_, contribution) => -contribution }
      .map { case (featureName, contribution) =>
        f"$featureName contributed $contribution%.3f"
      }

    ScoredHypothesis(
      hypothesis = hypothesis,
      score = totalScore,
      featureContributions = contributions,
      rationale = rationale
    )
  }
}
