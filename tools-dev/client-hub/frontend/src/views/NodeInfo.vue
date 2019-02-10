<template>
<div class="columns is-fullheight  is-mobile flex-box" >
    <div class="column is-3 sidebar">
        <div class="title is-dark">Node Data</div>
        <p class="notification is-dark">Peer count: {{peer_count}}</p>
        <p class="notification is-dark">Last block: {{last_block}}</p>
    </div>
    <div class="column is-9 content" >
        <div class="title" style="display:block;max-width:320px;">Node VM Metrics</div>
        <div class="tile is-ancestor is-mobile">
            <div class="tile is-vertical is-4">
                <div class="tile is-parent is-vertical">
                    <div class="tile is-child notification has-background-black chart_box">
                        <p class="title chart-title">CPU</p>
                        <apexchart width="250" type="radialBar" :options="chartOptions" :series="mx_cpu"></apexchart>
                    </div>

                    <div class="tile is-child notification has-background-black chart_box">
                        <p class="title chart-title">Memory:Free</p>
                        <apexchart width="250" type="radialBar" :options="chartOptions" :series="mx_mem_free"></apexchart>
                    </div>
                </div><!-- end  is-parent  -->
            </div><!-- end  is-4  -->

            <div class="tile is-vertical is-4">
                <div class="tile is-parent is-vertical ">
                      <div class="tile is-child notification has-background-black chart_box">
                          <p class="title chart-title">DB Size</p>
                          <apexchart width="250" type="radialBar" :options="chartOptions" :series="mx_db_size"></apexchart>
                      </div>

                      <div class="tile is-child notification has-background-black chart_box">
                            <p class="title chart-title">Disk:Free Space</p>
                            <apexchart width="250" type="radialBar" :options="chartOptions" :series="mx_disk_free_space"></apexchart>
                      </div>
                </div><!-- end  is-parent  -->
            </div><!-- end  is-4  -->
        </div><!-- end  ancestor -->
    </div><!-- end  section -->
 
</div>  
</template>
<style>
text.apexcharts-datalabel-label {
  text-decoration: none !important;
  fill:crimson !important;
}
text.apexcharts-datalabel-value {
  text-decoration: none !important;
  fill:rgb(217, 220, 20) !important;
  font-size:.9em;
}

 .apexcharts-svg {
  margin-left:-28%  !important;
  margin-top:-17%  !important;
  max-width:220px;
  max-height:240px;
  border:transparent;
}

.chart_box {
 
   max-width:230px;
   max-height:240px;
   border:4px solid rgb(0, 4, 255);
   padding:0px;
}

.tile {
    padding:0px 0px 0px 0px; 
 
}

.chart-title {
  padding:10px 0 0 0; 
  text-decoration: none !important;
  color:crimson !important;
  font-size:1.2em;
  margin-left:-8%  !important;
}

.sidebar {
    width: 220px !important;
    min-width: 200px !important;
    overflow: visible;
    border:4px solid rgb(0, 4, 255);
     margin-right:8%  !important;
}
</style>

<script>
export default {
  name: 'NodeCpu',
  data: function() {
    return {
      chartOptions: {
        labels: ["Percent"],
      },
    }
  },

mounted: function () {
    var call_timer = null
    let mssg = 
            this.$jsonRpcMessageProvider(
                            this.$jsonRpcMethods.NET_PEER_COUNT,
                            this.$jsonRpcTopics.NET_PEER_COUNT);
   
    this.lib_processJsonRpc(mssg);



    mssg = 
            this.$jsonRpcMessageProvider(
                            this.$jsonRpcMethods.ETH_BLOCK_NUMBER,
                            this.$jsonRpcTopics.ETH_BLOCK_NUMBER);

    this.lib_processJsonRpc(mssg);

    const context = this;
    this.call_timer = setInterval(
        function() { context.lib_processJsonRpc(mssg) }, 7500)
    },

created() {


  },

computed: {

      //mx bean data  {  mx_mem_total, mx_db_size, mx_disk_free_space} 
    mx_cpu() {
       var storeData =  this.$store.state.mx_cpu;
       storeData = storeData*4;
       return [storeData];
    },

    mx_mem_free() {
       var storeData =  this.$store.state.mx_mem_free;
       storeData = Math.trunc(storeData*1e-7);
       return [storeData];
    },
    mx_db_size() {
       var storeData =  this.$store.state.mx_db_size;
        storeData = Math.trunc(storeData*1e-7);
       return [storeData];
    },

    mx_disk_free_space() {
     var storeData =  this.$store.state.mx_disk_free_space;
         storeData = Math.trunc(storeData*1e-9);
       return [storeData];
    },
    // w3 api
    peer_count() {
        var store_peer_count =  this.$store.state.peer_count;
        return store_peer_count;
    },

    last_block() {
        var store_last_block =  this.$store.state.last_block;
        return store_last_block;
    }

  },
 

}
</script>