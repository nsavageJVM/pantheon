<template>
<div class="columns is-fullheight flex-box" >
 
    <div class="column is-4">
       <nav class="panel"> 
        <p class="panel-heading">Accounts</p>
        <table class="table is-striped has-background-grey-lighter" style="min-width:100%;">  
            <tbody>
            <tr v-for="(data, index) in accts_list" :key="data"  >
                <th  >{{ index  }}</th>
                <td style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:120px;">{{ data  }}</td>
                <td  ><a class="button is-link">Get Balance</a></td>
            </tr>
            </tbody>
        </table>
        </nav>
    </div>
    <div class="column is-9" >
        <nav class="panel"> 
            <p class="panel-heading" style="max-width:80%;">Balance</p>
            <p class="is-medium has-text-grey"  style="max-width:80%;">feedback data</p>
        </nav>
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
    return { accts_list : {
       'acct1': '0x4c6666106f61a85a7ca15b95e8c072b1ec902439',
       'acct2': '0x217c0a153fcfb74946b8a15f7b3de958b3ba310b',
       'acct3': '0x60874873b677db1422ad44f5f6f888d169ce2a78' }
    }
  },
  
  mounted: function () {
       //  this.lib_getAccount();
  },

  computed: {

//    accts_list() {
//        var storeData =  this.$store.state.accts;
//        return storeData;
//     },

  }
}
</script>