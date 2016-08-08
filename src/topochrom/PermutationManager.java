package topochrom;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class PermutationManager
{

    private static PermutationManager singleton;
    private static Integer lock;
    private int numTransactionsReceived = 0;
    
    public PeerGroup peers;
    public HashMap<Peer, Integer> peerIndexHashMap;
    public HashMap<Sha256Hash, TransactionPermutation> transactionPermutationHashMap;
    public TransactionReceivedListener listener;

    public PermutationManager()
    {
        this.peerIndexHashMap = new HashMap<Peer, Integer>();
        this.transactionPermutationHashMap = new HashMap<Sha256Hash, TransactionPermutation>();
        this.listener = new TransactionReceivedListener();
    }

    public static PermutationManager getPermutationManager()
    {
        if (singleton == null)
        {
            singleton = new PermutationManager();
        }
        return singleton;
    }

    public void setPeers(PeerGroup peers)
    {
        this.peers = peers;
        int i = 0;
        for (Peer peer : peers.getConnectedPeers())
        {
            peerIndexHashMap.put(peer, i++);
        }
    }

    public void addPeerToTransaction(Peer peer, Sha256Hash txHash)
    {
        synchronized (lock)
        {
            TransactionPermutation txPerm = transactionPermutationHashMap.get(txHash);
            int index = peerIndexHashMap.get(peer);
            txPerm.addIndex(index);

            // if this is the last call for this transaction, count it so we can tell when we are done.
            if (txPerm.permutation.size() == peerIndexHashMap.size())
            {
                numTransactionsReceived++;
            }
        }
    }

    public void addLabelledTx(Sha256Hash txHash, int peerIndex)
    {
        TransactionPermutation txPerm = new TransactionPermutation(txHash, peerIndex);
        transactionPermutationHashMap.put(txHash, txPerm);
        listener.addTargetTxHash(txHash);
    }

    public int getNumTransactionsReceived()
    {
        return this.numTransactionsReceived;
    }

    public static int[][] getDistanceMatrix(List<TransactionPermutation> txs)
    {
        int[][] distanceMatrix = new int[txs.size()][txs.size()];

        for (int i = 0; i < txs.size(); i++)
        {
            TransactionPermutation perm1 = txs.get(i);
            distanceMatrix[i][i] = 0;
            for (int j = i + 1; j < txs.size(); j++)
            {
                TransactionPermutation perm2 = txs.get(j);
                int dist = distanceBetweenPermutations(perm1.permutation, perm2.permutation);
                distanceMatrix[i][j] = distanceMatrix[j][i] = dist;
            }
        }
        return distanceMatrix;
        // TODO write the distance matrix to file
    }

    // Kendall-Tau distance https://en.wikipedia.org/wiki/Kendall_tau_distance
    public static Integer distanceBetweenPermutations(List<Integer> permutation1, List<Integer> permutation2)
    {
        int distance = 0;
        // loop over distinct unordered pairs of indices
        for (int i = 0; i < permutation1.size(); i++)
        {
            for (int j = i + 1; j < permutation2.size(); j++)
            {
                // if i and j are in different orders in each permutation, then they will need to be swapped
                if ((permutation1.get(i) > permutation1.get(j) && permutation2.get(i) < permutation2.get(j)) ||
                        (permutation1.get(i) < permutation1.get(j) && permutation2.get(i) > permutation2.get(j)))
                {
                    // one transposition changes one such ordering, leaves all others intact
                    distance++;
                }
            }
        }
        return distance;
    }

    // https://en.wikipedia.org/wiki/Kendall_rank_correlation_coefficient
    public static double significanceTest()
    {
        // TODO
       return 0.0;
    }
}
