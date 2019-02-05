<template>
<section class="home is-fullheight">
<div class="columns is-fullheight flex-box" >

   <div class="column is-3">
      <nav class="panel" >
          <p class="panel-heading">Contract Operations</p>
          <div  v-if= "address === ' '" style="position:relative;float:left;padding:10px 0 0 10px;"  >
            <a v-on:click="deployContract()" class="button is-link" 
                              style="display:block;margin:10px 0 0 10px;">
               Deploy Contract</a>
            <a v-on:click="searchContract()" class="button is-link"  
                              style="display:block;margin:10px 0 0 10px">
               Contract From DB</a>
          </div>
         <div  v-if= "address !== ' '" style="position:relative;float:left;padding:10px 0 0 10px;"  >
            <a class="button is-success" style="display:block;margin:10px 0 0 10px;">
               Deployed Contract Exists</a>
 
          </div>
      </nav>   
   </div>
 
    <div class="column is-9" >
      <nav class="panel">
            <p class="panel-heading" style="max-width:80%;">Contract Data</p>
             <div  v-if= "address !== ' '"> 
              <span style="position:relative;float:left;padding-left:10px;margin:24px 0 0 0;" > 
                 <p class="is-medium has-text-grey" 
                              style="display:inline;padding-right:10px;"><strong>Address</strong></p>
               <a class="button is-success" style="display:inline;">{{address}}</a> </span> 
            </div>
      </nav>    
    </div>

</div>
</section>
</template>

<script>
// @ is an alias to /src

export default {
  name: 'contracts',

  data:  function() {
    return { 
    //    accts_list : {
    //    'acct1': '0x4c6666106f61a85a7ca15b95e8c072b1ec902439',
    //    'acct2': '0x217c0a153fcfb74946b8a15f7b3de958b3ba310b',
    //    'acct3': '0x60874873b677db1422ad44f5f6f888d169ce2a78' }

     }
  },
   
   computed: {  
      address() {
        var storeData =  this.$store.state.sol_addr;
        return storeData;
        },
   },
   methods: {
      deployContract: function( ) {
        // this.$store.state.sol_addr = '0x60874873b677db1422ad44f5f6f888d169ce2a78';
         this.lib_deployContract();
      },

      searchContract: function( ) {
          //  this.$store.state.sol_addr = '0x60874873b677db1422ad44f5f6f888d169ce2a78';
           this.lib_searchContract();
      }



    }
}

</script>