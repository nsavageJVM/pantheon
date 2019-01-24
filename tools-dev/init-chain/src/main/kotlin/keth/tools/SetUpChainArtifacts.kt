package keth.tools

import keth.tools.db.bootStrapDbArtifacts
import keth.tools.utils.bootStrapChainArtifacts

fun main() {

    val addrs:Triple<String,String,String> = bootStrapChainArtifacts()
    bootStrapDbArtifacts(addrs)



}