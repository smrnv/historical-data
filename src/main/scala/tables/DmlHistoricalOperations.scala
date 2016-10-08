package tables


import org.joda.time.{DateTime, DateTimeZone}
import scalikejdbc.{DB, DBSession, SQLSyntaxSupportFeature}
import scalikejdbc._

import scala.util.control.NoStackTrace

trait DmlHistoricalOperations[H <: HistoricalTrait[H, A, T], A, T] {
  self: SQLSyntaxSupport[H] =>

  def insertVersion(r: H)(implicit session: DBSession): Unit

  def deleteLastVersion(dataId: T, deleteDt: DateTime)(implicit session: DBSession): Unit

  def selectLastVersion(dataId: T)(implicit session: DBSession): Option[H]

  def objectToHistoricalObject(o: A): H


  final def modify(row: H)(implicit session: DBSession): Unit = {
    selectLastVersion(row.id) match {
      case Some(v) if v.changeTrackingHash() != row.changeTrackingHash() =>
        deleteLastVersion(row.id, row.createDt)
        insertVersion(row.nextVersion(v.versionId))
      case None =>
        insertVersion(row)
      case _ => ()
    }
  }


  final def deleteData(dataId: T): Unit = {
    DB autoCommit { implicit session =>
      deleteLastVersion(dataId, DateTime.now(DateTimeZone.UTC))
    }
  }

  final def selectData(dataId: T): Option[A] = {
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

  def insertData(row: A): Unit = {
    DB localTx{implicit  session =>
      val historicalObject = objectToHistoricalObject(row)

      selectLastVersion(historicalObject.id) match {
        case None =>  modify(historicalObject)
        case Some(d) => throw PrimaryKeyViolation(tableName, historicalObject.id.toString)
      }
    }
  }

  def updateData(row: A, raiseException: Boolean = false): Unit = {
    DB localTx{implicit  session =>
      val historicalObject = objectToHistoricalObject(row)

      selectLastVersion(historicalObject.id) match {
        case None if raiseException =>  throw NoSuchElement(tableName, historicalObject.id.toString)
        case Some(d) => modify(historicalObject)
        case _ => None
      }
    }
  }

  private case class PrimaryKeyViolation(table: String, keyValue: String) extends NoStackTrace{
    override def getMessage: String = s"""there is already record with the key "$tableName" in the table "$table""""
  }

  private case class NoSuchElement(table: String, keyValue: String) extends NoStackTrace{
    override def getMessage: String = s"""there is no record with the key "$tableName" in the table "$table""""
  }
}


