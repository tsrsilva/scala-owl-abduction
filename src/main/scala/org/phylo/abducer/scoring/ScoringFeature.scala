package org.phylo.abducer.scoring

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext

trait ScoringFeature {
  def name: String
  def compute(hypothesis: CandidateHypothesis, context: CompletionContext): Double
}
