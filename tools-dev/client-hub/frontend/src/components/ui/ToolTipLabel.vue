<template>
<div>
  <div  @mouseover ="runShowToolTip(true)"  @mouseleave ="runShowToolTip(false)" >
      <a v-show="showToolTip" class="label">{{ value }} </a>
 
      <a v-if="!showToolTip" class="label" 
            style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:240px;margin:0 0 0 20px" >
            {{ value }} </a>
  </div>
 </div>   
</template>

<script>
export default {
  name: "ToolTipLabel",
    props: {
    value: String,

 
  },
  components: {},

  data: function() {
    return {

     showToolTip: false

    };
  },

  methods: {
      runShowToolTip: function(toggle ) {
        if(toggle) {
          this.showToolTip = true;
        } else {
          this.showToolTip = false;
        }

        console.debug("showToolTip "+this.showToolTip)

      }
    }
     
};

</script>