import Vue from 'vue'
import Vuex from 'vuex'

import { MESSAGE_TOPICS} from './plugins/RpcData'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    // management(mx) bean
    stomp_mx: null,
    // mx beans data
    mx_cpu: 0,
    mx_mem_free: 0,
    mx_mem_total: 0,
    mx_db_size: 0,
    mx_disk_free_space: 0,
    // json rpc api
    ws_jrpc: null,
    // json rpc api data
    peer_count:0,
    last_block:-1


  },
  mutations: {

    setJsonRpc(state,  jrpc) {
      state.ws_jrpc = jrpc;
    },

    setMxStompClient(state, stomp) {
      state.stomp_mx = stomp;
    }, 

    setMxData(state, payload ) {
      //{mx_cpu, mx_mem_free, mx_mem_total, mx_db_size, mx_disk_free_space} 
      state = Object.assign(state, payload)
    },

    // json-rpc api
    setJsonRpcData(state, jrpc_data) {
      console.log("store "+ JSON.stringify(jrpc_data));

      if(jrpc_data.id === MESSAGE_TOPICS.NET_PEER_COUNT) {
        console.log(MESSAGE_TOPICS.NET_PEER_COUNT+" :: "+jrpc_data.result);
        state.peer_count = parseInt(jrpc_data.result);
      } else if(jrpc_data.id === MESSAGE_TOPICS.ETH_BLOCK_NUMBER) {
        console.log(MESSAGE_TOPICS.ETH_BLOCK_NUMBER+" :: "+jrpc_data.result);
        state.last_block = parseInt(jrpc_data.result);
      }

    },


  },
  getters: {
 
  },
  actions: {
    // https://stackoverflow.com/questions/49224266/component-not-updating-on-store-change-vuex
  }
})
