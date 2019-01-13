# Tools for Panetheon

## See project `tools dev`

### Currently implements scripts to configure a private 3 node chain  with a Clique POA Consensus loosley following this recipie 
[Creating a Private Network](https://docs.pantheon.pegasys.tech/en/latest/Tutorials/Create-Private-Network/)

 See module / package keth.tools.utils.ChainUtils main function to create directorys, artifacts and scripts 

```
 fun main(args : Array<String>) {
    InitChain.initDirs()
    InitChain.buildDistribution()
    InitChain.copyDistribution()
    InitChain.initBinDir()
    KeyUtils.generateKeysForBootNode()

    var scriptPath =  Constants.bootNodePath.resolve("run_boot_node.sh")
    BashProvider.writeScript(scriptPath, TYPE.BOOT)
    scriptPath =  Constants.bootNode2.resolve("run_node_2.sh")
    BashProvider.writeScript(scriptPath, TYPE.NODE2)
    scriptPath =  Constants.bootNode3.resolve("run_node_3.sh")
    BashProvider.writeScript(scriptPath, TYPE.NODE3)

}
```

if you manage to get this to run on your box then shoud see something on your box similar to

![demo](https://github.com/nsavageJVM/pantheon/blob/tools_dev/tools-dev/prichain.png "dirs, scripts, artifacts")

run the scripts and you should get a chain composed of a boot node with 2 peers running with clique

### TODO tools to work with a clique private chai
* [gui](https://github.com/msink/kotlin-libui)   
* account management /  contract tools  [web3](https://github.com/web3j/web3j)  
* sidechain
      