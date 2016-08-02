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

    private static PermutationManager singleton; // declares that there can only ever be one PermutationManager instance at a time?
    private static Integer lock; // declared for the purposes for the synchronized block later?

    public List<Peer> peers; //array of peer objects
    public HashMap<Peer, Integer> peerIndexHashMap; //hashmap of peer to index relationship
    public HashMap<Sha256Hash, TransactionPermutation> transactionPermutationHashMap; //hashmap of the transaction's hash and a permutation object
    // which contains a list of integers corresponding to the peer/index relationship in peerIndexHashMap

    public PermutationManager(List<Peer> peers)
    {
        //constructor
        this.peers = peers;
        this.peerIndexHashMap = new HashMap<Peer, Integer>();
        this.transactionPermutationHashMap = new HashMap<Sha256Hash, TransactionPermutation>();

        int i = 0;
        for (Peer peer : peers)
        {
            peerIndexHashMap.put(peer, i++); // establishes the peer/index relationship in the peerIndexHashMap
        }
    }

    public static PermutationManager getPermutationManager()
    {
        return singleton; //calls the constructor as a singleton?
    }

    public void addPeerToTransaction(Peer peer, Sha256Hash txHash)
    {
        synchronized (lock) //thread-safe zone in order not to overwrite or collide in the transactionPermutation mappings
        {
            TransactionPermutation perm = transactionPermutationHashMap.get(txHash); //loads permutation array for the correct transaction
            int index = peerIndexHashMap.get(peer); //loads index of peer argument from the peerIndexHashMap
            assert perm.permutation.contains(index) == false; //makes sure that the transactionPermutationHashMap for the tx does not already contain the peer's index
            perm.permutation.add(index); // if pass, then add the index to the permutation map

        }
    }
}
