<template>
<div>
  <div class="columns is-fullheight flex-box is-flex-touch" >
 
    <div class="column is-full">
        <div class="label has-text-grey-dark">Chai(n) Tools</div>
        <div class="github-link-container has-text-primary">
                Available on <a href="https://github.com/nsavageJVM"
                        target="_blank"
                        rel="noopener"
                        class="github-link"> GitHub</a>  
        </div>
    </div>  
  </div>
  <div class="columns is-fullheight flex-box" >
    <div class="column is-4">
    <nav class="panel">
      <p class="panel-heading">Contracts</p>
      <div v-if= "num_contracts !== 0" >
        <select  v-model="contract_select" @change="onContractSelect($event)"
                style="postion:relative;float:left;margin:10px 0 0 10px;"    >
            <option  selected disabled value=""  >Contracts</option>
            <!-- 2nd item is iter object key(contract name), here text, :value binds to the event value, here addr -->
            <option v-for="(key, text) in contracts_list" :value="key" :key="text">{{ text }}</option>
        </select>
      </div>  
      <div v-if= "num_contracts === 0" >
        <router-link to="/contracts" 
              class="button is-link is-info"
              style="postion:relative;float:left;margin:10px 0 0 10px;">Deploy Contract</router-link>
      </div>
    </nav>
    </div>
    <div class="column is-9" >
    <nav class="panel">
      <p class="panel-heading" style="max-width:80%;">Contract Data</p> 
    </nav>
    </div>
  </div>
</div>
</template>

<script>
// @ is an alias to /src
 

export default {
  name: 'home',
  components: {
 
  },

  data:  function() {
     return {
        //  contracts_list : {
        //   'Simple storage': '0x4c6666106f61a85a7ca15b95e8c072b1ec902439',
        //   'EthToken': '0x217c0a153fcfb74946b8a15f7b3de958b3ba310b'  },
          // only used to select drop down default val
          contract_select: "",
          
     }
   },

  methods:{
    onContractSelect :function(event){
        alert(JSON.stringify(event.target.value));
      }
  },

  beforeMount () {
    var solVal =  this.$store.state.sol_exists; 
    if(solVal === 0 ) {
        this.$data.num_contracts = 0;
    } else {
        this.$data.num_contracts = 1;
    }
 
  },
 

  mounted: function () {
  
   },

      computed: {  
      
       num_contracts() {
         var storeData =  this.$store.state.sol_exists;
         return storeData;
        //  return 1;
        },

        contracts_list() {
          var sol_name =  this.$store.state.sol_name;
          var sol_addr =  this.$store.state.sol_addr;
          // var sol_name  =  'Simple storage';
          // var sol_addr  =  '0x4c6666106f61a85a7ca15b95e8c072b1ec902439';
          var result = {};
          result[sol_name] = sol_addr;
          return result;
        }
 
   },
 
}
</script>

<style>
 
</style>
