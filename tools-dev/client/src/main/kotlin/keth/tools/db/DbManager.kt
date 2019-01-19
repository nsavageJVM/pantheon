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

abstract class DbManagerBase {

    lateinit var  db : RocksDB
    lateinit var  options: Options
    lateinit var  txOptions: TransactionDBOptions

    init {
        RocksDB.loadLibrary();
    }

    /**
     * how to do this ?
     *
     * to reuse Pantheon cryto code with web3j need a map between the key format
     *
     */
    fun getNewKeyAsWeb3Key(): ECKeyPair {
        val key = SECP256K1.KeyPair.generate();
        val priKeyAsBigInt : BigInteger = key.privateKey.d
        val pubKeyAsBigInt = BigInteger(key.publicKey.toString().substring(2), 16)

        return ECKeyPair(priKeyAsBigInt, pubKeyAsBigInt)
    }


    fun getKeyAsPanKey(web3Key : ECKeyPair): SECP256K1.KeyPair {
        val key = SECP256K1.KeyPair(SECP256K1.PrivateKey.create(web3Key.privateKey), SECP256K1.PublicKey.create(web3Key.publicKey))
        return key
    }


}


object  DbManager: DbManagerBase(){

  fun createWalletStore(storageDirectory: Path) {

      options = Options().setCreateIfMissing(true)
      db = RocksDB.open(options,  storageDirectory.toString())

  }


    fun doPutStrings(key: String, value: String) {
         db.put(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8));

    }

    fun doPutBytes(key: String, value: BytesValue) {
        db.put(key.toByteArray(Charsets.UTF_8),  value.extractArray( ));

    }

    fun doGet(key: String):String {

        val value = db.get(key.toByteArray(Charsets.UTF_8))
        return String (value, Charsets.UTF_8)
    }

    fun getBytes(key: String): BytesValue {

       return BytesValue.wrap(db.get(key.toByteArray(Charsets.UTF_8)))

    }


}

fun bootStrapDbArtifacts()   {

    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')
    DbManager.createWalletStore(Constants.walletDbPath)
    DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))

}




fun main(args: Array<String>) {


    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')

    DbManager.createWalletStore(Constants.walletDbPath)

    // create java keystore for database crypto security
    DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))
    val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
    val data =  "the text to persist".toByteArray()
    val cryptoResult = DbCrypto.doFinalEncrypt(key, BytesValue.wrap(data))
    val objectMapper =  jacksonObjectMapper()
    DbManager.doPutStrings("test", objectMapper.writeValueAsString(cryptoResult) )

    val cipherResult = objectMapper.readValue(DbManager.doGet("test" ), CipherWithParams::class.java )

    println(cipherResult)

   val result =  DbCrypto.doFinalDeCrypt(key, cipherResult)

    println(String(result.extractArray(), Charsets.UTF_8 ))

}


