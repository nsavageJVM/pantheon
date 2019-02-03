// {"method":"net_peerCount", "params":[ ] } return like this, 
// missing id is like topic, input param 

export const MESSAGE_TEMPATE  = Object.freeze({
    NET_PEER_COUNT :'{"method":"net_peerCount", "params":[ ] }',
    ETH_BLOCK_NUMBER: '{"method":"eth_blockNumber","params":[]}'
 
})

export const MESSAGE_TOPICS  = Object.freeze({
    NET_PEER_COUNT :"p_ct",
    ETH_BLOCK_NUMBER: 'e-num'
 
})