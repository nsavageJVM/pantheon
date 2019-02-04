package keth.tools.client.db

import keth.tools.client.GlobalConstants
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class ClientHubDbTests {

    @Autowired
    lateinit var constants: GlobalConstants

    @Autowired
    lateinit var db: DbManager

    @Test
    fun dbConfigTest() {
        println(db.ALIAS)
    }


    @Test
    fun getAccts( ) {


       val  addresses: Triple<String, String, String> = db.getAccounts()
        println(addresses)

    }


}