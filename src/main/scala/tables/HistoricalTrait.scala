package tables

import org.joda.time.DateTime

trait HistoricalTrait[H <: HistoricalTrait[H, A, T], A, T] {

  def id: T

  def versionId: Int

  def createDt: DateTime

  def deleteDt: Option[DateTime]

  def changeTrackingFields(): Seq[Any]

  def changeTrackingHash() = changeTrackingFields().hashCode()

  def toNormalClass: A

  def nextVersion(prevVersionId: Int): H
}