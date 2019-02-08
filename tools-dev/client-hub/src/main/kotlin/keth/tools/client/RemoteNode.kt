package keth.tools.client

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import keth.tools.client.db.DbManager
import keth.tools.client.mx.MachineInfoDTO
import keth.tools.client.mx.MxDataProvider
import keth.tools.client.sol.ContractOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect
import java.util.*


@SpringBootApplication
class ClientGuiApplication

fun main(args: Array<String>) {
	runApplication<ClientGuiApplication>(*args)
}

@Controller
class Dispatcher {



	@RequestMapping("/")
	fun root(locale: Locale): String {
		return "/login.html"
	}


	/** Home page.  */
	@RequestMapping("/tools")
	fun index(): String {
		return "index"
	}


	/** Login form.  */
	@RequestMapping("/tools/login")
	fun login(): String {
		return "login"
	}

	/** Login form.  */
	@RequestMapping("/tools/login-error")
	fun loginError(): String {
		return "login-error"
	}



}


@RestController
class RestData {

	class Accounts(var acct1:String, var acct2:String, var acct3:String)

	class Addresss(var solAddr:String, var solName:String)

	@Autowired
	lateinit var constants: GlobalConstants

	@Autowired
	lateinit var db: DbManager

	@Autowired
	lateinit var solOpps: ContractOperations

	@Autowired
	lateinit var simpleStorageOps: SimpleStorageOps



	@RequestMapping( "/accts-data_path", method = arrayOf(RequestMethod.POST) )
	fun getAccounts( ) : Accounts  {
		if(!db.isInitialised) {
			db.initDb()
		}

		val  addresses: Triple<String, String, String> = db.getAccounts()

		return Accounts(addresses.first, addresses.second, addresses.third)

	}

	@RequestMapping( "/deploy_contract", method = arrayOf(RequestMethod.POST) )
	fun deployContract( ) : Addresss  {
		if(!db.isInitialised) {
			db.initDb()
		}
		return Addresss(solOpps.deployContract(), " ")
	}


	@RequestMapping( "/contract_addr", method = arrayOf(RequestMethod.POST) )
	fun getContractAddress( ) : Addresss  {
		if(!db.isInitialised) {
			db.initDb()
		}

		val addr = db.getContractAddress(constants.CONTRACT_ADDRESS_KEY)

		if(addr.equals(constants.NULL_CONTRACT_ADDRESS_VALUE)) {
			return	Addresss(addr, constants.NULL_CONTRACT_ADDRESS_VALUE)
		}

	 	val contract = solOpps.getDeployedContract(addr)

		return Addresss(addr, contract.javaClass.simpleName)

	}


	// https://www.journaldev.com/3358/spring-requestmapping-requestparam-pathvariable-example
	@RequestMapping( "/simple_storage_ops/{op} " )
	fun runSimpleStorageOps( @PathVariable(value="op" ) op:String, @RequestParam("value")  value:String  ) {
		if(!db.isInitialised) {
			db.initDb()
		}

		when(op) {
			"set" -> {
				simpleStorageOps.runSend(value.toLong())
			}
			"get" -> {
				println("TO DO")
				}
			}
		}
	}



@Controller
class WsDispatcher(@Autowired private val mxDataProvider: MxDataProvider, @Autowired private val storageOps: SimpleStorageOps) {

	protected val mapper = jacksonObjectMapper()


    @Autowired
	lateinit var  mxDataMessages: SimpMessagingTemplate

	@Autowired
	lateinit var  receiptMessages: SimpMessagingTemplate

	    init {
			GlobalScope.launch {
				mxDataProvider.broadCastMxInfo()
				storageOps.broadCastRecieptInfo()
				delay(5000)
				processMachineInfo(mxDataProvider.mxInfoChannel)
			}

    }


	fun CoroutineScope.processMachineInfo(inStream: Channel<MachineInfoDTO>) = launch {

		for (node in inStream) {
			broadcastMxInfo(node)
		}
	}

	fun CoroutineScope.processReceiptInfo(inStream: Channel<String>) = launch {

		for (node in inStream) {
			broadcastReceiptInfo(node)
		}
	}

	suspend fun broadcastMxInfo(dto: MachineInfoDTO) {
		mxDataMessages.convertAndSend("/topic/mx", mapper.writeValueAsString(dto));
	}


	/** already json */
	suspend fun broadcastReceiptInfo(dto: String) {
		receiptMessages.convertAndSend("/topic/receipt", mapper.writeValueAsString(dto) );
	}
}



// https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/html5/
@Configuration
@EnableWebSocketMessageBroker
class WebSocketAuthorizationSecurityConfig: AbstractSecurityWebSocketMessageBrokerConfigurer() {


	override fun registerStompEndpoints(registry: StompEndpointRegistry) {
		registry.addEndpoint("/mx-data_path","/receipt-data_path" )
				.setAllowedOrigins("*").withSockJS();

	}

	override fun configureMessageBroker(config: MessageBrokerRegistry) {
		// base path for out going subscription
		// in sockjs .subscribe('/topic/xxxxx' =>
		// controller   @SendTo("/topic/xxxxx")
		config.enableSimpleBroker("/topic");
		// in sockjs send to  "/mx/endpoint") =>  hits @MessageMapping("/endpoint")
		config.setApplicationDestinationPrefixes("/mx_in_path", "/receipt_in_path"); // base send path on js
	}



	override fun configureInbound(messages : MessageSecurityMetadataSourceRegistry) {
		messages
				.nullDestMatcher().permitAll()
				.simpSubscribeDestMatchers("/topic/" + "**").permitAll()
				.simpDestMatchers("/mx_in_path/**", "/receipt_in_path/**").permitAll();


	}

	override fun sameOriginDisabled(): Boolean {
		return true
	}

}



@EnableWebSecurity
class SecurityConfig: WebSecurityConfigurerAdapter() {

	override fun configure (http: HttpSecurity) {

		http.csrf().disable();
		http.formLogin()
				// note login form in login page must point to this url
				.loginPage("/tools/login")
				.defaultSuccessUrl("/index.html", true)
				.and()
				.logout()
				.invalidateHttpSession(true)
				.logoutSuccessUrl("/tools/login")
				.permitAll()
				.and()
				.authorizeRequests()
				.antMatchers("/tools").hasAnyRole("USER","ADMIN")
				.antMatchers("/accts-data_path").hasAnyRole("USER","ADMIN")
				.antMatchers("/deploy_contract").hasAnyRole("USER","ADMIN")
				.antMatchers("/contract_addr").hasAnyRole("USER","ADMIN")
				.antMatchers("/").hasAnyRole("USER","ADMIN")
				.antMatchers("/index**").hasAnyRole("USER","ADMIN")
				.antMatchers("**/mx-data_path/**", "/receipt_in_path/**").permitAll()
				.and()
				.exceptionHandling().accessDeniedPage("/tools/403.html");


	}

	override fun configure(auth: AuthenticationManagerBuilder) {
		auth.inMemoryAuthentication()
				.passwordEncoder(NoOpPasswordEncoder.getInstance())
				.withUser("user").password("password").roles("USER")
				.and()
				.passwordEncoder(NoOpPasswordEncoder.getInstance())
				.withUser("admin").password("admin").roles("ADMIN");

	}


}



@Configuration
@ConfigurationProperties(prefix = "mx")
class  PropertyValues {

	lateinit var dbDir: String

	@Bean
	open fun springSecurityDialect(): SpringSecurityDialect {
		return SpringSecurityDialect()
	}

	@Bean
	open fun objectMapperBuilder(): Jackson2ObjectMapperBuilder
			= Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())


}


