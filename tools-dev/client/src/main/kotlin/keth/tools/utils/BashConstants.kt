package keth.tools.utils

import keth.tools.Constants
import keth.tools.KeyUtils
import keth.tools.ResourceUtils
import tech.pegasys.pantheon.crypto.SECP256K1
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path


abstract class BashConstants {

    val SHEBANG = "#!/bin/sh"
    val NET_ID = 123
    val BOOT_NODE_PORT = 30303
    val NODE_2_PORT = 30304
    val NODE_3_PORT = 30305
    val BOOT_NODE_HOST = "127.0.0.1"

    val RUN_BootNode = """
      |../peg/bin/pantheon --datadir=cdata --genesis=%s --network-id %s --miner-enabled=true --miner-coinbase=%s  --rpc-enabled=true --ws-enabled=true  --host-whitelist=* --rpc-cors-origins=all """

    val BOOT_ENODE = "enode://%s@$BOOT_NODE_HOST:%s"

    val RUN_Node = """
        |../peg/bin/pantheon  --datadir=cdata  --genesis=%s  --miner-enabled=true --miner-coinbase=%s --network-id %s --bootnodes=%s --p2p-listen=127.0.0.1:%s
        """

    protected fun genBootNodeScript(addr: String): String {

        val script = BashProvider.RUN_BootNode.format(Constants.genesisPath, NET_ID, addr)
        return "${BashProvider.SHEBANG} \n${script.trimMargin()}"
    }

    protected fun genEnode(key: SECP256K1.KeyPair): String {

        return BashProvider.BOOT_ENODE.format(key.publicKey.toString().substring(2), BOOT_NODE_PORT)
    }

}

enum class TYPE { BOOT, NODE2, NODE3 }

object BashProvider : BashConstants() {

    fun writeScript(path: Path, addr:String, type: TYPE) {

        try {
            Files.newBufferedWriter(path, StandardCharsets.UTF_8)
                    .use({ fileWriter -> fileWriter.write(generateNodeScripts(addr, type)) })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun generateNodeScripts( addr:String, type: TYPE): String {

        when (type) {
            TYPE.BOOT -> {
                return genBootNodeScript(addr)
            }
            TYPE.NODE2 -> {
                return getNodeScript( addr, type)
            }
            TYPE.NODE3 -> {
                return getNodeScript( addr, type)
            }
            else -> return "Unknown Type"
        }
    }


    private fun getNodeScript(addr:String, type: TYPE): String {

        val key = KeyUtils.loadKeyFile(Constants.bootNodePath)
        val enode = genEnode(key)
        when (type) {
            TYPE.NODE2 -> {
                val script = RUN_Node.format(Constants.genesisPath, addr, NET_ID, enode, NODE_2_PORT)
                return "$SHEBANG \n${script.trimMargin()}"

            }
            TYPE.NODE3 -> {
                val script = RUN_Node.format(Constants.genesisPath, addr, NET_ID, enode, NODE_3_PORT)
                return "$SHEBANG \n${script.trimMargin()}"
            }
            else -> return "Unknown Type"
        }
    }


}


object GenesisProvider {

    fun updateGenesisExtraData(addrs: Triple<String, String, String>) {

        // val extraDataString = "${addrs.first.substring(2)}${addrs.second.substring(2)}${addrs.third.substring(2)}"
        val extraDataString = "${addrs.second.substring(2)}${addrs.third.substring(2)}"
        var input = genesisTemplateConfig("Genesis.tmpl")
        val inputAsString = input!!.bufferedReader().use { it.readText() }

        val genesis = inputAsString.replace("%extraData%".toRegex(), extraDataString)
                                    .replace("%A1%".toRegex(),addrs.first.substring(2) )
                                    .replace("%A2%".toRegex(),addrs.second.substring(2) )
                                    .replace("%A3%".toRegex(),addrs.third.substring(2) )
        writeGenesis(genesis)

    }

    private fun genesisTemplateConfig(template: String): InputStream? {
        try {
            val url = ResourceUtils.getResource(template).toURI().toURL()
            return url.openStream()
        } catch (e: URISyntaxException) {
            throw IllegalStateException("Unable to get test genesis config $template")
        } catch (e: IOException) {
            throw IllegalStateException("Unable to get test genesis config $template")
        }

    }


    private fun writeGenesis(tmpl: String) {

        var g = Constants.genesisTemplateOutPath.toFile()

        if (!g.exists()) {
            g.createNewFile();
        }

        try {
            Files.newBufferedWriter(g.toPath(), StandardCharsets.UTF_8)
                    .use({ fileWriter -> fileWriter.write(tmpl) })
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }
}


fun main() {
    GenesisProvider.updateGenesisExtraData(Triple("0x44b9b2561360158775715c60d883f1948ad05846", "0x2640f349cbe433a2d4b9c241892cbad70445d6e2", "0x3c03f613654a92320a21bfd6dabe19a7bbfbc54b"))

}


