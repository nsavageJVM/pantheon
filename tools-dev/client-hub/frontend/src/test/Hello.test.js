// https://vue-test-utils.vuejs.org/guides/testing-single-file-components-with-jest.html
// Jest will recursively pick up all files that have a .spec.js or .test.js extension

// search contract
// [{"solAddr":"empty","solName":"empty"},{"solAddr":"empty","solName":"empty"}]
//  [{"solAddr":"empty","solName":"empty"},{"solAddr":"0x845680ae6ddc5d621568db26f475f5fee8a88b0d","solName":"PowerBudgetToken"}]
// deploy
//  {"empty_addr":" ","simple_storage":" ","power_budget_token":" "}
// lib_deployContract calls back:  {"solAddr":"0x845680ae6ddc5d621568db26f475f5fee8a88b0d","solName":"PowerBudgetToken"}
// store setContractAddress payload [object Object] store.js:105:8
// store setContractAddress loop item JSON.stringify(c_data) "0x845680ae6ddc5d621568db26f475f5fee8a88b0d"


describe('W T F', () => {
  test('is a wtf', () => {

  
    let payload =[{"solAddr":"empty","solName":"empty"},{"solAddr":"0x845680ae6ddc5d621568db26f475f5fee8a88b0d","solName":"PowerBudgetToken"}];
 

    for (var index in payload) {
      let c_data = payload[index];
      console.debug("store setContractAddress loop item c_data.solName "+c_data.solName);
      if(c_data.solName === "empty") {
        console.debug("store setContractAddress loop continue c_data empty" );
        continue;     
      } else {
 
  
        if(c_data.solName==='SimpleStorage') {
          console.debug( c_data.solAddr);
        }
        if(c_data.solName==='PowerBudgetToken') {
          console.debug(c_data.solAddr);
        }

      }

    }


 
  })
})
