<template>
<div>
<ProgressModal :m_text="modal_text" ></ProgressModal>  
<section class="home is-fullheight">
<div class="columns is-fullheight flex-box" >

   <div class="column is-3">
      <nav class="panel" >
           <p class="panel-heading">Contract Operations</p>
           <div  v-if= "sol_exists === 0" style="position:relative;float:left;padding:10px 0 0 10px;"  >
            <a v-on:click="deployContract()" class="button is-link" 
                              style="display:block;margin:10px 0 0 10px;">
               Deploy Contract</a>
           </div>
           <div  v-if= "sol_exists === 1" style="position:relative;float:left;padding:10px 0 0 10px;"  >
            <a class="button is-success" style="display:block;margin:10px 0 0 10px;">
               Deployed Contract Exists</a>
 
          </div>
      </nav>   
   </div>
 
    <div class="column is-9" >
      <nav class="panel">
            <p class="panel-heading" style="max-width:80%;">Contract Data</p>
             <div  v-if= "sol_exists === 1"> 
              <span style="position:relative;float:left;padding-left:10px;margin:24px 0 0 0;" > 
                 <p class="is-medium has-text-grey" 
                              style="display:inline;padding-right:10px;"><strong>Address</strong></p>
               <a class="button is-success" style="display:inline;">{{address}}</a> </span> 
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

    beforeMount () {
      var solVal =  this.$store.state.sol_exists; 
      this.$data.sol_exists = solVal; 
      
      if(solVal === 1) {
         this.$data.address = this.$store.state.sol_addr; 
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
   },
   methods: {
      deployContract: function( ) {
        // this.$store.state.sol_addr = '0x60874873b677db1422ad44f5f6f888d169ce2a78';
         this.lib_deployContract();
      },

    }
}

</script>