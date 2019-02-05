package keth.tools.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import tech.pegasys.pantheon.crypto.SECP256K1
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


@Configuration
class GlobalConstants {

    @Value("\${crypto.db.key.pass}")
    val DBCRYPTO_KEY_PASS: String? = null

    @Value("\${crypto.db.j12key.pass}")
    val DB_CRYPTO_KEY_STORE_PASS: String? = null

    val  BASE_PATH = Paths.get(System.getProperty("user.home"))

    val CHAIN_BASE_PATH = BASE_PATH.resolve("chains/private")
    val SECURE_DB_PATH =  CHAIN_BASE_PATH.resolve(".secure")
    val KEY_STORE_PATH =  SECURE_DB_PATH.resolve("key_str.jks")
    val WALLET_DB_PATH =  CHAIN_BASE_PATH.resolve("wallets")
    val SOL_PATH =  CHAIN_BASE_PATH.resolve("sol")
    val BOOT_NODE_PATH = CHAIN_BASE_PATH.resolve("node1/cdata")

    val CONTRACT_ADDRESS_KEY="c_addres"


    fun loadKeyFile(path: Path): SECP256K1.KeyPair {

        return  loadKeyPair(path)
    }

    @Throws(IOException::class)
    fun loadKeyPair(homeDirectory: Path): SECP256K1.KeyPair {
        val keyFile =   homeDirectory.resolve("key").toFile()
        return SECP256K1.KeyPair.load(keyFile)
    }


}