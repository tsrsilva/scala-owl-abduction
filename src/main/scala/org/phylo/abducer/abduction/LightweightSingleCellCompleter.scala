package org.phylo.abducer.abduction

import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.CompletionResult
import org.phylo.abducer.model.MissingCellQuery

final class LightweightSingleCellCompleter(
    candidateGenerator: CandidateGenerator,
    consistencyFilter: ConsistencyFilter,
    hypothesisScorer: HypothesisScorer
) extends AbductiveCompleter {

  override def complete(query: MissingCellQuery, context: CompletionContext): CompletionResult = {
    val ranked = candidateGenerator
      .generate(query, context)
      .filter { hypothesis =>
        consistencyFilter.check(hypothesis, context).isConsistent
      }
      .map { hypothesis =>
        hypothesisScorer.score(hypothesis, context)
      }
      .sortBy(scored => -scored.score)
      .take(math.max(1, query.topK))

    CompletionResult(query, ranked)
  }
}
