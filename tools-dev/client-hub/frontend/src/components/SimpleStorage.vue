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
                    <input class="input" type="text" v-model="amount"  placeholder="Amount to store">
                </p>
                 <p class="control">
                    <a class="button is-primary" v-on:click="runSimpleStorage('store')"  style="margin:0 20px 0 0;">Send</a> 
                </p>
            </div>

            <a class="button is-info">Get</a> 
         </div><!--end !toggleForReciept -->

          <div  v-if="toggleForReciept">

            <div class="field has-addons has-addons">  
                      <p class="control">
                        <a class="button is-primary" >Transaction Hash</a> 
                      </p>
                      <p class="control">
                        <ToolTipLabel :value ="sol_data.transactionHash" />
                      </p>
            </div>   
            <div class="field has-addons">   
                      <p class="control">
                          <a class="button is-primary"  style="margin:0 20px 0 0;">Block Hash</a> 
                      </p>  
                      <p class="control">
                      <ToolTipLabel :value ="sol_data.blockHash" />
                      </p>
            </div> 

                <div class="field is-grouped- is-grouped-multiline ">  

                  <div class="field has-addons">
                          <p class="control">
                              <a class="button is-primary"  style="margin:0 20px 0 0;">Block Number</a> 
                          </p>         
                          <p class="control">
                            <a class="button">
                                {{ sol_data.blockNumber }}
                            </a>
                          </p>
                  </div>
                  <div class="field has-addons">
                          <p class="control">
                              <a class="button is-primary"  style="margin:0 20px 0 0;">Gas Used</a> 
                          </p>    
                          <p class="control">
                            <a class="button">
                                  {{ sol_data.gasUsed }}
                            </a>
                          </p>
                  </div> 
                  <div class="field has-addons">
                          <p class="control">
                              <a class="button is-primary"  style="margin:0 20px 0 0;">Staus</a> 
                          </p>         
                          <p class="control">
                            <a class="button">
                                  {{ sol_data.statusOK }}
                            </a>
                          </p>
                  </div>

                </div><!--end multiline --> 

            <div class="field has-addons">
                      <p class="control">
                          <a class="button is-primary"  style="margin:0 20px 0 0;">From </a> 
                      </p>         
                      <p class="control">
                         <ToolTipLabel :value ="sol_data.from" />
                      </p>
            </div>
            <div class="field has-addons">
                      <p class="control">
                          <a class="button is-primary"  style="margin:0 20px 0 0;">To </a> 
                      </p>           
                      <p class="control">
                        <ToolTipLabel :value ="sol_data.to" />
                      </p>  
                  
            </div>
            <a class="button is-primary" v-on:click="runReturnOps( )">Back to contract Operations</a>
          </div>><!--end toggleForReciept -->

    </div><!--end is-12 -->
  </div> 
</template>

<script>
import ToolTipLabel  from "@/components/ui/ToolTipLabel.vue"

export default {
  name: "SimpleStorage",
  props: {
    value: String
 
  },
  components: {
    ToolTipLabel
  },

  data: function() {
    return {
     amount: "" 
    };
  },  
  methods: {
    runSimpleStorage: function(opp ) {
 
        this.lib_runContractOpps(opp, this.amount);

        },

    runReturnOps: function() {
     this.toggleForReciept = false;
     this.$store.commit('setToggleForReciept');
    }
  },

  computed:  { 

    sol_data: function()  {
      var storeData = this.$store.state.sol_data;
      console.debug("storeData "+storeData);
      return storeData;
    },
    
    toggleForReciept: function()  {
      var storeData = this.$store.state.toggleForReciept;
      return storeData;
      }
    },
    
  };
 
</script>

<style >
.wrapper {
  border:2px solid hsl(0, 0%, 86%);
  margin: 10px 0 0 8px;
  
}
</style>