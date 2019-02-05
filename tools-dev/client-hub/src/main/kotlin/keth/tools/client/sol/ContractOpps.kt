package keth.tools.client.sol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.client.GlobalConstants
import keth.tools.client.db.DbManager
import keth.tools.wrappers.Simple
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.web3j.codegen.SolidityFunctionWrapper
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.AbiDefinition
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Files
import org.web3j.utils.Strings
import java.io.File
import java.math.BigInteger
import java.nio.file.Path

abstract class ContractBase {


    val objectMapper = jacksonObjectMapper()

    abstract val abiDirPath: Path
    abstract val  abiDirOutPath: Path
    val basePackageName = "keth.tools.wrappers"

    abstract val  binFile: File
    abstract val  abiFile: File
    abstract val  outFile: File

    var contractName:String? = null
    var className:String? = null

    val web3j = Web3j.build(HttpService("http://127.0.0.1:8545"));


    fun generateWrapper() {
        var binBytes = Files.readBytes( binFile)
        val binary = String(binBytes)
        var abiBytes = Files.readBytes( abiFile)
        val abi = String(abiBytes)
        val functionDefinitions = loadContractDefinition( abiFile!!)


        if (functionDefinitions.isEmpty()) {
            println("Unable to parse input ABI file")
        } else {
            contractName =  getFileNameNoExtension( abiFile!!.getName())
            className = Strings.capitaliseFirstLetter(contractName)

            SolidityFunctionWrapper(true).generateJavaFiles(
                    contractName, binary, abi,  abiDirOutPath.toString(),  basePackageName)
            println("File written to " + abiDirOutPath.toString() + "\n")
        }

    }

    fun getFileNameNoExtension(fileName: String): String {
        val splitName = fileName.split("\\.(?=[^.]*$)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray();
        return splitName[0]
    }

    fun  loadContractDefinition(absFile: File):Array<AbiDefinition> {
        val functionDefinitions:Array<AbiDefinition> =  objectMapper.readValue(absFile,  object : TypeReference<Array<AbiDefinition>>() {});
        return functionDefinitions
    }


}

/**
 * run test to generate wrapper
 */
@Service
class ContractOperations : ContractBase() {

    @Autowired
    lateinit var consts: GlobalConstants

    @Autowired
    lateinit var db: DbManager


    override val abiDirPath: Path
        get() =consts.SOL_PATH

    override val abiDirOutPath: Path
        get() = consts.BASE_PATH.resolve("pantheon/tools-dev/client-hub/src/main/java")

    override val outFile: File
        get() = abiDirOutPath.toFile()

    override val abiFile: File
        get() = abiDirPath.resolve("Simple.abi").toFile()

    override val binFile: File
        get() =  abiDirPath.resolve("Simple.bin").toFile()

    //  db.initDb() called in controller
    fun deployContract(): String  {
        val keyPair = consts.loadKeyFile(consts.BOOT_NODE_PATH)
        val priKeyBytes =  keyPair.privateKey.encodedBytes
        val pairForCredentials =   ECKeyPair.create(priKeyBytes.extractArray())

        val contract = Simple.deploy(web3j, Credentials.create(pairForCredentials),
                BigInteger.valueOf(4000000),  BigInteger.valueOf(4700000)).send();
        val contractAddress = contract.contractAddress

        db.storeContractAddress(consts.CONTRACT_ADDRESS_KEY, contractAddress)

        return contractAddress;
    }



}


fun main() {


}



