import scalikejdbc.config._

trait DbInit {
  DBs.setupAll()

  def closeDb() = DBs.closeAll()
}
