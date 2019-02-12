<template>
  <div>
   <ProgressModal :m_text="modal_text" ></ProgressModal>
    <div class="columns is-fullheight flex-box is-flex-touch">

      <div class="column is-full">
        <div class="label has-text-grey-dark">Chai(n) Tools</div>
        <div class="github-link-container has-text-primary">
          Available on <a
            href="https://github.com/nsavageJVM"
            target="_blank"
            rel="noopener"
            class="github-link"
          > GitHub</a>
        </div>
      </div>
    </div>
    <div class="columns is-fullheight flex-box">
      <div class="column is-4">
        <nav class="panel">
          <p class="panel-heading">Contracts</p>
          <div v-if="num_contracts !== 0">
            <select
              @change="onContractSelect($event)"
              style="postion:relative;float:left;margin:10px 0 0 10px;"
            >
              <option
                selected
                disabled
                value=""
              >Contracts</option>
              <!-- 2nd item is iter object key(contract name), here text, :value binds to the event value, here addr -->
              <option
                v-for="(key, text) in contracts_list"
                :value="text"
                :key="key"
              >{{ text }}</option>
            </select>
          </div>
          <div v-if="num_contracts === 0">
            <router-link
              to="/contracts"
              class="button is-link is-info"
              style="postion:relative;float:left;margin:10px 0 0 10px;"
            >Deploy Contract</router-link>
          </div>
        </nav>
      </div>
      <div class="column is-9">
        <nav class="panel">
          <p
            class="panel-heading"
            style="max-width:80%;"
          >Contract Data</p>
          <div v-if="contract_name === 'SimpleStorage' ">
            <SimpleStorage :value="contract_address" />
          </div>
          <div v-if="contract_name === 'PowerBudgetToken' ">
            <StandardToken :value="contract_address" />
          </div>
        </nav>
      </div>
    </div>
  </div>
</template>

<script>
import SimpleStorage from "@/components/SimpleStorage.vue";
import StandardToken from "@/components/StandardToken.vue";
import ProgressModal from "@/components/ui/ProgressModal.vue";

export default {
  name: "home",
  components: {
    SimpleStorage,
    StandardToken,
    ProgressModal
  },

  data: function() {
    return {
      // contracts_list: {
      //   SimpleStorage: "0x4c6666106f61a85a7ca15b95e8c072b1ec902439",
      //   StandardToken: "0x217c0a153fcfb74946b8a15f7b3de958b3ba310b"
      // },
      // {transactionHash, blockHash, blockNumber, gasUsed, statusOK, from, to}
      // only used to select drop down default val
      contract_select: "",
      contract_address: "",
      contract_name: "", 
      modal_text:"Query for contract data in progress"
    };
  },

  methods: {
    onContractSelect: function(event) {
      //  alert(JSON.stringify(event.target.value));
      this.contract_name = event.target.value;
      console.debug(Object.keys(this.contracts_list));
      Object.keys(this.contracts_list).forEach(element => {
        if (element === this.contract_name) {
          this.contract_address = this.contracts_list[element];
          console.debug(this.contract_address);
        }
      });
    }, 
  },

  computed: {
    num_contracts() {
      var storeData = this.$store.state.sol_exists;
      return storeData;
      //  return 1;
    },
    
    contracts_list() {
      var sol_addr =  this.$store.state.sol_addr;
      var result = {};
      if(sol_addr["simple_storage"] !==' ') {
         result["SimpleStorage"] = sol_addr["simple_storage"];
       }
       if(sol_addr["power_budget_token"] !==' ') {
         result["PowerBudgetToken"] = sol_addr["power_budget_token"];
       }
 
      return result;
    }
 
  }
}
</script>

