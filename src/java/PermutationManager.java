package java;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class PermutationManager
{

    private static PermutationManager singleton;
    private static Integer lock;

    public List<Peer> peers;
    public HashMap<Peer, Integer> peerIndexHashMap;
    public HashMap<Sha256Hash, TransactionPermutation> transactionPermutationHashMap;

    public PermutationManager(List<Peer> peers)
    {
        this.peers = peers;
        this.peerIndexHashMap = new HashMap<Peer, Integer>();
        this.transactionPermutationHashMap = new HashMap<Sha256Hash, TransactionPermutation>();

        int i = 0;
        for (Peer peer : peers)
        {
            peerIndexHashMap.put(peer, i++);
        }
    }

    public static PermutationManager getPermutationManager()
    {
        return singleton;
    }

    public void addPeerToTransaction(Peer peer, Sha256Hash txHash)
    {
        synchronized (lock)
        {
            TransactionPermutation perm = transactionPermutationHashMap.get(txHash);
            int index = peerIndexHashMap.get(peer);
            assert perm.permutation.contains(index) == false;
            perm.permutation.add(index);

        }
    }
}
