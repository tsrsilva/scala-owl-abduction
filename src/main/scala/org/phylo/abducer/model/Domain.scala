package org.phylo.abducer.model

final case class TaxonId(value: String) extends AnyVal
final case class CharacterId(value: String) extends AnyVal
final case class StateId(value: String) extends AnyVal

final case class PhenotypeAssertion(
    taxon: TaxonId,
    character: CharacterId,
    state: StateId
)

final case class MissingCellQuery(
    taxon: TaxonId,
    character: CharacterId,
    topK: Int = 3
)

final case class CompletionContext(
    characterStateSpace: Map[CharacterId, Vector[StateId]],
    knownAssertions: Set[PhenotypeAssertion],
    relatedTaxa: Map[TaxonId, Vector[TaxonId]],
    priors: Map[(CharacterId, StateId), Double] = Map.empty
)
