<template>
  <div>
    <div v-if="show_modal" @close="show_modal = false">
      <transition name="modal">
        <div class="modal-mask">
          <div class="modal-wrapper">
              <div class="modal-container">
              <div class="modal-header">
                Query for contract data in progress
              </div>
              <div class="modal-body">
              <ProgressContent :total="prog_total" :done="prog_done" :modify="prog_modify" :tip='
                      [
                          {
                              text: "loading",
                              fillStyle:prog_col1,
                          }, {
                              text: "contract",
                              fillStyle:prog_col2,
                          }, {
                              text: "data",
                              fillStyle:prog_col3
                          }
                      ]
                      ' ></ProgressContent>
                </div>      
              </div>
            </div>
        </div>
      </transition>
    </div>                
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
          <div v-if="contract_name === 'StandardToken'">
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
import ProgressContent from "@/components/ui/ProgressContent.vue";

 

export default {
  name: "home",
  components: {
    SimpleStorage,
    StandardToken,
    ProgressContent
  },

  mounted() {
   this.genProgressData();
  
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
      prog_total:100,
      prog_done:0,
      prog_modify:0,
      prog_col1:'rgb(224,255,255)',
      prog_col2:'rgb(174, 193, 17)',
      prog_col3:'rgb(249, 149, 0)'
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
      this.showModal = true;
    },

    genProgressData() {
      let chunk = 20;
      const context = this;
      let  progressRunner = setInterval(function() {
      if(context.prog_modify < 10 || context.prog_modify > 99){
        context.prog_modify = 0;
        let tmp = context.prog_col1;
        context.prog_col1 = context.prog_col2;
        context.prog_col2 = context.prog_col3;
        context.prog_col3 = tmp;
      }
      if(context.prog_done < 0 || context.prog_done > 99){
        context.prog_done =0;
      }
      context.prog_done =  context.prog_done + chunk;
      context.prog_modify = context.prog_total - context.prog_done;
       console.debug("done: "+ context.prog_done+"  modify: "+ context.prog_modify) 
      },1000);
      }
  },

 

  computed: {
    num_contracts() {
      var storeData = this.$store.state.sol_exists;
      return storeData;
      //  return 1;
    },
    contracts_list() {
      var sol_name =  this.$store.state.sol_name;
      var sol_addr =  this.$store.state.sol_addr;

      var result = {};
      result[sol_name] = sol_addr;
      return result;
    },
    
    show_modal() {
      let  result =  this.$store.state.show_modal;
      return result;
    }
  }
}
</script>

<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(105, 99, 99, 0.5);
  display: table;
  transition: opacity .3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-container {
  width: 300px;
  margin: 0px auto;
  padding: 20px 30px;
  background-color: #fff;
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .33);
  transition: all .3s ease;
  font-family: Helvetica, Arial, sans-serif;
  color: black;
}

.modal-header h3 {
  margin-top: 0;
  color: black;
}

.modal-body {
  margin: 20px 0;
}



.modal-enter .modal-container,
.modal-leave-active .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}
</style>
