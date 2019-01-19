package keth.tools

import tech.pegasys.pantheon.crypto.SECP256K1
import java.io.IOException
import java.net.URL
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


    //== ws EVENTBUS topics

   val REMOVE_SUBSCRIPTIONS_ADDRESS = "removeSubscription";



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

      return  loadKeyPair(Constants.bootNodeKeyPath)
    }

    @Throws(IOException::class)
    fun loadKeyPair(homeDirectory: Path): SECP256K1.KeyPair {
       val keyFile =   homeDirectory.resolve("key").toFile()
        return SECP256K1.KeyPair.load(keyFile)
    }
}


object ResourceUtils {

  fun getResource(name: String ) : URL {


        var url = Thread.currentThread().getContextClassLoader().getResource(name);

        if (url == null) {
            url = ResourceUtils::class.java.getResource(name);
         }
        if (url == null) {
            url = ResourceUtils::class.java.getClassLoader().getResource(name);
        }

        if (url == null) {
            url = ClassLoader.getSystemResource(name);
        }

        return url;
    }
}



