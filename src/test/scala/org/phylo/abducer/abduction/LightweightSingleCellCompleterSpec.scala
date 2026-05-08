package org.phylo.abducer.abduction

import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId
import org.phylo.abducer.reasoning.InMemoryReasoningPort
import org.phylo.abducer.reasoning.OwlConsistencyFilter
import org.phylo.abducer.scoring.WeightedLinearScorer
import org.phylo.abducer.scoring.features.AssumptionPenaltyFeature
import org.phylo.abducer.scoring.features.PriorProbabilityFeature
import org.phylo.abducer.scoring.features.RelatedTaxaSupportFeature
import org.scalatest.funsuite.AnyFunSuite

final class LightweightSingleCellCompleterSpec extends AnyFunSuite {

  private val taxonA = TaxonId("Species_A")
  private val taxonB = TaxonId("Species_B")
  private val taxonC = TaxonId("Species_C")

  private val character = CharacterId("mandible_shape")
  private val sNarrow = StateId("narrow")
  private val sWide = StateId("wide")
  private val sIntermediate = StateId("intermediate")

  private def baseContext(
      known: Set[PhenotypeAssertion] = Set.empty,
      priors: Map[(CharacterId, StateId), Double] = Map.empty,
      related: Map[TaxonId, Vector[TaxonId]] = Map(taxonA -> Vector(taxonB, taxonC))
  ): CompletionContext = {
    CompletionContext(
      characterStateSpace = Map(character -> Vector(sNarrow, sWide, sIntermediate)),
      knownAssertions = known,
      relatedTaxa = related,
      priors = priors
    )
  }

  private def buildCompleter(context: CompletionContext): LightweightSingleCellCompleter = {
    val reasoner = new InMemoryReasoningPort(knownAssertions = context.knownAssertions)
    val scorer = new WeightedLinearScorer(
      features = Vector(
        new PriorProbabilityFeature(),
        new RelatedTaxaSupportFeature(),
        new AssumptionPenaltyFeature()
      ),
      weights = Map(
        "prior" -> 1.0,
        "related_taxa_support" -> 1.0,
        "assumption_penalty" -> 1.0
      )
    )

    new LightweightSingleCellCompleter(
      candidateGenerator = new FiniteStateCandidateGenerator,
      consistencyFilter = new OwlConsistencyFilter(reasoner),
      hypothesisScorer = scorer
    )
  }

  test("complete ranks candidates by score after filtering") {
    val known = Set(
      PhenotypeAssertion(taxonB, character, sWide),
      PhenotypeAssertion(taxonC, character, sWide)
    )

    val context = baseContext(
      known = known,
      priors = Map(
        (character, sNarrow) -> 0.1,
        (character, sWide) -> 0.2,
        (character, sIntermediate) -> 0.15
      )
    )

    val query = MissingCellQuery(taxon = taxonA, character = character, topK = 3)

    val result = buildCompleter(context).complete(query, context)

    assert(result.ranked.map(_.hypothesis.candidateState) == Vector(sWide, sIntermediate, sNarrow))
    assert(result.bestOption.exists(_.hypothesis.candidateState == sWide))
  }

  test("complete excludes candidates rejected by consistency filter") {
    val known = Set(PhenotypeAssertion(taxonA, character, sNarrow))
    val context = baseContext(known = known)
    val query = MissingCellQuery(taxon = taxonA, character = character, topK = 3)

    val result = buildCompleter(context).complete(query, context)

    assert(result.ranked.size == 1)
    assert(result.ranked.head.hypothesis.candidateState == sNarrow)
  }

  test("complete returns empty ranked list when all candidates are inconsistent") {
    val context = baseContext(known = Set(PhenotypeAssertion(taxonA, character, sNarrow)))

    val rejectingPort = new InMemoryReasoningPort(
      knownAssertions = context.knownAssertions,
      forbiddenStates = Set(
        (taxonA, character, sNarrow),
        (taxonA, character, sWide),
        (taxonA, character, sIntermediate)
      )
    )

    val completer = new LightweightSingleCellCompleter(
      candidateGenerator = new FiniteStateCandidateGenerator,
      consistencyFilter = new OwlConsistencyFilter(rejectingPort),
      hypothesisScorer = new WeightedLinearScorer(
        features = Vector(new PriorProbabilityFeature()),
        weights = Map("prior" -> 1.0)
      )
    )

    val query = MissingCellQuery(taxon = taxonA, character = character, topK = 3)
    val result = completer.complete(query, context)

    assert(result.ranked.isEmpty)
    assert(result.bestOption.isEmpty)
  }

  test("complete respects topK truncation after ranking") {
    val context = baseContext(
      priors = Map(
        (character, sNarrow) -> 0.1,
        (character, sWide) -> 0.9,
        (character, sIntermediate) -> 0.5
      ),
      related = Map.empty
    )

    val query = MissingCellQuery(taxon = taxonA, character = character, topK = 2)
    val result = buildCompleter(context).complete(query, context)

    assert(result.ranked.map(_.hypothesis.candidateState) == Vector(sWide, sIntermediate))
    assert(result.ranked.size == 2)
  }

  test("complete treats non-positive topK as one") {
    val context = baseContext(
      priors = Map(
        (character, sNarrow) -> 0.2,
        (character, sWide) -> 0.6,
        (character, sIntermediate) -> 0.4
      ),
      related = Map.empty
    )

    val query = MissingCellQuery(taxon = taxonA, character = character, topK = 0)
    val result = buildCompleter(context).complete(query, context)

    assert(result.ranked.size == 1)
    assert(result.ranked.head.hypothesis.candidateState == sWide)
  }
}
