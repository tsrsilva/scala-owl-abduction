package org.phylo.abducer.scoring.features

import org.phylo.abducer.model.CandidateHypothesis
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.scoring.ScoringFeature

final class RelatedTaxaSupportFeature extends ScoringFeature {
  override val name: String = "related_taxa_support"

  override def compute(hypothesis: CandidateHypothesis, context: CompletionContext): Double = {
    val related = context.relatedTaxa.getOrElse(hypothesis.query.taxon, Vector.empty)
    if (related.isEmpty) return 0.0

    val supportCount = related.count { relativeTaxon =>
      context.knownAssertions.exists { assertion =>
        assertion.taxon == relativeTaxon &&
        assertion.character == hypothesis.query.character &&
        assertion.state == hypothesis.candidateState
      }
    }

    supportCount.toDouble / related.size.toDouble
  }
}
