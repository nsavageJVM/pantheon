package keth.tools.db

import keth.tools.Constants
import org.rocksdb.Options
import org.rocksdb.RocksDB
import org.rocksdb.TransactionDB
import org.rocksdb.TransactionDBOptions
import java.nio.file.Path
import java.nio.file.Paths

abstract class DbManagerBase {

    lateinit var  db : RocksDB
    lateinit var  options: Options
    lateinit var  txOptions: TransactionDBOptions

    init {
        RocksDB.loadLibrary();
    }

}


object  DbManager: DbManagerBase(){

  fun createWalletSore(storageDirectory: Path) {

      options = Options().setCreateIfMissing(true)
      db = RocksDB.open(options,  storageDirectory.toString())

  }



}





