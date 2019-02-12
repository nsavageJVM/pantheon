package keth.tools.client.sol

import keth.tools.client.GlobalConstants
import keth.tools.client.ContractOps
import keth.tools.client.db.DbManager
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.CountDownLatch


@RunWith(SpringRunner::class)
@SpringBootTest
class ClientHubSolTests {

    @Autowired
    lateinit var constants: GlobalConstants

    @Autowired
    lateinit var db: DbManager

    @Autowired
    lateinit var simpleStoreCodeGen: SimpleStorageCodeGen

    @Autowired
    lateinit var contractOps: ContractOps


    @Autowired
    lateinit var powerBudgetTokCodeGen: PowerBudgetTokenCodeGen

    @Test
    fun solConfigTest() {

        println(simpleStoreCodeGen.abiDirPath.toUri())
        println(simpleStoreCodeGen.abiDirOutPath.toUri())
    }

    @Test
    fun simpleStorageWrapperTest() {
        simpleStoreCodeGen.generateWrapper()

    }


    @Test
    fun powerBudgetTokenWrapperTest() {
        powerBudgetTokCodeGen.generateWrapper()

    }



    @Test
    fun simpleStorageDeployTest() {

        db.initDb()

        val address = simpleStoreCodeGen.deployContract()
        println(address)

    }

    @Test
    fun powerBudgetTokenDeployTest() {

        db.initDb()

        val address = powerBudgetTokCodeGen.deployContract()
        println(address)

    }


    @Test
    fun solGetContractAddressTest() {

        db.initDb()
        val result = db.getContractAddress(constants.CONTRACT_ADDRESS_KEY_SIMPLE)
        println(result)
    }

    @Test
    fun solRunContractFuncTest() {
       val holder = CountDownLatch(1)
        db.initDb()
        contractOps. initVals()
        val result = contractOps.runSend(20170000000000000)
        println("solRunContractFuncTest result: ${result}"   )

        holder.await()
    }






}