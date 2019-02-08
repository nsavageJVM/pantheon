<template>
  <div class="columns  wrapper" style="max-width:80%;">
    <div class="column is-12 flex-box">
 
          <div class="field is-horizontal is-expanded">
            <div class="field-label has-text-grey">Address</div>
            <div class="field-body has-text-grey">{{value}}</div>
          </div>

          <div  v-if="!toggleForReciept" class="field is-horizontal is-grouped">
            <div class="field-label has-text-grey" style="max-width:110px;">Operations</div>
            <div class="field has-addons has-addons-right">
                <p class="control">
                    <input class="input" type="text" v-model="send"  placeholder="Amount to store">
                </p>
                 <p class="control">
                    <a class="button is-primary" v-on:click="runSimpleStorage()"  style="margin:0 20px 0 0;">Send</a> 
                </p>
            </div>
            <a class="button is-info">Get</a> 
          </div>
          <div  v-if="toggleForReciept">

          <div class="field has-addons has-addons">  
                    <p class="control">
                        <a class="button is-primary" >Transaction Hash</a> 
                    </p>
                    <p class="control">
                      <a class="label" 
                            style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:240px;margin:0 0 0 20px" >
                        {{ sol_data.transactionHash }}
                      </a>
                    </p>
          </div>   
          <div class="field has-addons">   
                    <p class="control">
                        <a class="button is-primary"  style="margin:0 20px 0 0;">Block Hash</a> 
                    </p>  
                    <p class="control">
                      <a class="label" 
                            style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:240px;margin:0 0 0 20px">
                          {{ sol_data.blockHash }}
                      </a>
                    </p>
          </div>         
                    <p class="control">
                      <a class="button">
                          {{ sol_data.blockNumber }}
                      </a>
                    </p>
                    <p class="control">
                      <a class="button">
                            {{ sol_data.gasUsed }}
                      </a>
                    </p>
                    <p class="control">
                      <a class="button">
                            {{ sol_data.statusOK }}
                      </a>
                    </p>
                    <p class="control">
                      <a class="button" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px;">
                            {{ sol_data.from }}
                      </a>
                    </p>
                    <p class="control">
                      <a class="button" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px;">
                            {{ sol_data.to }}
                      </a>
                    </p>  
          </div>


    </div>
  </div>
</template>

<script>
export default {
  name: "SimpleStorage",
    props: {
    value: String
 
  },
  components: {},

  data: function() {
    return {
     sol_data: {
          transactionHash:"0xfcf79d6b98b725fc209ba050dc5d1a1dd70c6fb455b54d96cc597601c3250fc8","transactionIndex":0,
          blockHash:"0xb5871f823065a732d30db80830af208f144524a262de4e60a72fec9f8b2c5b8e",
          blockNumber:211,
          gasUsed:22153,
          statusOK:true,
          from:"0x1001a611673dbbb6b77f79bcab88ff3c30889883",
          to:"0x9d17f87eaa363a5c2f89c924bd1b73a00a3a8652",

     },
     send: "", 
     toggleForReciept:false
    };
  },  
  methods: {
      runSimpleStorage: function( ) {
        // this.$store.state.sol_addr = '0x60874873b677db1422ad44f5f6f888d169ce2a78';
        // this.lib_deployContract();
        console.debug(this.send);
        this.toggleForReciept = true;
      },

    }
};
</script>

<style >
.wrapper {
  border:2px solid hsl(0, 0%, 86%);
  margin: 10px 0 0 8px;
  
}
</style>