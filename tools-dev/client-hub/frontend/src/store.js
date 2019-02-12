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
    sol_addr:{empty_addr:' ', simple_storage:' ', power_budget_token:' ' },
    sol_exists:0,
    sol_data:{
          "transactionHash":"",
          "blockHash":"",
          "blockNumber":0,
          "gasUsed":0,
          "statusOK":true,
          "from":"",
          "to":"" },
    sol_value:'',
    toggleForReciept:false,
    toggleForValue:false,
    show_modal:false


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

    setValueData(state, payload) {
      state.sol_value = payload;

    },

    setToggleForReciept(state ) {
      state.toggleForReciept=!state.toggleForReciept ;
    },

    setToggleForValue(state ) {
      state.toggleForValue=!state.toggleForValue ;
    },

    setToggleForModal(state, toggle ) {
      state.show_modal = toggle;

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

    setContractAddressFromDeploy(state, payload ) {
      console.debug("store setContractAddressFromDeploy payload: "+payload );
       // not a json array but object keys
       state.sol_exists = 1;
       if(payload["solName"]==='SimpleStorage') {
         state.sol_addr.simple_storage = payload["solAddr"];
       }
       if(payload["solName"]==='PowerBudgetToken') {
         state.sol_addr.power_budget_token =  payload["solAddr"];
       }
    },

    setContractAddress(state, payload ) {
        console.debug("store setContractAddress payload: "+payload );
 
       // let j_arr = JSON.parse(payload);
       for (var index in payload) {
        let c_data = payload[index];
        console.debug("store setContractAddress loop item c_data.solName "+c_data.solName);
        if(c_data.solName === "empty") {
          console.debug("store setContractAddress loop continue c_data empty" );
          continue;     
        } else {
          console.debug("store setContractAddress loop else  " );
          state.sol_exists = 1;
          if(c_data.solName==='SimpleStorage') {
            state.sol_addr.simple_storage =  c_data.solAddr;
          }
          if(c_data.solName==='PowerBudgetToken') {
            state.sol_addr.power_budget_token =  c_data.solAddr;
          }

        }

      }
 
    },

  },
  getters: {
 
  },
  actions: {
    // https://stackoverflow.com/questions/49224266/component-not-updating-on-store-change-vuex
  }
})
