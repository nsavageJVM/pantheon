package keth.tools

import tech.pegasys.pantheon.controller.KeyPairUtil
import tech.pegasys.pantheon.crypto.SECP256K1
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Constants {

    val chainBasePath = Paths.get(System.getProperty("user.home"),"chains/private")
    val secureDbPath =  chainBasePath.resolve(".secure")
    val keySstorePath =  secureDbPath.resolve("key_str.jks")

    val walletDbPath =  chainBasePath.resolve("wallets")

    val panBaseProject = Paths.get(System.getProperty("user.home"),"pantheon" );
    val bootNodeKeyPath = Paths.get(System.getProperty("user.home"),"chains/private/node1/cdata" );
    val bootNodeKeyPubPath = "publicKey"

    val bootNodePath = chainBasePath.resolve("node1")
    val bootNode2 = chainBasePath.resolve("node2")
    val bootNode3 = chainBasePath.resolve("node3")

    val genesisPath = chainBasePath.resolve("prichainGenesis.json")


}


object KeyUtils {

    fun generateKeysForBootNode() {
        val key  = SECP256K1.KeyPair.generate();


        var f = Constants.bootNodeKeyPath.resolve("key")     .toFile()

        if(!f.exists()){
            f.createNewFile();
        }
        key.privateKey.store(f)

        var g = Constants.bootNodeKeyPath.resolve(Constants.bootNodeKeyPubPath).toFile()

        if(!g.exists()){
            g.createNewFile();
        }

        try {
            Files.newBufferedWriter(g.toPath(), StandardCharsets.UTF_8)
                    .use({ fileWriter -> fileWriter.write(key.getPublicKey().toString()) })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun loadKeyFile(path: Path): SECP256K1.KeyPair {

      return  KeyPairUtil.loadKeyPair(Constants.bootNodeKeyPath)

    }
}