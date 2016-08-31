package tables


import org.joda.time.{DateTime, DateTimeZone}
import scalikejdbc.{DB, DBSession}

trait DmlHistoricalOperations[H <: HistoricalTrait[H, A, T], A, T] {

  def insertVersion(r: H)(implicit session: DBSession): Unit

  def deleteLastVersion(dataId: T, deleteDt: DateTime)(implicit session: DBSession): Unit

  def selectLastVersion(dataId: T)(implicit session: DBSession): Option[H]

  def objectToHistoricalObject(o: A): H


  def modify(row: H)(implicit session: DBSession): Unit = {
    selectLastVersion(row.id) match {
      case Some(v) if v.changeTrackingHash() != row.changeTrackingHash() =>
        deleteLastVersion(row.id, row.createDt)
        insertVersion(row.nextVersion(v.versionId))
      case None =>
        insertVersion(row)
      case _ => ()
    }
  }


  def deleteData(dataId: T): Unit = {
    DB autoCommit { implicit session =>
      deleteLastVersion(dataId, DateTime.now(DateTimeZone.UTC))
    }
  }

  def selectData(dataId: T): Option[A] = {
    DB readOnly { implicit session =>
      selectLastVersion(dataId) match {
        case None => None
        case Some(d) => Some(d.toNormalClass)
      }
    }
  }

  def insertOrUpdate(row: A): Unit = {
    DB localTx { implicit session =>
      modify(objectToHistoricalObject(row))
    }
  }

}
