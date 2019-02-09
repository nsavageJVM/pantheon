<template>
    <div id="app">
        <section class="hero has-background-white-bis is-bold is-fullheight">
            <div class="hero-head">
                <nav class="navbar" role="navigation" aria-label="main navigation">

                    <div class="navbar-brand">
                        <a class="navbar-item" href="/tools"> Chai(n) Tools </a>

                        <a role="button" class="navbar-burger" @click="showNav = !showNav" :class="{ 'is-active': showNav }">
                            <span aria-hidden="true"></span>
                            <span aria-hidden="true"></span>
                            <span aria-hidden="true"></span>
                        </a>
                    </div>

                    <div class="navbar-menu" :class="{ 'is-active': showNav }">
                        <div class="navbar-start">
                            <div class="navbar-item has-dropdown is-hoverable">
                                <a class="navbar-link"> More</a>
                                <div class="navbar-dropdown">
                                    <router-link to="/" class="navbar-item">Home</router-link>
                                    <hr class="navbar-divider">
                                    <router-link to="/node" class="navbar-item">Node Health</router-link>
                                    <router-link to="/accts" class="navbar-item">Accounts</router-link>
                                    <router-link to="/contracts" class="navbar-item">Contract Management</router-link>
                                    <hr class="navbar-divider">
                                    <router-link to="/about" class="navbar-item">Info</router-link>
                                </div>
                            </div>
                        </div> 

                        <div class="navbar-end">
                            <div class="navbar-item">
                                <div class="buttons">
                                    <a href="/logout" class="button is-primary">Logout</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </nav>
            </div>

            <div class="hero-body white-bg-value">
                    <div class="container is-fullheight ">
                        <router-view />
                    </div>
            </div>

            <div  class="hero-foot content has-text-centered">
                <p>Contact nsavagejvm @github.com/nsavageJVM 2019</p>
            </div>
        </section>
    </div>
</template>


<script>
export default {
  mounted() {
       this.lib_createJsonRpcApi();
       this.lib_searchContract();
       this.lib_createDataSocket();

  },
  data: function() {
      return {  showNav: false }
  }
}
</script>
<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #f5efef;
  margin-top: 0px;
}
.navbar-item {
  color:#ff0dcb !important;
}
 
</style>
