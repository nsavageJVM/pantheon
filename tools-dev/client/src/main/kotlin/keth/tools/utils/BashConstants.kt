package keth.tools.utils

import keth.tools.Constants
import keth.tools.KeyUtils
import tech.pegasys.pantheon.crypto.SECP256K1
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


abstract class BashConstants {

    val SHEBANG = "#!/bin/sh"
    val NET_ID = 123
    val BOOT_NODE_PORT = 30303
    val NODE_2_PORT = 30304
    val NODE_3_PORT = 30305
    val BOOT_NODE_HOST= "127.0.0.1"

    val RUN_BootNode ="""
      |../peg/bin/pantheon --datadir=cdata --genesis=%s --network-id %s  --rpc-enabled --ws-enabled  --host-whitelist=* """

    val BOOT_ENODE= "enode://%s@$BOOT_NODE_HOST:%s"

    val RUN_Node ="""
        |../peg/bin/pantheon  --datadir=cdata   --genesis=%s --network-id %s --bootnodes=%s --p2p-listen=127.0.0.1:%s
        """


    protected fun genBootNodeScript():String {

        val script =  BashProvider.RUN_BootNode.format( Constants.genesisPath,  NET_ID)
        return "${BashProvider.SHEBANG} \n${script.trimMargin()}"
    }

    protected  fun genEnode(key:SECP256K1.KeyPair ):String {

        return BashProvider.BOOT_ENODE.format(key.publicKey.toString().substring(2), BOOT_NODE_PORT)
    }



}

enum class TYPE { BOOT, NODE2, NODE3 }

object BashProvider :BashConstants() {

    fun writeScript(path: Path, type: TYPE) {

        try {
            Files.newBufferedWriter(path, StandardCharsets.UTF_8)
                    .use({ fileWriter -> fileWriter.write( generateNodeScripts(type)) })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun generateNodeScripts( type:TYPE):String {

        when(type) {
            TYPE.BOOT -> {
                return genBootNodeScript()
            }
            TYPE.NODE2 -> {
                return getNodeScript(type)
            }
            TYPE.NODE3 -> {
                return getNodeScript(type)
            }
            else -> return "Unknown Type"
        }
    }


    private fun getNodeScript( type:TYPE):String {

        val key = KeyUtils.loadKeyFile(Constants.bootNodeKeyPath)
        val enode = genEnode(key)
        when(type) {
            TYPE.NODE2 -> {
                val script =  RUN_Node.format( Constants.genesisPath, NET_ID, enode, NODE_2_PORT )
                return "$SHEBANG \n${script.trimMargin()}"

            }
            TYPE.NODE3 -> {
                val script =  RUN_Node.format( Constants.genesisPath, NET_ID, enode, NODE_3_PORT )
                return "$SHEBANG \n${script.trimMargin()}"
            }
            else -> return "Unknown Type"
        }
    }

}


