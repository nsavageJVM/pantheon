<template>
  <div class="columns  wrapper" style="max-width:80%;">
    <div class="column is-4 flex-box ">
 
          <div class="field is-horizontal is-expanded">
            <div class="field-label has-text-grey">Address</div>
            <div class="field-body has-text-grey">{{value}}</div>
          </div>

          <div class="field is-horizontal is-expanded">
            <div class="field-label  has-text-grey">Name</div>
            <div class="field-body has-text-grey">to do</div>
          </div>
 
          <div class="field is-horizontal is-expanded">
            <div class="field-label  has-text-grey">Balance</div>
            <div class="field-body has-text-grey">to do</div>
          </div>
  
    </div>
  </div>
</template>

<script>
export default {
  name: "StandardToken",
    props: {
    value: String
 
  },
  components: {},

  data: function() {
    return {};
  }
};
</script>

<style >
.wrapper {
  border:2px solid hsl(0, 0%, 86%);
  margin: 10px 0 0 8px;
  
}
</style>