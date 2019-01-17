package keth.tools.account

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.Constants
import keth.tools.db.CipherWithParams
import keth.tools.db.DbCrypto
import keth.tools.db.DbManager
import org.web3j.crypto.Keys
import org.web3j.crypto.Wallet
import tech.pegasys.pantheon.crypto.SECP256K1
import tech.pegasys.pantheon.ethereum.p2p.rlpx.handshake.ecies.ECIESEncryptionEngine
import tech.pegasys.pantheon.util.bytes.BytesValue


abstract class WalletBase {
    val keys = Keys.createEcKeyPair();
    val keyPair = SECP256K1.KeyPair.generate()
    // hard coded for now will come from gui
    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')
    val objectMapper = jacksonObjectMapper()


    init {
        DbManager.createWalletStore(Constants.walletDbPath)
        DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))

    }

}

object WalletProvider : WalletBase() {

    fun createJsonWallet(menomic: String): String {
        val walletFile = Wallet.createStandard(menomic, keys)
        return objectMapper.writeValueAsString(walletFile)
    }

    fun getRocksDbWallet(menomic: String) {
        val cipherResult = objectMapper.readValue(DbManager.doGet(menomic), CipherWithParams::class.java )
        val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
        val result =  DbCrypto.doFinalDeCrypt(key, cipherResult)
        println(String(result.extractArray(), Charsets.UTF_8 ))
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