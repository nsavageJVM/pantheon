<template>
  <div
      v-if="show_modal"
      @close="show_modal = false"
    >
      <transition name="modal">
        <div class="modal-mask">
          <div class="modal-wrapper">
            <div class="modal-container">
              <div class="modal-header">
                {{m_text}}
              </div>
              <div class="modal-body">
                <ProgressContent
                  :total="prog_total"
                  :done="prog_done"
                  :modify="prog_modify"
                  :tip='
                      [
                          {
                              text: "loading",
                              fillStyle:prog_col1,
                          }, {
                              text: "contract",
                              fillStyle:prog_col2,
                          }, {
                              text: "data",
                              fillStyle:prog_col3
                          }
                      ]
                      '
                ></ProgressContent>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
</template>

<script>
import ProgressContent from "@/components/ui/ProgressContent.vue";

export default {
  name: "ProgressModal",
  props: {
    m_text: String
  },
 

  components: {
    ProgressContent
  },

  mounted() {
    this.genProgressData();
  },

  data: function() {
    return {
      prog_total: 100,
      prog_done: 0,
      prog_modify: 0,
      prog_col1: "rgb(224,255,255)",
      prog_col2: "rgb(174, 193, 17)",
      prog_col3: "rgb(249, 149, 0)"
    };
  },
  
  methods: {
 
    genProgressData() {
      let chunk = 20;
      const context = this;
      let  progressRunner = setInterval(function() {
      if(context.prog_modify < 10 || context.prog_modify > 99){
        context.prog_modify = 0;
        let tmp = context.prog_col1;
        context.prog_col1 = context.prog_col2;
        context.prog_col2 = context.prog_col3;
        context.prog_col3 = tmp;
      }
      if(context.prog_done < 0 || context.prog_done > 99){
        context.prog_done =0;
      }
      context.prog_done =  context.prog_done + chunk;
      context.prog_modify = context.prog_total - context.prog_done;
       console.debug("done: "+ context.prog_done+"  modify: "+ context.prog_modify) 
      },1000);
      }
  },

  computed: {
 
    show_modal() {
      let  result =  this.$store.state.show_modal;
      return result;
    }
  }

};
</script>
 
<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(105, 99, 99, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-container {
  width: 300px;
  margin: 0px auto;
  padding: 20px 30px;
  background-color: #fff;
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  transition: all 0.3s ease;
  font-family: Helvetica, Arial, sans-serif;
  color: black;
}

.modal-header h3 {
  margin-top: 0;
  color: black;
}

.modal-body {
  margin: 20px 0;
}

.modal-enter .modal-container,
.modal-leave-active .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}
</style>
