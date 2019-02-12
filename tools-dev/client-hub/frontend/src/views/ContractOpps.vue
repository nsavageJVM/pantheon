<template>
<div>
<ProgressModal :m_text="modal_text" ></ProgressModal>  
<section class="home is-fullheight">
<div class="columns is-fullheight flex-box" >

   <div class="column is-3">
      <nav class="panel" >
           <p class="panel-heading">Contract Operations</p>
           <div  v-if= "sol_exists === 0" style="position:relative;float:left;padding:10px 0 0 10px;"  >
           <select @change="deployContract($event)" style="postion:relative;float:left;margin:10px 0 0 10px;" >
              <option  selected  disabled value="">Deploy Contracts</option>
              <!-- option value binds to object key, :value binds to the event value, here {1, 2} -->
              <option
                v-for="(val, opt) in create_drop_down_list"
                :value="opt"
                :key="val"
              >{{ val }}</option>
           </select>
           </div>
           <div  v-if= "sol_exists === 1" style="position:relative;float:left;padding:10px 0 0 10px;"  >
           <router-link to="/" class="button is-success" style="display:block;margin:10px 0 0 10px;">Deployed Contract Operations
           </router-link>
           <select @change="deployContract($event)" style="postion:relative;float:left;margin:10px 0 0 10px;" >
              <option  selected  disabled value="">Deploy Contracts</option>
              <!-- option value binds to object key, :value binds to the event value, here {1, 2} -->
              <option
                v-for="(val, opt) in create_drop_down_list"
                :value="opt"
                :key="val"
              >{{ val }}</option>
           </select>

          </div>
      </nav>   
   </div>
 
    <div class="column is-9" >
      <nav class="panel">
            <p class="panel-heading" style="max-width:80%;">Contract Data</p>
             <div  v-if= "sol_exists === 1"> 
              <table class="table is-striped has-background-grey-lighter"  
                              style="position:relative;float:left;padding-left:10px;margin:24px 0 0 0;">  
                  <tbody>
                  <tr  >
                      <th>Simple Storage</th>
                      <td style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px;">{{ data  }}</td>
                      <td><a >{{address.simple_storage}}</a></td>
                  </tr>
                  <tr  >
                      <th>Power Budget Token</th>
                      <td style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px;">{{ data  }}</td>
                      <td><a >{{address.power_budget_token}}</a></td>
                  </tr>
                  </tbody>
              </table>
            </div>
      </nav>    
    </div>
</div>
</section>
</div>
</template>

<script>
// @ is an alias to /src
import ProgressModal from "@/components/ui/ProgressModal.vue";

export default {
  name: 'contracts',
  components: {
    ProgressModal
  },
  data:  function() {
    return { 
    //    accts_list : {
    //    'acct1': '0x4c6666106f61a85a7ca15b95e8c072b1ec902439',
    //    'acct2': '0x217c0a153fcfb74946b8a15f7b3de958b3ba310b',
    //    'acct3': '0x60874873b677db1422ad44f5f6f888d169ce2a78' }
    modal_text:"Operation to deploy contract is  in progress"

     }
  },
 
   
  computed: {  
      
      address() {
        var storeData =  this.$store.state.sol_addr;
        return storeData;
        //return ' ';
        },
     
      sol_exists() {
        var storeData =  this.$store.state.sol_exists;
        return storeData;
        // return 1;
        },

      create_drop_down_list() {
        var storeData =  this.$store.state.sol_addr;
        var contracts_list = {}; 
        console.debug("contractopps create_drop_down_list  "+JSON.stringify(storeData));
        if(storeData.simple_storage === ' ') {
            contracts_list['1']='Simple Storage';
          }
        if(storeData.power_budget_token === ' ') {
            contracts_list['2']='Power Budget Token';
          }  
          
          return contracts_list;
          
        }
   },
  methods: {
      deployContract: function( event) {
        // event.target.value  => select option value
        // this.$store.state.sol_addr = '0x60874873b677db1422ad44f5f6f888d169ce2a78';
          this.lib_deployContract(event.target.value);
          console.debug("contractopps called deployContract with option value  "+event.target.value);
        //  alert(event.target.value);
      },

    }
}

</script>