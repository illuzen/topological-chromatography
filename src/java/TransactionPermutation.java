package java;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snakecharmer1024 on 7/20/16.
 */
public class TransactionPermutation
{
    public Sha256Hash txHash;
    public Peer entryPeer;
    public List<Integer> permutation;

    public TransactionPermutation(Peer entryPeer, Sha256Hash txHash)
    {
        this.txHash = txHash;
        this.entryPeer = entryPeer;
        this.permutation = new ArrayList<Integer>();
    }

}
