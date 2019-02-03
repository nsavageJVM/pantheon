package keth.tools.account

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.Constants
import keth.tools.db.CipherWithParams
import keth.tools.db.DbCrypto
import keth.tools.db.DbManager
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Wallet
import org.web3j.crypto.WalletFile
import tech.pegasys.pantheon.crypto.SECP256K1
import tech.pegasys.pantheon.util.bytes.BytesValue


abstract class WalletBase {
    val keys = Keys.createEcKeyPair();
    val keyPair = SECP256K1.KeyPair.generate()
    // hard coded for now will come from gui
    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')
    val objectMapper = jacksonObjectMapper()


    val acctCB= "0x7ff4cb4cc4173e2d45a5a4b7fb40876c3c46ba4f"


    init {
        DbManager.initDb( )
        DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))

    }

}

object WalletProvider : WalletBase() {

    fun createJsonWallet(menomic: String): String {
        val walletFile = Wallet.createStandard(menomic, keys)
        return objectMapper.writeValueAsString(walletFile)
    }

    fun getRocksDbWallet(menomic: String) :WalletFile {
        val cipherResult = objectMapper.readValue(DbManager.doGet(menomic), CipherWithParams::class.java )
        val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
        val result =  DbCrypto.doFinalDeCrypt(key, cipherResult)
        val walletAsJson = String(result.extractArray(), Charsets.UTF_8 )
        val wFile =  objectMapper.readValue(walletAsJson, WalletFile::class.java)

        println(" address:${wFile.address}")
        val ecKeys = Wallet.decrypt(menomic, wFile )
        return wFile;

    }

    fun createEncryptedRocksDbWallet(menomic: String) {

        val rawData = createJsonWallet(menomic)
        val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
        val cryptoResult = DbCrypto.doFinalEncrypt(key, BytesValue.wrap(rawData.toByteArray()))
        DbManager.doPutStrings(menomic, objectMapper.writeValueAsString(cryptoResult) )
    }


}

fun main(args: Array<String>) {

    WalletProvider.createEncryptedRocksDbWallet("this is pop2")
    WalletProvider.getRocksDbWallet("this is pop2")

}