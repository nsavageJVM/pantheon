# Tools for Panetheon

## See project `tools dev`

#### why remove all the tests, test support and jmh?
to try and harmonise patheon gradle modules as dependencies with a gui kotlin native multi platform gradle project
[gui](https://github.com/msink/kotlin-libui)


### Currently implements scripts to configure a private 3 node chain  with a Clique POA Consensus loosley following this recipie 
[Creating a Private Network](https://docs.pantheon.pegasys.tech/en/latest/Tutorials/Create-Private-Network/)

See module / package keth.tools.utils.ChainUtils main function to create directory's, artifacts and scripts

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
```
// in chains/private/node1
sh run_boot_node.sh
// in  chains/private/nodeN
sh run_node_N.sh
// test
curl -X POST --data '{"jsonrpc":"2.0","method":"net_peerCount","params":[],"id":1}' 127.0.0.1:8545
```

### Account management
under development
see
```
keth.tools.account

fun main(args: Array<String>) {
    WalletProvider.createEncryptedRocksDbWallet("this is pop2")
    WalletProvider.getRocksDbWallet("this is pop2")
}
```
#### above code
* creates a json string that represents a wallet pub/pri EC keys from [web3](https://github.com/web3j/web3j)
* encrypts this string with a EC key stored in a java keystore
* stores the encyrpted bytes with cipher parameters in rocksdb
* retrives the encrypted bytes and decrypts the json wallet


### [Gui](https://github.com/msink/kotlin-libui)
* unable to harmonise kotlin native gradle plugin with pantheon gradle setup
* latest ides is set up a backend in pantheon and set up secure protocol to a completely independent kNative project
