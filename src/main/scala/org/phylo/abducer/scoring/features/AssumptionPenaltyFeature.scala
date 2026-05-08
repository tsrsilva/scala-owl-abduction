package org.phylo.abducer.scoring.features

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.scoring.ScoringFeature

final class AssumptionPenaltyFeature(penaltyPerAssumption: Double = 0.2) extends ScoringFeature {
  override val name: String = "assumption_penalty"

  override def compute(hypothesis: CandidateHypothesis, context: CompletionContext): Double = {
    -penaltyPerAssumption * hypothesis.assumptions.size.toDouble
  }
}
