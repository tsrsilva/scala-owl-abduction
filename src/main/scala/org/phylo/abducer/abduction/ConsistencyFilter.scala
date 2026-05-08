package org.phylo.abducer.abduction

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.ConsistencyCheck

trait ConsistencyFilter {
  def check(hypothesis: CandidateHypothesis, context: CompletionContext): ConsistencyCheck
}
