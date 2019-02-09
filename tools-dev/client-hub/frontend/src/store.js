import Vue from 'vue'
import Vuex from 'vuex'

import { MESSAGE_TOPICS} from './plugins/RpcData'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    // management(mx) bean
 
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
    last_block:-1,
    // accounts
    accts:{ },
    // accounts balance
    accts_bal_query_result: 0,

    // contract address
    sol_addr:' ',
    sol_name:' ',
    sol_exists:0,
    sol_data:{
          "transactionHash":"",
          "blockHash":"",
          "blockNumber":0,
          "gasUsed":0,
          "statusOK":true,
          "from":"",
          "to":"" },
    toggleForReciept:false


  },
  mutations: {

    setJsonRpc(state,  jrpc) {
      state.ws_jrpc = jrpc;
    },

    setMxData(state, payload ) {
      //{mx_cpu, mx_mem_free, mx_mem_total, mx_db_size, mx_disk_free_space} 
      state = Object.assign(state, payload)
    },


    setReceiptData(state, payload) {
      state.sol_data = { ...payload}  ;

    },

    setToggleForReciept(state ) {
      state.toggleForReciept=true ;

    },
 
    // json-rpc api
    setJsonRpcData(state, jrpc_data) {
      console.log("store "+ JSON.stringify(jrpc_data) );

      if(jrpc_data.id === MESSAGE_TOPICS.NET_PEER_COUNT) {
        console.log(MESSAGE_TOPICS.NET_PEER_COUNT+" :: "+jrpc_data.result);
        state.peer_count = parseInt(jrpc_data.result);
      } else if(jrpc_data.id === MESSAGE_TOPICS.ETH_BLOCK_NUMBER) {
        console.log(MESSAGE_TOPICS.ETH_BLOCK_NUMBER+" :: "+jrpc_data.result);
        state.last_block = parseInt(jrpc_data.result);
      }
        else if(jrpc_data.id === MESSAGE_TOPICS.ETH_BALANCE) {
        console.log(MESSAGE_TOPICS.ETH_BALANCE+" :: "+jrpc_data.result);
        state.accts_bal_query_result = parseInt(jrpc_data.result);
        console.log("state.accts_bal_query_result "+" :: "+ state.accts_bal_query_result);
      }

    },

    setAccounts(state, payload) {
      state.accts =  payload;
    },

    setContractAddress(state, payload) {
  
      if(payload.solName === "empty") {
        console.log("store setContractAddress found empty address");
        state.sol_exists = 0;
        state.sol_addr =' ';
      } else {
        state.sol_exists = 1;
        state.sol_addr =  payload.solAddr;
        state.sol_name =  payload.solName;
        console.log("store setContractAddress set data as "+payload.solAddr+" "+payload.solName);
      }
  
    },

  },
  getters: {
 
  },
  actions: {
    // https://stackoverflow.com/questions/49224266/component-not-updating-on-store-change-vuex
  }
})
