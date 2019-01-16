package keth.tools.db

import keth.tools.Constants
import keth.tools.crypto.CryptoUtils
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tech.pegasys.pantheon.crypto.SECP256K1
import tech.pegasys.pantheon.crypto.SecureRandomProvider
import tech.pegasys.pantheon.util.bytes.BytesValue
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Security
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


abstract class CryptoBase {

    val ALIAS = "secretkey"

    val CURVE_NAME = "secp256k1";
    val ecGenParameterSpec = ECGenParameterSpec(CURVE_NAME)
    lateinit var KEY_PAIR_GENERATOR: KeyPairGenerator
    var privateKeyValue: BigInteger? = null
    var privateKey: SECP256K1.PrivateKey? = null

    init {
        Security.addProvider(BouncyCastleProvider())
        try {
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("ECDSA", "BC")
            KEY_PAIR_GENERATOR.initialize(ecGenParameterSpec, SecureRandomProvider.createSecureRandom())

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /** probably for transactions */
    fun setNewPrivateKeyPanFormat() {

        val rawKeyPair = DbCrypto.KEY_PAIR_GENERATOR.generateKeyPair()
        val rawPrivateKey = rawKeyPair.private as BCECPrivateKey
        privateKeyValue = rawPrivateKey.d
        privateKey = SECP256K1.PrivateKey.create(privateKeyValue)
    }

    fun getPublicKey(): SECP256K1.PublicKey {
        if (privateKey == null) {
            setNewPrivateKeyPanFormat()
        }

        return SECP256K1.PublicKey.create(privateKey)
    }

    fun getPanKeyPair(): SECP256K1.KeyPair {
        if (privateKey == null) {
            setNewPrivateKeyPanFormat()
        }
        return SECP256K1.KeyPair(privateKey, getPublicKey())
    }

}

// so we can pass the iv around
data class CipherWithParams(val iv: ByteArray, val data: ByteArray )

object DbCrypto : CryptoBase() {

    val salt = ByteArray(64)

    init {
        SecureRandomProvider.publicSecureRandom().nextBytes(salt)
    }

    fun genDbEncryptionKey(password: CharArray): SecretKey {
        val generator = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        val hmacKey = generator.generateSecret(PBEKeySpec(password, salt, 1024, 256));
        return SecretKeySpec(hmacKey.getEncoded(), "AES");
    }

    fun persistKeyStore(storePass: CharArray, keyPass: CharArray, secretKey: SecretKey) {

        val keyStore = KeyStore.getInstance("BKS", "BC");
        keyStore.load(null, null);
        // chain parameter is null normally certs stuff
        keyStore.setKeyEntry(ALIAS, secretKey, keyPass, null);

        Constants.secureDbPath.toFile().mkdirs()

        var fileStore = FileOutputStream(Constants.keySstorePath.toFile())
        keyStore.store(fileStore, storePass);
        fileStore.flush();

    }

    fun getDbEncryptionKey(storePass: CharArray, keyPass: CharArray): SecretKey {

        val keyStore = KeyStore.getInstance("BKS", "BC");
        val inStream = FileInputStream(Constants.keySstorePath.toFile())
        keyStore.load(inStream, storePass);

        val keyPass = KeyStore.PasswordProtection(keyPass)

        val entry: KeyStore.SecretKeyEntry = keyStore.getEntry(ALIAS, keyPass) as KeyStore.SecretKeyEntry;
        return entry.secretKey
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


object Signer : CryptoBase() {

    val params = SECNamedCurves.getByName(CURVE_NAME)
    lateinit var CURVE: ECDomainParameters

    init {
        CURVE = ECDomainParameters(params.curve, params.g, params.n, params.h)
    }

    fun signBytes(data: BytesValue, keyPair: SECP256K1.KeyPair): CryptoUtils.AccountSignature {
        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()));
        val privKeyParams = ECPrivateKeyParameters(
                BigInteger(1, keyPair.getPrivateKey().getEncodedBytes().getArrayUnsafe()), CURVE);
        signer.init(true, privKeyParams)
        val components = signer.generateSignature(data.getArrayUnsafe())
        val r = components[0]
        var s = components[1]

        var recId = -1;
        val publicKeyBI = BigInteger(1, keyPair.getPublicKey().getEncodedBytes().getArrayUnsafe())

        for (i in 0..3) {
            val k = CryptoUtils.recoverFromSignature(i, r, s, data)
            if (k != null && k == publicKeyBI) {
                recId = i
                break
            }
        }

        return CryptoUtils.AccountSignature(r, s, recId)
    }

    fun verify(mssg: BytesValue, signature: CryptoUtils.AccountSignature, pubKey: SECP256K1.PublicKey): Boolean {

        return CryptoUtils.verify(mssg, signature, pubKey)

    }


}


fun main(args: Array<String>) {

    println("start sign test")
    val mssg = "something to sign"
    val sig = Signer.signBytes(BytesValue.wrap(mssg.toByteArray()), DbCrypto.getPanKeyPair())
    val result = Signer.verify(BytesValue.wrap(mssg.toByteArray()), sig, DbCrypto.getPanKeyPair().publicKey)
    println("sign test worked = "+result)

    val keyPass = charArrayOf('T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'p', 'o', 'p')
    val strPass = charArrayOf('y', 'e', 'a', 'h', ' ', 'y', 'e', 'a', 'h')

    println("start database crypto test")
    // create java keystore for database crypto security
    DbCrypto.persistKeyStore(strPass, keyPass, DbCrypto.genDbEncryptionKey(keyPass))
    val key = DbCrypto.getDbEncryptionKey(strPass, keyPass)
    val data =  "the text to encrypt".toByteArray()
    val cryptoResult = DbCrypto.doFinalEncrypt(key, BytesValue.wrap(data))
    val plainText = DbCrypto.doFinalDeCrypt(key, cryptoResult)

    println( "db test worked if text = 'the text to encrypt' :::: "+ String(  plainText.extractArray(), Charsets.UTF_8 ))

}
