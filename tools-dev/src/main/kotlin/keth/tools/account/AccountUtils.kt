package keth.tools.account

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Wallet
import tech.pegasys.pantheon.crypto.SECP256K1
import java.math.BigInteger


abstract class WalletBase {

    fun getNewKeyAsWeb3Key():ECKeyPair {
        val key = SECP256K1.KeyPair.generate();
        val priKeyAsBigInt : BigInteger = key.privateKey.d
        val pubKeyAsBigInt = BigInteger(key.publicKey.toString().substring(2), 16)

        return ECKeyPair(priKeyAsBigInt, pubKeyAsBigInt)
    }



}

object WalletProvider : WalletBase() {

    fun createRocksDbWallet(menomic:String) {

        val walletFile =  Wallet.createStandard(menomic, getNewKeyAsWeb3Key())

        val objectMapper =  jacksonObjectMapper()

        val jsonStr =  objectMapper.writeValueAsString(walletFile)
        println(jsonStr)

    }
}


fun main(args : Array<String>) {

    WalletProvider.createRocksDbWallet("this is pop")

}