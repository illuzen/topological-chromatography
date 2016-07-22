package java;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snakecharmer1024 on 7/20/16.
 */

// This class is created to track the ordinality of PeerGroup 
// transmissions with regard to a specific transaction.

public class TransactionPermutation {

    public Sha256Hash txHash; //SHA256 encoded transaction information
    public Peer entryPeer; // is entryPeer self? (ie entry node of network)
    public List<Integer> permutation; 
    /* array of n integers where n is the size of the peer group 
	the integers represent the index number of the original peer group ordering
    will be updated by TransactionReceivedListener
    */

    public TransactionPermutation(Peer entryPeer, Sha256Hash txHash)
    {
    	//constructor
        this.txHash = txHash;
        this.entryPeer = entryPeer;
        this.permutation = new ArrayList<Integer>();
    }

}
