package keth.tools

import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
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

    val genesisTemplateOutPath  = panBaseProject.resolve("tools-dev/client/src/main/resources/prichainGenesis.json")


    val sealersPath = "sealers"

    val bootNodePath = chainBasePath.resolve("node1/cdata")
    val node2Path = chainBasePath.resolve("node2/cdata")
    val node3Path = chainBasePath.resolve("node3/cdata")

    val bootNodeScriptPath = chainBasePath.resolve("node1")
    val node2ScriptPath = chainBasePath.resolve("node2")
    val node3ScriptPath = chainBasePath.resolve("node3")


    val bootNodeKeyPubPath = "publicKey"

    val genesisPath = chainBasePath.resolve("prichainGenesis.json")


    //== ws EVENTBUS topics

   val REMOVE_SUBSCRIPTIONS_ADDRESS = "removeSubscription";



}

//Loaded key 0xa... from /home/pathy/chains/private/node1/cdata/key

object KeyUtils {


    fun generateKeysForNodes() :Triple<String,String,String> {
        val bNodeKey  = SECP256K1.KeyPair.generate();

        var bNode = Constants.bootNodePath.resolve("key")     .toFile()

        if(!bNode.exists()){
            bNode.createNewFile();
        }
        bNodeKey.privateKey.store(bNode)
        val bNodeECKeyPair =   ECKeyPair.create( bNodeKey.privateKey.encodedBytes.extractArray())
        val bNodeCredentials = Credentials.create(bNodeECKeyPair)


        val nodeKey2  = SECP256K1.KeyPair.generate();
        var node2 = Constants.node2Path.resolve("key")     .toFile()

        if(!node2.exists()){
            node2.createNewFile();
        }
        nodeKey2.privateKey.store(node2)
        val nodeKey2ECKeyPair =   ECKeyPair.create( nodeKey2.privateKey.encodedBytes.extractArray())
        val nodeKey2Credentials = Credentials.create(nodeKey2ECKeyPair)


        val nodeKey3  = SECP256K1.KeyPair.generate();
        var node3 = Constants.node3Path.resolve("key")     .toFile()

        if(!node3.exists()){
            node3.createNewFile();
        }
        nodeKey3.privateKey.store(node3)
        val nodeKey3ECKeyPair =   ECKeyPair.create( nodeKey3.privateKey.encodedBytes.extractArray())
        val nodeKey3Credentials = Credentials.create(nodeKey3ECKeyPair)

        var g = Constants.bootNodePath.resolve(Constants.bootNodeKeyPubPath).toFile()

        if(!g.exists()){
            g.createNewFile();
        }

        try {
            Files.newBufferedWriter(g.toPath(), StandardCharsets.UTF_8)
                    .use({ fileWriter -> fileWriter.write(bNodeKey.getPublicKey().toString()) })
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Triple(bNodeCredentials!!.address, nodeKey2Credentials!!.address, nodeKey3Credentials!!.address )
    }




    fun loadKeyFile(path: Path): SECP256K1.KeyPair {

      return  loadKeyPair(Constants.bootNodePath)
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



