package org.phylo.abducer.abduction

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery

trait CandidateGenerator {
  def generate(query: MissingCellQuery, context: CompletionContext): Vector[CandidateHypothesis]
}
