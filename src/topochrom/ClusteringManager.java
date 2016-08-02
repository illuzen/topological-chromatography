package topochrom;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by snakecharmer1024 on 7/29/16.
 */
public class ClusteringManager
{
    private static Random random = new Random();

    public static List<TransactionPermutation> clusterTransactions(List<TransactionPermutation> txs)
    {

        // randomly assign to clusters;
        List<TransactionPermutation> clusteredTxs = new ArrayList<TransactionPermutation>();
        for (TransactionPermutation tx : txs)
        {
            // random peerLabel
            int r = random.nextInt(2);
            clusteredTxs.add(new TransactionPermutation(tx.txHash, r));
        }

        // dissolve and recombine
        for (int i = 0; i < 5; i++)
        {
            int[][] distanceMatrix = PermutationManager.getPermutationManager().getDistanceMatrix(clusteredTxs);
            int[] avgDistance0 = new int[distanceMatrix.length];
            int[] avgDistance1 = new int[distanceMatrix.length];
            int[][] avgDistance = {avgDistance0, avgDistance1};
            int[] nums = new int[2];


            // the average of the distance vectors is the distance vector of the average
            for (int k = 0; k < distanceMatrix.length; k++)
            {
                // add each component
                addVectors(avgDistance[clusteredTxs.get(k).peerLabel], distanceMatrix[k]);
                nums[clusteredTxs.get(k).peerLabel]++;
            }
            divideVectorByScalar(avgDistance0, nums[0]);
            divideVectorByScalar(avgDistance1, nums[1]);

            // avgDistance1 and avgDistance2 are now our centroids
            // relabel transactions
            for (int k = 0; k < distanceMatrix.length; k++)
            {
                float distanceToClassZero = distanceSquared(distanceMatrix[k], avgDistance0);
                float distanceToClassOne = distanceSquared(distanceMatrix[k], avgDistance1);
                if (distanceToClassZero < distanceToClassOne)
                {
                    clusteredTxs.get(k).peerLabel = 0;
                }
                else
                {
                    clusteredTxs.get(k).peerLabel = 1;
                }
            }

        }
        return clusteredTxs;
    }

    public static void addVectors(int[] first, int[] second)
    {
        for (int j = 0; j < first.length; j++)
        {
           first[j] += second[j];
        }
    }

    public static void divideVectorByScalar(int[] vector, float scalar)
    {
        for (int j = 0; j < vector.length; j++)
        {
            vector[j] /= scalar;
        }
    }

    public static float distanceSquared(int[] first, int[] second)
    {
        float sum = 0;
        for (int j = 0; j < first.length; j++)
        {
            float coordDist = first[j] - second[j];
            sum += coordDist * coordDist;
        }
        return sum;
    }

    public static boolean[][] getAdjacencyMatrix(List<TransactionPermutation> txs)
    {
        boolean[][] adjMat = new boolean[txs.size()][txs.size()];
        for (int i = 0; i < txs.size(); i ++)
        {
            adjMat[i][i] = true;
            for (int j = i + 1; j < txs.size(); j++)
            {
                if (txs.get(i).peerLabel == txs.get(j).peerLabel)
                {
                    adjMat[i][j] = adjMat[j][i] = true;
                }
                else
                {
                    adjMat[i][j] = adjMat[j][i] = false;
                }
            }
        }
        return adjMat;
    }

    public static int clusteringGoodness(boolean[][] first, boolean[][] second)
    {
        int rights = 0;
        for (int i = 0; i < first.length; i++)
        {
            for (int j = i + 1; j < first.length; j++)
            {
                if (first[i][j] == second[i][j])
                {
                    rights++;
                }
            }
        }

        return rights;
    }

}
