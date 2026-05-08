package org.phylo.abducer.abduction

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery

final class FiniteStateCandidateGenerator extends CandidateGenerator {
  override def generate(query: MissingCellQuery, context: CompletionContext): Vector[CandidateHypothesis] = {
    context.characterStateSpace
      .getOrElse(query.character, Vector.empty)
      .map(state => CandidateHypothesis(query = query, candidateState = state))
  }
}
