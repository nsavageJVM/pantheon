package keth.tools.db

import keth.tools.Constants
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tech.pegasys.pantheon.crypto.SECP256K1
import tech.pegasys.pantheon.crypto.SecureRandomProvider
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.Security
import java.security.spec.ECGenParameterSpec


abstract class CryptoBase {

    val CURVE_NAME = "secp256k1";
    val ecGenParameterSpec = ECGenParameterSpec(CURVE_NAME)
    lateinit var KEY_PAIR_GENERATOR: KeyPairGenerator

    init {
        Security.addProvider(BouncyCastleProvider())
        try {
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("ECDSA", "BC")
            KEY_PAIR_GENERATOR.initialize( ecGenParameterSpec, SecureRandomProvider.createSecureRandom())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }


    }

}

object EncryptForDb : CryptoBase() {


    lateinit var privateKeyValue: BigInteger

    fun getPrivateKeyPanFormat(): SECP256K1.PrivateKey {

        val rawKeyPair = KEY_PAIR_GENERATOR.generateKeyPair()
        val privateKey = rawKeyPair.private as BCECPrivateKey
        privateKeyValue = privateKey.d
        return SECP256K1.PrivateKey.create(privateKeyValue)
    }


}


fun main(args: Array<String>) {

     EncryptForDb.getPrivateKeyPanFormat().store( Constants.secureDbPath.resolve("dbdata.key").toFile())


}
