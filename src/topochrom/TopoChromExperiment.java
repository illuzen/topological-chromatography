package topochrom;

import org.bitcoinj.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bitcoinj.wallet.Wallet;
import static org.spongycastle.asn1.ua.DSTU4145NamedCurves.params;

/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class TopoChromExperiment {
    
    private static Wallet wallet;
    
    private static PermutationManager manager;

    private static PeerGroup listeningPeers;

    private static List<PeerGroup> sendingPeers;

    private static NetworkParameters parameters;

    private static List<Transaction> txs;

    private static int waitTimeMilli = 100;

    private static void runExperiment(Transaction tx)
    {
        // randomly select a sending peer to send to
        // this peer is the data label for that transaction "tx"
        Random random = new Random();
        int peerIndex = random.nextInt();
        PeerGroup entryPeer = sendingPeers.get(peerIndex % sendingPeers.size());
        manager.addLabelledTx(tx.getHash(), peerIndex);
        entryPeer.broadcastTransaction(tx);
    }

    private static ArrayList<Transaction> createTxList()
    {
        ArrayList<Transaction> txList = new ArrayList<Transaction>();
        TransactionOutput txO = wallet.getUnspents().get(0);
        for (int i=0;i > 10;i++)
        {
            Address adr = wallet.freshReceiveAddress();
            Transaction tx = new Transaction(parameters);
            tx.addInput(txO);
            tx.addOutput(txO.getValue(), adr);
            txList.add(tx);
            wallet.commitTx(tx);
            txO = tx.getOutput(0);
        }
        
        return txList;
        
    }

    private static ArrayList<PeerAddress> getListeningPeerAddresses()
    {
        // TODO we need to make this fixed, we should probably set up the bitcoin nodes ourselves
        return null;
    }

    private static ArrayList<PeerAddress> getSendingPeerAddresses()
    {
        // TODO we need to make this fixed, we should probably set up the bitcoin nodes ourselves
        return null;
    }

    private static void initializeListeningPeers()
    {
        listeningPeers = new PeerGroup(parameters);
        listeningPeers.setBloomFilteringEnabled(false);

        ArrayList<PeerAddress> listeningPeerList = getListeningPeerAddresses();
        for (PeerAddress addr: listeningPeerList)
        {
            listeningPeers.addAddress(addr);
        }
        // TODO need to make sure this waits until the peers are in CONNECTED state
        listeningPeers.waitForPeers(listeningPeerList.size());
        manager.setPeers(listeningPeers);

    }

    private static void initializeSendingPeers()
    {
        sendingPeers = new ArrayList<PeerGroup>();
        ArrayList<PeerAddress> sendingPeerList = getSendingPeerAddresses();
        for (PeerAddress addr: sendingPeerList)
        {
            PeerGroup group = new PeerGroup(parameters);
            group.addAddress(addr);
            sendingPeers.add(group);
        }

        // wait for them all to connect
        for (PeerGroup sendingPeer : sendingPeers)
        {
            sendingPeer.waitForPeers(1);
        }

    }

    private static void initializeWallet()
    {
        wallet = new Wallet(parameters);
        Context context = new Context(parameters);
        BlockChain chain = new BlockChain(context, wallet, blockstore);
    
    }
    
    private static void waitForExperimentsToComplete()
    {
        while (manager.getNumTransactionsReceived() < txs.size())
        {
            try {
                Thread.sleep(waitTimeMilli);
            }
            catch (InterruptedException ex)
            {
                System.err.println("Thread interrupted while sleeping :(");
            }
        }
    }
  
    public static void main(String[] args)
    {

        // We will change this to ID_MAINNET when we productionize it...
        parameters = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);
        initializeListeningPeers();
        initializeSendingPeers();
        manager = PermutationManager.getPermutationManager();

        txs = createTxList();
        for (Transaction tx : txs)
        {
            runExperiment(tx);
        }
        waitForExperimentsToComplete();

        // get adjacency matrices for true labels and inferred labels
        List<TransactionPermutation> labelledTxPerms = new ArrayList(manager.transactionPermutationHashMap.values());
        boolean[][] trueAdjMat = ClusteringManager.getAdjacencyMatrix(labelledTxPerms);

        List<TransactionPermutation> inferredTxPerms  = ClusteringManager.clusterTransactions(labelledTxPerms);
        boolean[][] inferredAdjMat = ClusteringManager.getAdjacencyMatrix(inferredTxPerms);

        int goodness = ClusteringManager.clusteringGoodness(trueAdjMat, inferredAdjMat);
    }

}
