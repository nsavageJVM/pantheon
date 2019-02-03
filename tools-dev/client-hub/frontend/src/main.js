import Vue from 'vue'
import Buefy from 'buefy';
import 'buefy/dist/buefy.css'

import { library } from '@fortawesome/fontawesome-svg-core'
import { faHome, faChild, faCircle, faInfo, faReply } from '@fortawesome/free-solid-svg-icons'
import { faComment } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon, FontAwesomeLayers, FontAwesomeLayersText } from '@fortawesome/vue-fontawesome'
 
import VueApexCharts from 'vue-apexcharts'
import VueCookies from 'vue-cookies'
 
import SockJS from "sockjs-client";
import Stomp from "webstomp-client";

import App from './App.vue'
import router from './router'
import store from './store'

import jrpcProvider from './plugins/JsonRpcProvider'

Vue.config.productionTip = false

Vue.use(Buefy);
Vue.use(VueCookies)
Vue.use(jrpcProvider)

library.add(
  faHome,
  faChild,
  faCircle,
  faInfo,
  faComment,
  faReply
)

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.component('font-awesome-layers', FontAwesomeLayers)
Vue.component('font-awesome-layers-text', FontAwesomeLayersText)

Vue.component('apexchart', VueApexCharts)

var WS_URI = "ws://localhost:8546";
var HTTP_URI = "http://127.0.0.1:8545";

// global functions 
Vue.mixin( {
  methods: {
    lib_createJsonRpcApi(mssg) {
      const savedStore = this.$store;
      let ws_rpc = new WebSocket( WS_URI);
    
      ws_rpc.onopen = (event ) => {
        console.log("Socket has been opened!");
        savedStore.commit('setJsonRpc',  ws_rpc);

      };

      ws_rpc.onmessage = (event ) => {
        console.log("Socket calls back");
        let jrpc_data = JSON.parse(event.data);
        //console.log(jrpc_data);
        savedStore.commit('setJsonRpcData', jrpc_data);
      };
 
    },

    // mx data and callback code
    lib_createMxDataSocket() {
      console.log('lib_createMxDataSocket') 

      const ws = new SockJS("/mx-data_path");
      const stomp = Stomp.over(ws);
      const savedStore = this.$store;
   
      var token = $cookies.get('JSESSIONID') || 0;
      console.log('token:  '+token);
      console.log('token as json:  '+JSON.stringify(token)   );
        var headers = {
            Authorization : 'Bearer ' + token,
        };

      stomp.connect( {},
        frame => {
          this.connected = true;
            stomp.subscribe("/topic/mx", tick => {
           //{mx_cpu, mx_mem_free, mx_mem_total, mx_db_size, mx_disk_free_space} 
            var mx_obj =  JSON.parse(  tick.body); 
            var payload = {};
            payload.mx_cpu =  Math.trunc(mx_obj.cpuUsage);   
            payload.mx_mem_free =  Math.trunc(mx_obj.memoryFree); 
            payload.mx_mem_total =  Math.trunc(mx_obj.memoryTotal); 
            payload.mx_db_size =  Math.trunc(mx_obj.dbSize); 
            payload.mx_disk_free_space =  Math.trunc(mx_obj.freeSpace);   
            savedStore.commit('setMxData', payload);
     
          }, headers);
        },
        error => {
          console.log(error);
          this.connected = false;
        }
      ); // end connect

      savedStore.commit('setMxStompClient', stomp);
    },

    lib_processJsonRpc(mssg) {
       // console.log('lib_processJsonRpc '+JSON.stringify(mssg)); 
        this.$store.state.ws_jrpc.send(JSON.stringify(mssg));
 
    }

} // end methods

}); // end mix in

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
