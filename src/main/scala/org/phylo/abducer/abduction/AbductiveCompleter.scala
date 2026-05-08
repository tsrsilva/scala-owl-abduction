package org.phylo.abducer.abduction

import org.phylo.abducer.model.CompletionContext
import org.phylo.abducer.model.CompletionResult
import org.phylo.abducer.model.MissingCellQuery

trait AbductiveCompleter {
  def complete(query: MissingCellQuery, context: CompletionContext): CompletionResult
}
