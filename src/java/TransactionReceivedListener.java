package java;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;


/**
 * Created by snakecharmer1024 on 7/19/16.
 */

/** TransactionReceivedListener inherits from PreMessageReceived
 *    class from bitcoinj libraries.
*/  

public class TransactionReceivedListener implements PreMessageReceivedEventListener {

    public Sha256Hash targetTxHash; //single class property


    public TransactionReceivedListener(Transaction tx) {
        this(tx.getHash()); //on transaction received, 
    } // calls itself with a different data type(hash of the transaction originally passed in)?


    // why seperate the logic? will the second listener instantiate on listening too?

    public TransactionReceivedListener(Sha256Hash hash) {
        this.targetTxHash = hash;
    }



    public Message onPreMessageReceived(Peer peer, Message message)
    {
        if (message instanceof InventoryMessage) // InventoryMessage class indentifies if message is of relevant transaction/block data
        {
            InventoryMessage invMessage = (InventoryMessage) message; //set invMessage to message as a class instance of Inventory Message?
            for (InventoryItem item : invMessage.getItems()) // for loop of each item in invMessage.
            {
                if (item.hash == this.targetTxHash) // does this item have the same hash as the transaction we are listening for?
                {
                    PermutationManager.getPermutationManager().addPeerToTransaction(peer, item.hash); //then add the peer and tx to the transactionPermutationHashMap
                    break; 
                }
            }
        }
        return message; // otherwise return the message, is this needed?
    }

}
