package org.phylo.abducer.abduction

import org.phylo.abducer.model.CharacterId
import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.MissingCellQuery
import org.phylo.abducer.model.PhenotypeAssertion
import org.phylo.abducer.model.StateId
import org.phylo.abducer.model.TaxonId
import org.phylo.abducer.reasoning.OwlConsistencyFilter
import org.phylo.abducer.reasoning.OwlReasoningPort
import org.phylo.abducer.scoring.WeightedLinearScorer
import org.phylo.abducer.scoring.features.PriorProbabilityFeature
import org.phylo.abducer.tags.HermitGuard
import org.scalatest.funsuite.AnyFunSuite
import org.semanticweb.HermiT.ReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLOntology

import scala.jdk.CollectionConverters.*

final class LightweightSingleCellCompleterHermitGuardSpec extends AnyFunSuite {

  private val baseIri = "http://example.org/toy#"
  private val taxonA = TaxonId("Species_A")
  private val character = CharacterId("mandible_shape")
  private val sNarrow = StateId("narrow")
  private val sWide = StateId("wide")

  test("HermiT guard: disjoint-state candidate is inconsistent", HermitGuard) {
    val manager = OWLManager.createOWLOntologyManager()
    val stream = Option(getClass.getResourceAsStream("/owl/toy-phenotype.ofn"))
      .getOrElse(fail("Missing test ontology fixture: /owl/toy-phenotype.ofn"))

    val ontology = try manager.loadOntologyFromOntologyDocument(stream)
    finally stream.close()

    val context = CompletionContext(
      characterStateSpace = Map(character -> Vector(sNarrow, sWide)),
      knownAssertions = Set.empty[PhenotypeAssertion],
      relatedTaxa = Map.empty,
      priors = Map(
        (character, sNarrow) -> 0.2,
        (character, sWide) -> 0.9
      )
    )

    val scorer = new WeightedLinearScorer(
      features = Vector(new PriorProbabilityFeature()),
      weights = Map("prior" -> 1.0)
    )

    val completer = new LightweightSingleCellCompleter(
      candidateGenerator = new FiniteStateCandidateGenerator,
      consistencyFilter = new OwlConsistencyFilter(
        new HermitGuardReasoningPort(
          baseOntology = ontology,
          taxonIriMap = Map(taxonA -> IRI.create(baseIri + "SpeciesA")),
          stateIriMap = Map(
            sNarrow -> IRI.create(baseIri + "Narrow"),
            sWide -> IRI.create(baseIri + "Wide")
          )
        )
      ),
      hypothesisScorer = scorer
    )

    val query = MissingCellQuery(taxon = taxonA, character = character, topK = 2)
    val result = completer.complete(query, context)

    assert(result.ranked.size == 1)
    assert(result.ranked.head.hypothesis.candidateState == sNarrow)
  }

  private final class HermitGuardReasoningPort(
      baseOntology: OWLOntology,
      taxonIriMap: Map[TaxonId, IRI],
      stateIriMap: Map[StateId, IRI]
  ) extends OwlReasoningPort {

    private val reasonerFactory = new ReasonerFactory
    private val dataFactory = OWLManager.getOWLDataFactory

    override def isConsistentWith(assertion: PhenotypeAssertion): Boolean = {
      val maybeTaxon = taxonIriMap.get(assertion.taxon)
      val maybeState = stateIriMap.get(assertion.state)

      (maybeTaxon, maybeState) match {
        case (Some(taxonIri), Some(stateIri)) =>
          val classExpr: OWLClass = dataFactory.getOWLClass(stateIri)
          val individual: OWLNamedIndividual = dataFactory.getOWLNamedIndividual(taxonIri)
          val classAssertion: OWLAxiom = dataFactory.getOWLClassAssertionAxiom(classExpr, individual)

          val tempManager = OWLManager.createOWLOntologyManager()
          val mergedAxioms = baseOntology.getAxioms.asScala.toSet + classAssertion
          val tempOntology = tempManager.createOntology(mergedAxioms.asJava)

          val reasoner = reasonerFactory.createReasoner(tempOntology)
          try reasoner.isConsistent
          finally {
            reasoner.dispose()
            tempManager.removeOntology(tempOntology)
          }

        case _ =>
          false
      }
    }

    override def entailedAssertionsFor(taxon: TaxonId): Set[PhenotypeAssertion] = Set.empty
  }
}
