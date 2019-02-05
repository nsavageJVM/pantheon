package keth.tools.client.config

import keth.tools.client.GlobalConstants
import org.junit.Test

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest
class ClientHubConfigTests {

    @Autowired
    lateinit var constants: GlobalConstants

    @Test
    fun configTest() {
        println(constants.DBCRYPTO_KEY_PASS)
        println(constants.DB_CRYPTO_KEY_STORE_PASS)
    }
}