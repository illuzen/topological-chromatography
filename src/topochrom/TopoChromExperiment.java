package topochrom;

import org.bitcoinj.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bitcoinj.utils.BriefLogFormatter;

/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class TopoChromExperiment {

    private static final Logger log = LoggerFactory.getLogger(TopoChromExperiment.class);

    private static PermutationManager manager;

    private static PeerGroup listeningPeers;

    private static List<PeerGroup> sendingPeers;

    private static NetworkParameters parameters;

    private static List<Transaction> txs;

    private static int waitTimeMilli = 100;

    private static int numSendingPeers = 2;

    private static int numListeningPeers = 8;

    private static void runExperiment(Transaction tx)
    {
        // randomly select a sending peer to send to
        // this peer is the data label for that transaction "tx"
        Random random = new Random();
        int peerIndex = random.nextInt();
        PeerGroup entryPeer = sendingPeers.get(peerIndex % numSendingPeers);
        manager.addLabelledTx(tx.getHash(), peerIndex);
        entryPeer.broadcastTransaction(tx);
    }

    private static ArrayList<Transaction> createTxList()
    {
        // TODO TerraFlux here?
        return null;
    }

//    private static ArrayList<PeerAddress> getListeningPeerAddresses()
//    {
//        // TODO we need to make this fixed, we should probably set up the bitcoin nodes ourselves
//
//        return null;
//    }
//
//    private static ArrayList<PeerAddress> getSendingPeerAddresses()
//    {
//        // TODO we need to make this fixed, we should probably set up the bitcoin nodes ourselves
//        return null;
//    }

    private static void initializeListeningPeers()
    {
        listeningPeers = new PeerGroup(parameters);
        listeningPeers.setBloomFilteringEnabled(false);
        listeningPeers.setMinBroadcastConnections(numListeningPeers);
        listeningPeers.addPeerDiscovery(new DnsDiscovery(parameters));
        //listeningPeers.getTorClient().
        listeningPeers.addConnectedEventListener(new PeerConnectedEventListener() {
            @Override
            public void onPeerConnected(final Peer peer, int peerCount) {
                log.info("Peer Connected: " + peer.toString());
                log.info(peerCount + " peers total");
            }
        });
        listeningPeers.addDisconnectedEventListener(new PeerDisconnectedEventListener() {
            @Override
            public void onPeerDisconnected(final Peer peer, int peerCount) {
                log.info("Peer Disconnected: " + peer.toString());
                log.info(peerCount + " peers left");
            }
        });

        try
        {
            listeningPeers.startAsync();
            listeningPeers.waitForPeers(numListeningPeers).get();
        }
        catch (InterruptedException e)
        {
            System.out.println(e);
            System.exit(1);
        }
        catch (ExecutionException e)
        {
            System.out.println(e);
            System.exit(1);
        }
        //listeningPeers.start();


        /*
        ArrayList<PeerAddress> listeningPeerList = getListeningPeerAddresses();
        for (PeerAddress addr: listeningPeerList)
        {
            listeningPeers.addAddress(addr);
        }
        */
        manager.setPeers(listeningPeers);

    }

    private static void initializeSendingPeers()
    {
        sendingPeers = new ArrayList<PeerGroup>();
        for (int i = 0; i < numSendingPeers; i++)
        {
            sendingPeers.add(new PeerGroup(parameters));
        }

        /*
        ArrayList<PeerAddress> sendingPeerList = getSendingPeerAddresses();
        for (PeerAddress addr: sendingPeerList)
        {
            PeerGroup group = new PeerGroup(parameters);
            group.addAddress(addr);
            sendingPeers.add(group);
        }
        */

        // wait for them all to connect
        for (PeerGroup sendingPeer : sendingPeers)
        {
            sendingPeer.setMaxConnections(1);
            sendingPeer.start();
        }

    }

    // used to make sure all txs return before attempting to analyze the data
    private static void waitForExperimentsToComplete()
    {
        while (manager.getNumTransactionsReceived() < txs.size())
        {
            try
            {
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
        BriefLogFormatter.init();

        // We will change this to ID_MAINNET when we productionize it...
        parameters = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);
        manager = PermutationManager.getPermutationManager();
        initializeListeningPeers();
        initializeSendingPeers();

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
        System.out.println("Goodness:" + goodness);
    }

}
