import scalikejdbc._

object Crebas extends App with DbInit {

  def createTableScripts = List(
    sql"""
      drop table if exists historical_table;
      create table historical_table(
        name varchar,
        id varchar not null,
        version_id int not null,
        counter int,
        is_disabled boolean not null,
        create_dt timestamp not null,
        delete_dt timestamp
      );
      create unique index idxa_historical_table_id on historical_table(id) where delete_dt is null;
      create index idxh_historical_table_id on historical_table(id) where delete_dt is not null;"""
  )

  def executor[A](scripts: Seq[SQL[A, NoExtractor]]) = scripts.foreach { script =>
    DB autoCommit { implicit session =>
      script.execute().apply()
    }
  }

  executor(createTableScripts)

  closeDb()
}
