package keth.tools.client.sol

import keth.tools.client.GlobalConstants
import keth.tools.client.db.DbManager
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest
class ClientHubSolTests {

    @Autowired
    lateinit var constants: GlobalConstants


    @Autowired
    lateinit var solOpps: ContractOperations

    @Autowired
    lateinit var db: DbManager


    @Test
    fun solConfigTest() {

        println(solOpps.abiDirPath.toUri())
        println(solOpps.abiDirOutPath.toUri())
    }


    @Test
    fun solWrapperTest() {
        solOpps.generateWrapper()

    }

    @Test
    fun solDeployTest() {

        db.initDb()

        val address = solOpps.deployContract()
        println(address)

    }


    @Test
    fun solGetContractAddressTest() {

        db.initDb()
        val result = db.getContractAddress(constants.CONTRACT_ADDRESS_KEY)
        println(result)



    }




}