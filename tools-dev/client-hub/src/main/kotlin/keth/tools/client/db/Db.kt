package keth.tools.client.db

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.client.GlobalConstants
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.rocksdb.Options
import org.rocksdb.RocksDB
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tech.pegasys.pantheon.util.bytes.BytesValue
import java.io.FileInputStream
import java.security.KeyStore
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

// so we can pass the iv around
data class CipherWithParams(val iv: ByteArray, val data: ByteArray )


abstract class DbManagerBase {

    var db: RocksDB? = null
    lateinit var options: Options
    val objectMapper = jacksonObjectMapper()
    var db_encryption_key: SecretKey? = null
    // to be user input in gui
    var keyPass: String? = null
    var strPass: String? = null
    val ALIAS = "secretkey"

    var isInitialised: Boolean = false

    init {
        Security.addProvider(BouncyCastleProvider())
        RocksDB.loadLibrary();
    }

    fun doFinalEncrypt(key: SecretKey, data: BytesValue): CipherWithParams {
        val cipher = Cipher.getInstance("AES/CFB/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return CipherWithParams(cipher.getIV(), cipher.doFinal(data.extractArray()))
    }

    fun doFinalDeCrypt(key: SecretKey, data: CipherWithParams): BytesValue {
        val cipher = Cipher.getInstance("AES/CFB/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(data.iv));
        return BytesValue.wrap(cipher.doFinal(data.data))
    }
}

@Service
class DbManager : DbManagerBase() {


    @Autowired
    lateinit var consts: GlobalConstants

    fun initDb() {

        options = Options().setCreateIfMissing(true)
        if (db == null) {
            db = RocksDB.open(options, consts!!.WALLET_DB_PATH.toString())
            isInitialised = true
        }

    }


    fun doPutStrings(key: String, value: String) {
        db!!.put(key.toByteArray(Charsets.UTF_8), value.toByteArray(Charsets.UTF_8));

    }


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

        if(keyPass == null) {
            keyPass = consts.dbCryptoKeyPass
            strPass = consts.dbCryptoKeyStorePass
        }


        if (db_encryption_key == null) {
            db_encryption_key =  getDbEncryptionKey(strPass!!.toCharArray(), keyPass!!.toCharArray())
        }
        val addr1 = getAccount("acct0")
        val addr2 = getAccount("acct1")
        val addr3 = getAccount("acct2")

        return Triple(addr1, addr2, addr3)
    }

    private fun getAccount(key: String): String {
        val cr = objectMapper.readValue(doGet(key), CipherWithParams::class.java)
        return String(doFinalDeCrypt( db_encryption_key!!, cr).extractArray(), Charsets.UTF_8)
    }


    fun getDbEncryptionKey(storePass: CharArray, keyPass: CharArray): SecretKey {

        val keyStore = KeyStore.getInstance("BKS", "BC");
        val inStream = FileInputStream(consts!!.KEY_STORE_PATH.toFile())
        keyStore.load(inStream, storePass);

        val keyPass = KeyStore.PasswordProtection(keyPass)

        val entry: KeyStore.SecretKeyEntry = keyStore.getEntry(ALIAS, keyPass) as KeyStore.SecretKeyEntry;
        return entry.secretKey
    }

}




