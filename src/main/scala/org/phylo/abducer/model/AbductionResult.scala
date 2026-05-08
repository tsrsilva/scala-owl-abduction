package org.phylo.abducer.model

final case class CandidateHypothesis(
    query: MissingCellQuery,
    candidateState: StateId,
    assumptions: Vector[PhenotypeAssertion] = Vector.empty
)

final case class ConsistencyCheck(
    isConsistent: Boolean,
    reason: Option[String] = None
)

final case class ScoredHypothesis(
    hypothesis: CandidateHypothesis,
    score: Double,
    featureContributions: Map[String, Double],
    rationale: Vector[String]
)

final case class CompletionResult(
    query: MissingCellQuery,
    ranked: Vector[ScoredHypothesis]
) {
  def bestOption: Option[ScoredHypothesis] = ranked.headOption
}
