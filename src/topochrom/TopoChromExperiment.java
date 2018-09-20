package topochrom;

import org.bitcoinj.core.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bitcoinj.utils.BriefLogFormatter;

/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class TopoChromExperiment {

    private static Wallet wallet;

    private static final Logger log = LoggerFactory.getLogger(TopoChromExperiment.class);

    private static PermutationManager manager;

    private static PeerGroup listeningPeers;

    private static List<PeerGroup> sendingPeers;

    private static NetworkParameters parameters;

    private static List<Transaction> txs;

    private static int numSendingPeers = 2;

    private static int numListeningPeers = 8;

    private static String walletFilename;

    private static boolean mainnet = false;

    private static int feeSatoshis = 20000;

//    private static void runExperiment(Transaction tx)
    private static void runExperiment()
    {
        TransactionOutput txO = wallet.getUnspents().get(0);
        Address adr = wallet.freshReceiveAddress();
        Coin amount = txO.getValue().subtract(Coin.valueOf(feeSatoshis));
        SendRequest request = SendRequest.to(adr, amount);
        try
        {
            wallet.completeTx(request);
        }
        catch (InsufficientMoneyException e)
        {
            System.err.println(e);
            System.exit(1);
        }
        Transaction tx = request.tx;

        // randomly select a sending peer to send to
        // this peer is the data label for that transaction "tx"
        Random random = new Random();
        int peerIndex = random.nextInt(numSendingPeers);
        PeerGroup entryPeer = sendingPeers.get(peerIndex);
        manager.addLabelledTx(tx.getHash(), peerIndex);
        entryPeer.broadcastTransaction(tx);
        manager.waitForExperimentToComplete();
    }

    private static ArrayList<Transaction> createTxList()
    {
        ArrayList<Transaction> txList = new ArrayList<Transaction>();
        TransactionOutput txO = wallet.getUnspents().get(0);
        for (int i=0;i < 10;i++)
        {
            ECKey key = new ECKey();
            Address adr = key.toAddress(parameters);
            wallet.importKey(key);

//            Address adr = wallet.freshReceiveAddress();
//            Coin amount = txO.getValue().subtract(Coin.valueOf(feeSatoshis));
//            SendRequest request = SendRequest.to(adr, amount);
//            try
//            {
//                wallet.completeTx(request);
//            }
//            catch (InsufficientMoneyException e)
//            {
//                System.err.println(e);
//                System.exit(1);
//            }
            Coin amount = txO.getValue().subtract(Coin.valueOf(feeSatoshis));
            Transaction tx = new Transaction(parameters);
            tx.addOutput(amount, adr);
            TransactionInput input = tx.addSignedInput(txO, key);

            log.info(input.getScriptSig().toString());
            log.info(input.toString());
            tx.verify();
            txList.add(tx);

            wallet.commitTx(tx);
            txO = tx.getOutput(0);
        }



        return txList;
        
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
//        listeningPeers.addConnectedEventListener(new PeerConnectedEventListener() {
//            @Override
//            public void onPeerConnected(final Peer peer, int peerCount) {
//                log.info("Peer Connected: " + peer.toString());
//                log.info(peerCount + " peers total");
//            }
//        });
//        listeningPeers.addDisconnectedEventListener(new PeerDisconnectedEventListener() {
//            @Override
//            public void onPeerDisconnected(final Peer peer, int peerCount) {
//                log.info("Peer Disconnected: " + peer.toString());
//                log.info(peerCount + " peers left");
//            }
//        });
        TransactionReceivedListener listener = new TransactionReceivedListener();
        listeningPeers.addPreMessageReceivedEventListener(listener);
        manager.setListener(listener);
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
            PeerGroup group = new PeerGroup(parameters);
            group.setBloomFilteringEnabled(false);
            group.setMinBroadcastConnections(1);
            group.addPeerDiscovery(new DnsDiscovery(parameters));
            sendingPeers.add(group);
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
        try
        {
            // wait for them all to connect
            for (PeerGroup sendingPeer : sendingPeers)
            {
                sendingPeer.setMaxConnections(1);
                sendingPeer.startAsync();
                sendingPeer.waitForPeers(1).get();
            }
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



    }

    private static void initializeWallet()
    {
        try
        {
            File walletFile = new File(walletFilename);
            wallet = Wallet.loadFromFile(walletFile);
            wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
                @Override
                public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                    System.out.println("\nReceived tx " + tx.getHashAsString());
                    System.out.println(tx.toString());
                }
            });

            Context context = new Context(parameters);
            String filename;
            if (mainnet)
            {
                filename = "./mainnet.blocks";
            }
            else
            {
                filename = "./testnet.blocks";
            }
            File blockfile = new File(filename);
            BlockStore blockStore = new SPVBlockStore(parameters, blockfile);
            BlockChain chain = new BlockChain(context, wallet, blockStore);
            final PeerGroup peerGroup = new PeerGroup(parameters, chain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(parameters));
            peerGroup.startAsync();
            peerGroup.downloadBlockChain();
            peerGroup.stopAsync();
            wallet.saveToFile(walletFile);
            log.info(wallet.toString());
        }
        catch (Exception e)
        {
            System.err.println(e);
            System.exit(1);
        }


    }


    public static void main(String[] args)
    {
        BriefLogFormatter.init();

        // We will change this to ID_MAINNET when we productionize it...
        if (mainnet)
        {
            parameters = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
            walletFilename = "./mainnet-wallet.dat";
        }
        else
        {
            parameters = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);
            walletFilename = "./testnet-wallet.dat";
        }

//        try
//        {
//            wallet = new Wallet(parameters);
//            wallet.saveToFile(new File(walletFilename));
//        }
//        catch (IOException e)
//        {
//            System.err.println(e);
//        }

        manager = PermutationManager.getPermutationManager();
        initializeWallet();
        initializeListeningPeers();
        initializeSendingPeers();

//        txs = createTxList();
//        for (Transaction tx : txs)
//        {
//            runExperiment(tx);
//        }
        for (int i = 0; i < 10; i++)
        {
            runExperiment();
        }


        // get adjacency matrices for true labels and inferred labels
        List<TransactionPermutation> labelledTxPerms = new ArrayList(manager.transactionPermutationHashMap.values());
        List<TransactionPermutation> inferredTxPerms  = ClusteringManager.clusterTransactions(labelledTxPerms);

        PermutationManager.writeDistanceMatrix(labelledTxPerms, inferredTxPerms);

        boolean[][] trueAdjMat = ClusteringManager.getAdjacencyMatrix(labelledTxPerms);
        boolean[][] inferredAdjMat = ClusteringManager.getAdjacencyMatrix(inferredTxPerms);

        int goodness = ClusteringManager.clusteringGoodness(trueAdjMat, inferredAdjMat);
        System.out.println("Goodness:" + goodness);
    }

}
