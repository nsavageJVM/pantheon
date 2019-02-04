// https://dev.to/nkoik/writing-a-very-simple-plugin-in-vuejs---example-8g8

import {MESSAGE_TEMPATE, MESSAGE_TOPICS} from './RpcData'


const JsonRpcAttrs  = Object.freeze({
    JSONRPC :'jsonrpc',
    JSONRPC_VERSION :'2.0',
    ID:'id'
});

// params, id
// ({"method":"net_peerCount", "params":[ ] },"p_ct"

const rpcMessageProvider = (mssg, id) => {
    const result  = Object.create(null);
    const mssgObject = JSON.parse(mssg)
    result[JsonRpcAttrs.JSONRPC] = JsonRpcAttrs.JSONRPC_VERSION;
    result[JsonRpcAttrs.ID] = id;
    for (let key of Object.keys(mssgObject)) {
        let value = mssgObject[key];
        if (value !== undefined) {
            result[key] =  value;
        }
      }
      
      return result;
}

const rpcMessageParamsProvider = (params) => {
    let result = [];
    params.forEach(function (value) {
        result.push(value)
      });
      
    return result;
}






export default {

    install (Vue, options) {
        Vue.prototype.$jsonRpcMethods = MESSAGE_TEMPATE;
        Vue.prototype.$jsonRpcTopics = MESSAGE_TOPICS;
        Vue.prototype.$jsonRpcMessageProvider = (params, id) => rpcMessageProvider(params, id );
        Vue.prototype.$rpcMessageParamsProvider = (params) => rpcMessageParamsProvider(params);

    }
}


 