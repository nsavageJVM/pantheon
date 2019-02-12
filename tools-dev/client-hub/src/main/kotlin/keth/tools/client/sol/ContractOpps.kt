package keth.tools.client.sol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.client.GlobalConstants
import keth.tools.client.db.DbManager
import keth.tools.wrappers.PowerBudgetToken
import keth.tools.wrappers.SimpleStorage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.web3j.codegen.SolidityFunctionWrapper
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.AbiDefinition
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Files
import org.web3j.utils.Strings
import java.io.File
import java.math.BigInteger
import java.nio.file.Path

data class Address(var solAddr:String, var solName:String)


abstract class ContractBase {


    val objectMapper = jacksonObjectMapper()

    abstract val abiDirPath: Path
    abstract val abiDirOutPath: Path
    val basePackageName = "keth.tools.wrappers"

    abstract val binFile: File
    abstract val abiFile: File
    abstract val outFile: File

    var contractName: String? = null
    var className: String? = null

    val web3j = Web3j.build(HttpService("http://127.0.0.1:8545"));

    val G_PRICE = BigInteger.valueOf(4700000)
    val G_LIMIT = BigInteger.valueOf(4000000)
    val STATIC_GAS_PROVIDER = StaticGasProvider(G_PRICE, G_LIMIT)



    fun generateWrapper() {
        var binBytes = Files.readBytes(binFile)
        val binary = String(binBytes)
        var abiBytes = Files.readBytes(abiFile)
        val abi = String(abiBytes)
        val functionDefinitions = loadContractDefinition(abiFile!!)


        if (functionDefinitions.isEmpty()) {
            println("Unable to parse input ABI file")
        } else {
            contractName = getFileNameNoExtension(abiFile!!.getName())
            className = Strings.capitaliseFirstLetter(contractName)

            SolidityFunctionWrapper(true).generateJavaFiles(
                    contractName, binary, abi, abiDirOutPath.toString(), basePackageName)
            println("File written to " + abiDirOutPath.toString() + "\n")
        }

    }

    fun getFileNameNoExtension(fileName: String): String {
        val splitName = fileName.split("\\.(?=[^.]*$)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray();
        return splitName[0]
    }

    fun loadContractDefinition(absFile: File): Array<AbiDefinition> {
        val functionDefinitions: Array<AbiDefinition> = objectMapper.readValue(absFile, object : TypeReference<Array<AbiDefinition>>() {});
        return functionDefinitions
    }


}

/**
 * run test to generate wrapper
 */
@Service
class SimpleStorageCodeGen : ContractBase() {

    @Autowired
    lateinit var consts: GlobalConstants

    @Autowired
    lateinit var db: DbManager

    // need GlobalConstants injected so create in base class
    override val abiDirPath: Path
        get() = consts.SOL_PATH

    override val abiDirOutPath: Path
        get() = consts.BASE_PATH.resolve("pantheon/tools-dev/client-hub/src/main/java")

    override val outFile: File
        get() = abiDirOutPath.toFile()

    override val abiFile: File
        get() = abiDirPath.resolve("SimpleStorage.abi").toFile()

    override val binFile: File
        get() = abiDirPath.resolve("SimpleStorage.bin").toFile()

    //  db.initDb() called in controller
    fun deployContract(): Address {

        val contract = SimpleStorage.deploy(web3j, getCredentials(), STATIC_GAS_PROVIDER).send();
        val contractAddress = contract.contractAddress

        db.storeContractAddress(consts.CONTRACT_ADDRESS_KEY_SIMPLE, contractAddress)



        return  Address(contractAddress,SimpleStorage::class.java.simpleName);


    }

    fun getDeployedContract(c_addr:String): SimpleStorage {

        val simple = SimpleStorage
                .load( c_addr, Web3j.build(HttpService()), getCredentials(), STATIC_GAS_PROVIDER)
        return simple
    }

    //== private

    fun getCredentials(): Credentials {
        val keyPair = consts.loadKeyFile(consts.BOOT_NODE_PATH)
        val priKeyBytes = keyPair.privateKey.encodedBytes
        val pairForCredentials = ECKeyPair.create(priKeyBytes.extractArray())
        return Credentials.create(pairForCredentials)
    }



}



@Service
class PowerBudgetTokenCodeGen : ContractBase() {

    @Autowired
    lateinit var consts: GlobalConstants

    @Autowired
    lateinit var db: DbManager


    override val abiDirPath: Path
        get() = consts.SOL_PATH

    override val abiDirOutPath: Path
        get() = consts.BASE_PATH.resolve("pantheon/tools-dev/client-hub/src/main/java")

    override val outFile: File
        get() = abiDirOutPath.toFile()

    override val abiFile: File
        get() = abiDirPath.resolve("PowerBudgetToken.abi").toFile()

    override val binFile: File
        get() = abiDirPath.resolve("PowerBudgetToken.bin").toFile()

    //  db.initDb() called in controller
    fun deployContract(): Address {

        var _initialAmount = BigInteger.valueOf(100000)
        var  _tokenName = "Power Budget Token"
        var  _decimalUnits =  BigInteger.ONE
        var  _tokenSymbol = "PBT"
        val contract = PowerBudgetToken.deploy(web3j, getCredentials(), STATIC_GAS_PROVIDER, _initialAmount, _tokenName, _decimalUnits, _tokenSymbol ).send();
        val contractAddress = contract.contractAddress

        db.storeContractAddress(consts.CONTRACT_ADDRESS_KEY_POWER, contractAddress)

        return Address(contractAddress,PowerBudgetToken::class.java.simpleName);


    }

    fun getDeployedContract(c_addr:String): PowerBudgetToken {

        val powerBudg = PowerBudgetToken
                .load( c_addr, Web3j.build(HttpService()), getCredentials(), STATIC_GAS_PROVIDER)
        return powerBudg
    }

    //== private

    fun getCredentials(): Credentials {
        val keyPair = consts.loadKeyFile(consts.BOOT_NODE_PATH)
        val priKeyBytes = keyPair.privateKey.encodedBytes
        val pairForCredentials = ECKeyPair.create(priKeyBytes.extractArray())
        return Credentials.create(pairForCredentials)
    }



}


fun main() {


}



