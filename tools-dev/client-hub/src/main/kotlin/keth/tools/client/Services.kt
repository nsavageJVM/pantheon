package keth.tools.client


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import keth.tools.client.db.DbManager
import keth.tools.client.mx.MachineInfoDTO
import keth.tools.client.sol.ContractOperations
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
class ContractData(val transactionHash:String, val blockHash:String, val blockNumber:String, val gasUsed:String, val statusOK:String, val from:String, val to:String )

@Service
class SimpleStorageOps {

    val objectMapper = jacksonObjectMapper()

    var tranReceiptChannel = Channel<ContractData>()

    var queue = mutableListOf(ContractData("","","","","","",""))


    @Autowired
    lateinit var contractOps: ContractOperations

    @Autowired
    lateinit var db: DbManager

    @Autowired
    lateinit var constants: GlobalConstants


    lateinit var contract: SimpleStorage
    lateinit var contractAddr: String

    fun initVals() {
        contractAddr = db.getContractAddress(constants.CONTRACT_ADDRESS_KEY)
        contract = contractOps.getDeployedContract(contractAddr)

    }

    fun broadCastRecieptInfo() = GlobalScope.launch {
        broadCast(produceRecieptInfo())
    }


    fun runSend(amt: Long) {

        val recipt = contract.set(BigInteger.valueOf(amt))
        recipt.flowable().subscribe { s -> queueTransactionReciepts(s) }

    }

    fun queueTransactionReciepts(r: TransactionReceipt) {

        val result = objectMapper.writeValueAsString(r)
        println("SimpleStorageOps result::  ${result}")

       val contractData = objectMapper.readValue(result, ContractData::class.java);

        println("SimpleStorageOps contractData result::  ${contractData.toString()}")

        queue.add(contractData)

    }

    fun CoroutineScope.produceRecieptInfo() = produce<ContractData> {

        while (true) {
            if (queue.size > 0) {
                send(queue.last())
                queue.removeAt(queue.size - 1)
            }
            delay(5000L)
        }
    }

    fun CoroutineScope.broadCast(channel: ReceiveChannel<ContractData>) = launch {

        for (node in channel) {
            tranReceiptChannel.send(node)
        }

    }


}
