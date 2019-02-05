<template>
<div class="columns is-fullheight flex-box" >
 
    <div class="column is-4">
       <nav class="panel"> 
        <p class="panel-heading">Accounts</p>
        <table class="table is-striped has-background-grey-lighter" style="min-width:100%;">  
            <tbody>
            <tr v-for="(data, index) in accts_list" :key="data"  >
                <th  >{{ index  }}</th>
                <td style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px;">{{ data  }}</td>
                <td><a v-on:click="getAccount(data)" class="button is-link">Get Balance</a></td>
            </tr>
            </tbody>
        </table>
        </nav>
    </div>
    <div class="column is-9" >
        <nav class="panel"> 
            <p class="panel-heading" style="max-width:80%;">Balance</p>
            <nav class="level" style="max-width:80%;">
            <div class="level-left"> 
                <div v-if= "selected_acct !== 0" class="level-item is-medium has-text-grey">
                   <span><p>Selected</p> {{selected_acct }}</span> 
                </div>
            </div>
            <div class="level-right">
                <div v-if= "selected_acct_bal !== 0" class="level-item is-medium has-text-grey">
                   <span><p>Balance</p> {{selected_acct_bal }}</span>
                </div>
            </div>
            </nav>  
 
        </nav>
    </div>
 
</div>
</template>

<script>
// @ is an alias to /src
export default {
  name: 'accounts',

  data:  function() {
    return { 
        selected_acct:0
    //    accts_list : {
    //    'acct1': '0x4c6666106f61a85a7ca15b95e8c072b1ec902439',
    //    'acct2': '0x217c0a153fcfb74946b8a15f7b3de958b3ba310b',
    //    'acct3': '0x60874873b677db1422ad44f5f6f888d169ce2a78' }

     }
  },

  beforeMount () {
    this.$data.accts_bal_query_result = 0;
  },
  
  mounted: function () {
      this.lib_getAllAccounts();
  },

  methods: {
    getAccount: function(data) {

        let paramsData = [data, "latest"];
        let paramsValue = this.$rpcMessageParamsProvider(paramsData);
        let mssgBaseObj =  JSON.parse(this.$jsonRpcMethods.ETH_BALANCE);
        mssgBaseObj.params = paramsValue;
        let mssg = 
            this.$jsonRpcMessageProvider(
                            JSON.stringify(mssgBaseObj),
                            this.$jsonRpcTopics.ETH_BALANCE);

        console.debug(JSON.stringify(mssg));
        this.lib_processJsonRpc(mssg);
        this.selected_acct = data;
      }
    },

   computed: {

    accts_list() {
        var storeData =  this.$store.state.accts;
        return storeData;
        },

    selected_acct_bal() {
        var storeData =  this.$store.state.accts_bal_query_result;
        return storeData;  
        },

    // selected_acct() {
        
    //     return 0;  
    //     }
    }


}
</script>