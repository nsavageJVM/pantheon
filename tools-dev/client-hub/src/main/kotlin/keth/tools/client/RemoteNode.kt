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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
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

	@Autowired
	lateinit var constants: GlobalConstants

	@Autowired
	lateinit var db: DbManager

	@Autowired
	lateinit var solOpps: ContractOperations



	@RequestMapping( "/accts-data_path", method = arrayOf(RequestMethod.POST) )
	fun getAccounts( ) : Accounts  {
		if(!db.isInitialised) {
			db.initDb()
		}

		val  addresses: Triple<String, String, String> = db.getAccounts()

		return Accounts(addresses.first, addresses.second, addresses.third)

	}

	@RequestMapping( "/deploy_contract", method = arrayOf(RequestMethod.POST) )
	fun deployContract( ) : String  {
		if(!db.isInitialised) {
			db.initDb()
		}
		return 	solOpps.deployContract()
	}


	@RequestMapping( "/contract_addr", method = arrayOf(RequestMethod.POST) )
	fun getContractAddress( ) : String  {
		if(!db.isInitialised) {
			db.initDb()
		}
		db.getContractAddress(constants.CONTRACT_ADDRESS_KEY)


		return ""

	}



}



@Controller
class WsDispatcher(@Autowired private val mxDataProvider: MxDataProvider) {

	protected val mapper = jacksonObjectMapper()


    @Autowired
	lateinit var  maDataMessages: SimpMessagingTemplate

	    init {
			GlobalScope.launch {
				val brodcastJob = mxDataProvider.broadCastMxInfo()
				delay(5000)
				processMachineInfo(mxDataProvider.mxInfoChannel)
			}

    }


	fun CoroutineScope.processMachineInfo(inStream: Channel<MachineInfoDTO>) = launch {

		for (node in inStream) {
			broadcast(node)
		}
	}

	suspend fun broadcast(dto: MachineInfoDTO) {
		maDataMessages.convertAndSend("/topic/mx", mapper.writeValueAsString(dto));
	}
}



// https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/html5/
@Configuration
@EnableWebSocketMessageBroker
class WebSocketAuthorizationSecurityConfig: AbstractSecurityWebSocketMessageBrokerConfigurer() {


	override fun registerStompEndpoints(registry: StompEndpointRegistry) {
		registry.addEndpoint("/mx-data_path")
				.setAllowedOrigins("*").withSockJS();

	}

	override fun configureMessageBroker(config: MessageBrokerRegistry) {
		// base path for out going subscription
		// in sockjs .subscribe('/topic/xxxxx' =>
		// controller   @SendTo("/topic/xxxxx")
		config.enableSimpleBroker("/topic");
		// in sockjs send to  "/mx/endpoint") =>  hits @MessageMapping("/endpoint")
		config.setApplicationDestinationPrefixes("/mx_in_path"); // base send path on js
	}



	override fun configureInbound(messages : MessageSecurityMetadataSourceRegistry) {
		messages
				.nullDestMatcher().permitAll()
				.simpSubscribeDestMatchers("/topic/" + "**").permitAll()
				.simpDestMatchers("/mx_in_path/**").permitAll();

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
				.antMatchers("/").hasAnyRole("USER","ADMIN")
				.antMatchers("/index**").hasAnyRole("USER","ADMIN")
				.antMatchers("**/mx-data_path/**").permitAll()

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


