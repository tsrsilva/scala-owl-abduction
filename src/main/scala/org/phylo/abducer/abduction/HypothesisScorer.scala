package org.phylo.abducer.abduction

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.ScoredHypothesis

trait HypothesisScorer {
  def score(hypothesis: CandidateHypothesis, context: CompletionContext): ScoredHypothesis
}
