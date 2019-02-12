pragma solidity >=0.4.22 <0.6.0;
// https://marketplace.visualstudio.com/items?itemName=JuanBlanco.solidity

import "./ECComplienceToken.sol";

interface tokenRecipient { function receiveApproval(address _from, uint256 _value, address _token, bytes calldata _extraData) external; }

contract PowerBudgetToken is ECComplienceToken {

    string public name = "Power Budget Token";
    uint8 public decimals;   //How many decimals to show. ie. 
    //There could 1000 base units with 3 decimals. Meaning 0.980 SBX = 980 base units. 
    // It's like comparing 1 wei to 1 ether.
    string public symbol = "PBT";
    string public standard = "PBdg Token v1.0";
    uint256 public totalSupply;

    constructor(
        uint256 _initialAmount,
        string memory _tokenName,
        uint8 _decimalUnits,
        string memory _tokenSymbol
        ) public {
        balances[msg.sender] = _initialAmount;               // Give the creator all initial tokens
        totalSupply = _initialAmount;                        // Update total supply
        name = _tokenName;                                   // Set the name for display purposes
        decimals = _decimalUnits;                            // Amount of decimals for display purposes
        symbol = _tokenSymbol;                               // Set the symbol for display purposes
    }

   /**
     * Set allowance for other address and notify
     *
     * Allows `_spender` to spend no more than `_value` tokens in your behalf, and then ping the contract about it
     *
     * @param _spender The address authorized to spend
     * @param _value the max amount they can spend
     * @param _extraData some extra information to send to the approved contract
     */
    function approveAndCall(address _spender, uint256 _value, bytes memory _extraData)
        public
        returns (bool success) {
        tokenRecipient spender = tokenRecipient(_spender);
        if (approve(_spender, _value)) {
            spender.receiveApproval(msg.sender, _value, address(this), _extraData);
            return true;
        }
    }

  
}