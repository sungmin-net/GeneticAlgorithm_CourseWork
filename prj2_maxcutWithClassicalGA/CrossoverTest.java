import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class CrossoverTest {


    private static int mNumVertex = 10;  // similar with chromosome length in GA prj1
    private static Random mRandom = new Random();
    private static final int NUM_CUTTING_POINT = 4;


    public static void main(String[] args) {

        boolean[] arr1 = new boolean[ mNumVertex + 1 ];
        boolean[] arr2 = new boolean[ mNumVertex + 1 ];

        for (int i = 0 ; i < arr1.length ; i++) {
            arr1[ i ] = true;
        }

        System.out.println(Arrays.toString(arr1));
        System.out.println(Arrays.toString(arr2));

        boolean[] arr3 = getChildChromosome(arr1, arr2);
        System.out.println(Arrays.toString(arr3));
    }


    private static boolean[] getChildChromosome(boolean[] p1Chromosome, boolean[] p2Chromosome) {
        boolean[] child = new boolean[ mNumVertex  + 1 ];

        Set<Integer> set = new TreeSet<>();
        do {
            set.add(mRandom.nextInt(mNumVertex + 1)); // 0 <= random <= NUM_VERTEX
        } while (set.size() != NUM_CUTTING_POINT);

        Integer[] indices = set.toArray(new Integer[ 0 ]);

        StringBuffer buf = new StringBuffer();
        buf.append("Cutting points: ");
        for (int c : indices) {
            buf.append(c).append(", ");
        }
        buf.setLength(buf.length() - 2);
        System.out.println(buf.toString());

        /*
        Integer[] indices = new Integer[ NUM_CUTTING_POINT ];
        indices[ 0 ] = 3;
        indices[ 1 ] = 5;
        indices[ 2 ] = 8;
        */


        System.arraycopy(p1Chromosome, 1, child, 1, indices[ 0 ]);

        boolean isP1Turn = false;
        for (int i = 1 ; i < NUM_CUTTING_POINT ; i++) {
            if (isP1Turn) {
                System.arraycopy(p1Chromosome, indices[ i - 1 ] + 1, child, indices[ i - 1 ] + 1,
                        indices[ i ] - indices[ i - 1 ]);
            } else {
                System.arraycopy(p2Chromosome, indices[ i - 1 ] + 1, child, indices[ i - 1 ] + 1,
                        indices[ i ] - indices[ i - 1 ]);
            }
            isP1Turn = !isP1Turn;
        }

        if (isP1Turn) {
            System.arraycopy(p1Chromosome, indices[ NUM_CUTTING_POINT  - 1 ] + 1, child,
                    indices[ NUM_CUTTING_POINT  - 1 ] + 1,
                    mNumVertex - indices[ NUM_CUTTING_POINT  - 1 ]);
        } else {
            System.arraycopy(p2Chromosome, indices[ NUM_CUTTING_POINT  - 1 ] + 1, child,
                    indices[ NUM_CUTTING_POINT  - 1 ] + 1,
                    mNumVertex - indices[ NUM_CUTTING_POINT  - 1 ]);
        }

        return child;
    }

}
