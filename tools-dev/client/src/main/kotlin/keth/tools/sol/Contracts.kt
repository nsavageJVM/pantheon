package keth.tools.sol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.Constants
import keth.tools.KeyUtils
import keth.tools.wrappers.SimpleStorage
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
    // paths, files, constants
    val abiDirPath = Constants.panBaseProject.resolve("tools-dev/client/build/resources/main/solidity")
    val abiDirOutPath = Constants.panBaseProject.resolve("tools-dev/client/src/main/java")
    val basePackageName = "keth.tools.wrappers"

    var binFile: File? = null
    var abiFile: File? = null
    var outFile: File? = null

    var contractName:String? = null
    var className:String? = null

    val objectMapper = jacksonObjectMapper()

    val web3j = Web3j.build(HttpService("http://127.0.0.1:8545"));
    // Constants.bootNodePath  looks for file "key" in path
    val keyPair = KeyUtils.loadKeyFile(Constants.bootNodePath)

    var chainCredentials:Credentials? = null

    fun createWeb3Credentials() {
        val priKeyBytes =  keyPair.privateKey.encodedBytes

        val pairForCredentials =   ECKeyPair.create(priKeyBytes.extractArray())
        chainCredentials = Credentials.create(pairForCredentials)
    }

    fun generateWrapper() {
        var binBytes = Files.readBytes(Generator.binFile)
        val binary = String(binBytes)
        var abiBytes = Files.readBytes(Generator.abiFile)
        val abi = String(abiBytes)
        val functionDefinitions = Generator.loadContractDefinition(Generator.abiFile!!)


        if (functionDefinitions.isEmpty()) {
            println("Unable to parse input ABI file")
        } else {
            contractName = Generator.getFileNameNoExtension(Generator.abiFile!!.getName())
            className = Strings.capitaliseFirstLetter(contractName)

            SolidityFunctionWrapper(true).generateJavaFiles(
                    contractName, binary, abi, Generator.abiDirOutPath.toString(), Generator.basePackageName)
            println("File written to " + Generator.abiDirOutPath.toString() + "\n")
        }

    }

    fun  loadContractDefinition(absFile: File):Array<AbiDefinition> {
        val functionDefinitions:Array<AbiDefinition> =  Generator.objectMapper.readValue(absFile,  object : TypeReference<Array<AbiDefinition> >() {});
        return functionDefinitions
    }

}

object Generator : ContractBase() {

    fun setABIFiles(abiPath: Path, binPath: Path, outPath: Path) {
        abiFile = abiPath.toFile()
        binFile = binPath.toFile()
        outFile = outPath.toFile()

    }

    fun getFileNameNoExtension(fileName: String): String {
        val splitName = fileName.split("\\.(?=[^.]*$)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray();
        return splitName[0]
    }

    fun deployContract() {
     val contract = SimpleStorage.deploy(web3j, chainCredentials,
             BigInteger.valueOf(4000000),  BigInteger.valueOf(4700000)).send();
        val contractAddress = contract.contractAddress

        println("contractAddress = ${contractAddress}")

    }

}


fun main() {

    Generator.createWeb3Credentials()
    Generator.setABIFiles(Generator.abiDirPath.resolve("SimpleStorage.abi"),
                                Generator.abiDirPath.resolve("SimpleStorage.bin"), Generator.abiDirOutPath)
    Generator.generateWrapper()
    Generator.deployContract()


}