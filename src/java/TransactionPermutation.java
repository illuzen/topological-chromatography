package java;

import org.bitcoinj.core.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snakecharmer1024 on 7/20/16.
 */
public class TransactionPermutation
{
    public Sha256Hash txHash;
    public List<Integer> permutation;
    public int peerLabel;

    public TransactionPermutation(Sha256Hash txHash, int peerLabel)
    {
        this.txHash = txHash;
        this.peerLabel = peerLabel;
        this.permutation = new ArrayList<Integer>();
    }

    public void addIndex(int index)
    {
        assert permutation.contains(index) == false;
        permutation.add(index);
    }

}
