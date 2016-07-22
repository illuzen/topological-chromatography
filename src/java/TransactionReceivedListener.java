package java;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;

import java.util.ArrayList;


/**
 * Created by snakecharmer1024 on 7/19/16.
 */
public class TransactionReceivedListener implements PreMessageReceivedEventListener {

    public ArrayList<Sha256Hash> targetTxHashes;

    public TransactionReceivedListener()
    {
        super();
        this.targetTxHashes = new ArrayList<Sha256Hash>();
    }

    public void addTargetTxHash(Sha256Hash hash)
    {
        this.targetTxHashes.add(hash);
    }

    public Message onPreMessageReceived(Peer peer, Message message)
    {
        if (message instanceof InventoryMessage)
        {
            InventoryMessage invMessage = (InventoryMessage) message;
            for (InventoryItem item : invMessage.getItems())
            {
                if (this.targetTxHashes.contains(item.hash))
                {
                    PermutationManager.getPermutationManager().addPeerToTransaction(peer, item.hash);
                    break;
                }
            }
        }
        return message;
    }

}
