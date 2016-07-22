package java;

import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class TopoChromExperiment {

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
        // TODO TerraFlux here?
        return null;
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
        manager.writeDataToFile("data.csv");

    }

}
