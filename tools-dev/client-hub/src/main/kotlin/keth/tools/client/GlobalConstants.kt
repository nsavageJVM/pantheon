package keth.tools.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.nio.file.Paths


@Configuration
class GlobalConstants {

    @Value("\${crypto.db.key.pass}")
    val dbCryptoKeyPass: String? = null

    @Value("\${crypto.db.j12key.pass}")
    val dbCryptoKeyStorePass: String? = null

    val CHAIN_BASE_PATH = Paths.get(System.getProperty("user.home"),"chains/private")

    val WALLET_DB_PATH =  CHAIN_BASE_PATH.resolve("wallets")

    val SECURE_DB_PATH =  CHAIN_BASE_PATH.resolve(".secure")
    val KEY_STORE_PATH =  SECURE_DB_PATH.resolve("key_str.jks")
}