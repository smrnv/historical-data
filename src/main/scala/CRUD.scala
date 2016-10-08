import domains.SomeData
import tables.HistoricalTable

object CRUD extends App with DbInit {

  def firstRecord = SomeData("a1", "name", 0, isDisabled = false)

  HistoricalTable.insertOrUpdate(firstRecord)

  HistoricalTable.insertData(firstRecord)

  HistoricalTable.updateData(firstRecord.copy(id = "not existing data id"))

  HistoricalTable.insertOrUpdate(firstRecord)

  HistoricalTable.insertOrUpdate(firstRecord.copy(name = "newName"))

  HistoricalTable.deleteData(firstRecord.id)



  println(HistoricalTable.selectData(firstRecord.id))



  closeDb()
}
