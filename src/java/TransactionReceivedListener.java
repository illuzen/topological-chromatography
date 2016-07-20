package java;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;


/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class TransactionReceivedListener implements PreMessageReceivedEventListener {

    public Sha256Hash targetTxHash;


    public TransactionReceivedListener(Transaction tx) {
        this(tx.getHash());
    }

    public TransactionReceivedListener(Sha256Hash hash) {
        this.targetTxHash = hash;
    }

    public Message onPreMessageReceived(Peer peer, Message message)
    {
        if (message instanceof InventoryMessage)
        {
            InventoryMessage invMessage = (InventoryMessage) message;
            for (InventoryItem item : invMessage.getItems())
            {
                if (item.hash == this.targetTxHash)
                {
                    PermutationManager.getPermutationManager().addPeerToTransaction(peer, item.hash);
                    break;
                }
            }
        }
        return message;
    }

}
