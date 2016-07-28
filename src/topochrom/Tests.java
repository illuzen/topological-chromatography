package topochrom;

import java.util.Arrays;
import java.util.List;

/**
 * Created by snakecharmer1024 on 7/28/16.
 */
public class Tests
{
    public static boolean testPermutationDistance()
    {
        boolean success = true;
        List<Integer> permutation1 = Arrays.asList(1,2,3,4,5);
        List<Integer> permutation2 = Arrays.asList(5,4,3,2,1);
        success &= PermutationManager.distanceBetweenPermutations(permutation1, permutation2) == 10;

        permutation1 = Arrays.asList(1,2,3);
        permutation2 = Arrays.asList(3,2,1);
        success &= PermutationManager.distanceBetweenPermutations(permutation1, permutation2) == 3;

        permutation1 = Arrays.asList(1,2,3);
        permutation2 = Arrays.asList(2,3,1);
        success &= PermutationManager.distanceBetweenPermutations(permutation1, permutation2) == 2;

        permutation1 = Arrays.asList(1,2,3);
        permutation2 = Arrays.asList(2,1,3);
        success &= PermutationManager.distanceBetweenPermutations(permutation1, permutation2) == 1;

        permutation1 = Arrays.asList(1,2,3,4,5,6,7);
        permutation2 = Arrays.asList(7,6,5,4,3,2,1);
        success &= PermutationManager.distanceBetweenPermutations(permutation1, permutation2) == 21;



        return success;
    }

    public static void main(String[] args)
    {
        String results = "Permutation distance test: " + (testPermutationDistance() ? "PASSED" : "FAILED");
        System.out.println(results);
    }
}
