package org.phylo.abducer.scoring.features

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.scoring.ScoringFeature

final class PriorProbabilityFeature(defaultPrior: Double = 0.01) extends ScoringFeature {
  override val name: String = "prior"

  override def compute(hypothesis: CandidateHypothesis, context: CompletionContext): Double = {
    context.priors.getOrElse((hypothesis.query.character, hypothesis.candidateState), defaultPrior)
  }
}
