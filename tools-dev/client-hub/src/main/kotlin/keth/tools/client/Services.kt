package keth.tools.client


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.client.db.DbManager
import keth.tools.client.sol.PowerBudgetTokenCodeGen
import keth.tools.client.sol.SimpleStorageCodeGen
import keth.tools.wrappers.PowerBudgetToken
import keth.tools.wrappers.SimpleStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigInteger

import org.web3j.protocol.core.methods.response.TransactionReceipt


@JsonIgnoreProperties(ignoreUnknown = true)
data class ContractData(val transactionHash: String, val blockHash: String, val blockNumber: String, val gasUsed: String, val statusOK: String, val from: String, val to: String)

@Service
class ContractOps {

    val objectMapper = jacksonObjectMapper()

    var tranReceiptChannel = Channel<ContractData>()
    var transValueChannel = Channel<BigInteger>()

    var queue = mutableListOf(ContractData("", "", "", "", "", "", ""))
    var queueValue = mutableListOf(BigInteger.ZERO)

    @Autowired
    lateinit var simpleStoreCodeGen: SimpleStorageCodeGen

    @Autowired
    lateinit var powerBudgetTokCodeGen: PowerBudgetTokenCodeGen

    @Autowired
    lateinit var db: DbManager

    @Autowired
    lateinit var constants: GlobalConstants


    lateinit var simpleStorageContract: SimpleStorage
    lateinit var simpleStoreAddr: String

    lateinit var powerBudgetTokenContract: PowerBudgetToken
    lateinit var powerBudgetTokenAddr: String

    fun initVals() {
        simpleStoreAddr = db.getContractAddress(constants.CONTRACT_ADDRESS_KEY_SIMPLE)
        powerBudgetTokenAddr = db.getContractAddress(constants.CONTRACT_ADDRESS_KEY_POWER)

        simpleStorageContract = simpleStoreCodeGen.getDeployedContract(simpleStoreAddr)
        powerBudgetTokenContract = powerBudgetTokCodeGen.getDeployedContract(powerBudgetTokenAddr)

    }

    fun broadCastContractInfo() = GlobalScope.launch {
        broadCastForReceipts(produceReceiptInfo())
        broadCastForValues(produceValueInfo())
    }

    /**
     *  Contract functions
     */
    fun runGet() {
      val result =  simpleStorageContract.get()
      result.flowable().subscribe { s -> queueValue.add(s)}
    }


    fun runSend(amt: Long) {

        val recipt = simpleStorageContract.set(BigInteger.valueOf(amt))
        recipt.flowable().subscribe { s -> queueTransactionReciepts(s) }

    }

    fun queueTransactionReciepts(r: TransactionReceipt) {

        val result = objectMapper.writeValueAsString(r)
        val contractData = objectMapper.readValue(result, ContractData::class.java);

        queue.add(contractData)

    }

    fun CoroutineScope.produceReceiptInfo() = produce<ContractData> {

        while (true) {
            if (queue.size > 0) {
                val cData = queue.last();
                println("produceReceiptInfo:   ${cData.toString()}")
                send(queue.last())
                queue.removeAt(queue.size - 1)
            }
            delay(5000L)
        }
    }

    fun CoroutineScope.produceValueInfo() = produce<BigInteger> {

        while (true) {
            if (queueValue.size > 0) {
                val cData = queueValue.last();
                println("produceReceiptInfo:   ${cData.toString()}")
                send(queueValue.last())
                queueValue.removeAt(queueValue.size - 1)
            }
            delay(5000L)
        }
    }



    fun CoroutineScope.broadCastForReceipts(channel: ReceiveChannel<ContractData>) = launch {

        for (node in channel) {
            tranReceiptChannel.send(node)
        }

    }

    fun CoroutineScope.broadCastForValues(channel: ReceiveChannel<BigInteger>) = launch {

        for (node in channel) {
            transValueChannel.send(node)
        }

    }
}
