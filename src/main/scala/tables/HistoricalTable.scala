package tables

import domains.SomeData
import org.joda.time.{DateTime, DateTimeZone}
import scalikejdbc._

case class SomeHistoricalData(id: String,
                              name: String,
                              counter: Int,
                              isDisabled: Boolean,
                              versionId: Int,
                              createDt: DateTime,
                              deleteDt: Option[DateTime]) extends HistoricalTrait[SomeHistoricalData, SomeData, String] {
  def changeTrackingFields() = Seq(name, counter, isDisabled)

  def toNormalClass = SomeData(id, name, counter, isDisabled)

  def nextVersion(prevVersionId: Int) = this.copy(versionId = prevVersionId + this.versionId)
}

object SomeHistoricalData {
  def apply(a: SomeData): SomeHistoricalData = new SomeHistoricalData(a.id, a.name, a.counter, a.isDisabled, 1, DateTime.now(DateTimeZone.UTC), None)
}


object HistoricalTable extends SQLSyntaxSupport[SomeHistoricalData] with DmlHistoricalOperations[SomeHistoricalData, SomeData, String] {
  override val tableName = "historical_table"

  def apply(rs: WrappedResultSet, rn: ResultName[SomeHistoricalData]): SomeHistoricalData =
    autoConstruct(rs, rn)

  val t = HistoricalTable.syntax("t")
  val c = HistoricalTable.column

  def insertVersion(r: SomeHistoricalData)(implicit session: DBSession) = {
    withSQL {
      insertInto(HistoricalTable)
        .namedValues(
          c.id -> r.id,
          c.name -> r.name,
          c.counter -> r.counter,
          c.isDisabled -> r.isDisabled,
          c.versionId -> r.versionId,
          c.createDt -> r.createDt,
          c.deleteDt -> r.deleteDt
        )
    }.execute().apply()
  }

  def deleteLastVersion(dataId: String, deleteDt: DateTime)(implicit session: DBSession) = {
    withSQL {
      update(HistoricalTable)
        .set(c.deleteDt -> deleteDt)
        .where.eq(c.id, dataId).and.eq(c.deleteDt, None)
    }.execute().apply()
  }

  def selectLastVersion(dataId: String)(implicit session: DBSession) = {
    withSQL {
      select
        .from(HistoricalTable as t)
        .where.eq(t.id, dataId).and.eq(t.deleteDt, None)
    }.map(rs => HistoricalTable(rs, t.resultName)).single().apply()
  }

  def objectToHistoricalObject(o: SomeData) = SomeHistoricalData(o)
}
