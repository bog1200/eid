// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/// @title OrangeIDP - Privacy-preserving Identity Proofs
contract OrangeIDP {
    /// @notice A record linking a wallet address to the hash of their identity data
    struct IdentityProof {
        string did;     // Decentralized Identifier
        bytes32 dataHash; // Hash of the off-chain identity data
    }

    mapping(address => IdentityProof) private identities;

    event IdentityProofRegistered(address indexed user, string did, bytes32 dataHash);
    event IdentityProofUpdated(address indexed user, string did, bytes32 dataHash);
    event IdentityProofDeleted(address indexed user, string did);

    /// @notice Register a new identity proof
    /// @param _did The Decentralized Identifier (DID)
    /// @param _dataHash The keccak256 hash of the identity data (off-chain)
    function registerIdentityProof(string memory _did, bytes32 _dataHash) external {
        require(identities[msg.sender].dataHash == 0, "Identity already exists");

        identities[msg.sender] = IdentityProof({
            did: _did,
            dataHash: _dataHash
        });

        emit IdentityProofRegistered(msg.sender, _did, _dataHash);
    }

    /// @notice Update an existing identity proof
    /// @param _dataHash The new keccak256 hash of the updated identity data
    function updateIdentityProof(bytes32 _dataHash) external {
        require(identities[msg.sender].dataHash != 0, "Identity does not exist");

        identities[msg.sender].dataHash = _dataHash;

        emit IdentityProofUpdated(msg.sender, identities[msg.sender].did, _dataHash);
    }

    /// @notice Get the identity proof for a given address
    /// @param _user The address of the user
    /// @return did The Decentralized Identifier (DID)
    /// @return dataHash The hash of the identity data
    function getIdentityProof(address _user) external view returns (string memory did, bytes32 dataHash) {
        require(identities[_user].dataHash != 0, "Identity does not exist");
        IdentityProof memory identity = identities[_user];
        return (identity.did, identity.dataHash);
    }

   /// @notice Remove identity proof for the caller
function removeIdentityProof() external {
    require(identities[msg.sender].dataHash != 0, "No identity exists");
    string memory did = identities[msg.sender].did;
    delete identities[msg.sender];
    emit IdentityProofDeleted(msg.sender, did);
}
}
