package keth.tools.account

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.Constants
import keth.tools.db.DbManager
import org.web3j.crypto.Keys
import org.web3j.crypto.Wallet
import tech.pegasys.pantheon.crypto.SECP256K1
import tech.pegasys.pantheon.ethereum.p2p.rlpx.handshake.ecies.ECIESEncryptionEngine
import tech.pegasys.pantheon.util.bytes.BytesValue



abstract class WalletBase {
    val key = Keys.createEcKeyPair();
    val keyPair = SECP256K1.KeyPair.generate()
    var  engine: ECIESEncryptionEngine
    init{
        DbManager.createWalletStore(Constants.walletDbPath)
        engine = ECIESEncryptionEngine.forEncryption(keyPair.publicKey);
    }


}

object WalletProvider : WalletBase() {

    fun createJsonWallet(menomic:String):String {

        val walletFile =  Wallet.createStandard(menomic, key)

        val objectMapper =  jacksonObjectMapper()

        return  objectMapper.writeValueAsString(walletFile)

    }


    fun getRocksDbWallet(menomic: String) {

      val result =  DbManager.doGet(menomic)
        println(result)

    }

    fun createEncryptedRocksDbWallet(menomic:String) {

     val rawData = createJsonWallet(menomic)
     val eVal: BytesValue = engine.encrypt( BytesValue.wrap(rawData.toByteArray(Charsets.UTF_8)))
     DbManager.doPutBytes("test", eVal)

    }


    fun getEncryptedRocksDbWallet(menomic:String) {

        val eValResult: BytesValue =  DbManager.getBytes(menomic)
        println(String(eValResult.extractArray(), Charsets.UTF_8))


    }





}

fun main(args : Array<String>) {


    WalletProvider.createEncryptedRocksDbWallet("this is pop")

    WalletProvider.getRocksDbWallet(("this is pop"))




}