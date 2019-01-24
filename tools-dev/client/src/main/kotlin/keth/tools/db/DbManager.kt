package keth.tools.db

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.Constants
import org.rocksdb.Options
import org.rocksdb.RocksDB
import org.rocksdb.TransactionDB
import org.rocksdb.TransactionDBOptions
import org.web3j.crypto.ECKeyPair
import tech.pegasys.pantheon.crypto.SECP256K1
import tech.pegasys.pantheon.util.bytes.BytesValue
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths
import org.rocksdb.RocksIterator
import javax.crypto.SecretKey


abstract class DbManagerBase {

    var db: RocksDB? = null
    lateinit var options: Options
    val objectMapper = jacksonObjectMapper()
    var key: SecretKey? = null
    // to be user input in gui
    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')

    init {
        RocksDB.loadLibrary();

    }
}


object DbManager : DbManagerBase() {

    fun initDb() {

        options = Options().setCreateIfMissing(true)
        if (db == null) {
            db = RocksDB.open(options, Constants.walletDbPath.toString())
        }

    }


    fun initKeyStoreForDb() {

        DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))
    }

    fun doPutStrings(key: String, value: String) {
        db!!.put(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8));

    }

//    fun doPutBytes(key: String, value: BytesValue) {
//        db!!.put(key.toByteArray(Charsets.UTF_8),  value.extractArray( ));
//
//    }

    fun printKeys() {
        val iter = db!!.newIterator()
        iter.seekToFirst();

        while (iter.isValid) {
            val key = String(iter.key(), Charsets.UTF_8)
            val `val` = String(iter.value(), Charsets.UTF_8)
            println("$key -> $`val`")
            iter.next()
        }
    }

    fun doGet(key: String): String {

        val value = db!!.get(key.toByteArray(Charsets.UTF_8))
        return String(value, Charsets.UTF_8)
    }

    fun getAccounts(): Triple<String, String, String> {
        if (key == null) {
            key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
        }
        val addr1 = getAccount("acct0")
        val addr2 = getAccount("acct1")
        val addr3 = getAccount("acct2")

        return Triple(addr1, addr2, addr3)
    }

    private fun getAccount(key: String): String {
        val cr = objectMapper.readValue(doGet(key), CipherWithParams::class.java)
        return String(DbCrypto.doFinalDeCrypt(DbManager.key!!, cr).extractArray(), Charsets.UTF_8)
    }


}

fun bootStrapDbArtifacts(addresses: Triple<String, String, String>) {

    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')
    DbManager.initDb()
    DbManager.initKeyStoreForDb()

    val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)

    // in db values are encrypted in the CipherWithParams data class with ::doFinalEncrypt
    addresses.toList().forEachIndexed {

        i, e ->

        DbManager.doPutStrings("acct${i}", DbManager.objectMapper.writeValueAsString(
                DbCrypto.doFinalEncrypt(key, BytesValue.wrap(e.toByteArray()))))

    }
}


fun main(args: Array<String>) {
    DbManager.initDb()
    println(DbManager.getAccounts())


//    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
//    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')
//    DbManager.initDb()
//
////    DbManager. printKeys()
//
//    val encrypted_account = DbManager.doGet("acct0")
//    println("encrypted_account " + encrypted_account)
//
//    val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
//    val cipherResult = DbManager.objectMapper.readValue(encrypted_account, CipherWithParams::class.java)
//    val result = DbCrypto.doFinalDeCrypt(key, cipherResult)
//    println("raw bytes " + result)
//    println("addr " + String(result.extractArray(), Charsets.UTF_8))

}

//    DbManager.createWalletStore(Constants.walletDbPath)
//
//    // create java keystore for database crypto security
//    DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))
//    val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
//    val data =  "the text to persist".toByteArray()
//    val cryptoResult = DbCrypto.doFinalEncrypt(key, BytesValue.wrap(data))
//    val objectMapper =  jacksonObjectMapper()
//    DbManager.doPutStrings("test", objectMapper.writeValueAsString(cryptoResult) )
//
//    val cipherResult = objectMapper.readValue(DbManager.doGet("test" ), CipherWithParams::class.java )
//
//    println(cipherResult)
//
//   val result =  DbCrypto.doFinalDeCrypt(key, cipherResult)
//
//    println(String(result.extractArray(), Charsets.UTF_8 ))




