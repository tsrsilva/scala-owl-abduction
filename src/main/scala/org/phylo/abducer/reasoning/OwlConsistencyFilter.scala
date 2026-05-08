package org.phylo.abducer.reasoning

import org.phylo.abducer.abduction.ConsistencyFilter
import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.ConsistencyCheck
import org.phylo.abducer.model.PhenotypeAssertion

final class OwlConsistencyFilter(reasoningPort: OwlReasoningPort) extends ConsistencyFilter {

  override def check(hypothesis: CandidateHypothesis, context: CompletionContext): ConsistencyCheck = {
    val assertion = PhenotypeAssertion(
      taxon = hypothesis.query.taxon,
      character = hypothesis.query.character,
      state = hypothesis.candidateState
    )

    val consistentWithReasoner = reasoningPort.isConsistentWith(assertion)

    if (consistentWithReasoner) ConsistencyCheck(isConsistent = true)
    else {
      ConsistencyCheck(
        isConsistent = false,
        reason = Some("Rejected by OWL consistency filter")
      )
    }
  }
}
