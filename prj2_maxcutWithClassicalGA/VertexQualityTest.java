import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class VertexQualityTest {

    private static final String INPUT_FILE = "weighted_chimera_297.txt";

    private static int mNumVertex;  // similar with chromosome length in GA prj1
    private static int mNumEdge;
    private static int[][] mGraph = null;

    public static void main(String[] args) throws Exception {

        List<String> data = Files.readAllLines(Paths.get(INPUT_FILE));
        String[] numVertexEdge = data.get(0).split(" ");
        mNumVertex = Integer.parseInt(numVertexEdge[ 0 ]); // chromosome length
        mNumEdge = Integer.parseInt(numVertexEdge[ 1 ]);

        // 0. parse the graph
        mGraph = new int[ mNumVertex  + 1 ][ mNumVertex + 1 ]; // node # starts with 1
        for (int i = 1 ; i < data.size() ; i++) {
            String[] line = data.get(i).split(" ");
            mGraph[ Integer.parseInt(line[ 0 ]) ][ Integer.parseInt(line[ 1 ]) ]
                    = Integer.parseInt(line[ 2 ]);
            mGraph[ Integer.parseInt(line[ 1 ]) ][ Integer.parseInt(line[ 0 ]) ]
                    = Integer.parseInt(line[ 2 ]);
        }

        for (int i = 1 ; i <= mNumVertex ; i++) {
            boolean[] testChromosome = new boolean[ mNumVertex + 1 ];
            testChromosome[ i ] = true;
//            System.out.println("Node: " + i + ", quality: " + getQuality(testChromosome));
            System.out.println(getQuality(testChromosome));
        }
    }

    private static int getQuality(boolean[] chromosome) {
        int sum = 0;
        for (int i = 1 ; i <= mNumVertex ; i++) {
            if (chromosome[ i ]) {
                for (int j = 1 ; j <= mNumVertex ; j++) {
                    /*
                    if (i == j && chromosome[ i ][ j ] == 0) {
                        continue;
                    }
                    */
                    if (!chromosome[ j ]) {
                        sum += mGraph[ i ][ j ];
                    }
                }
            }
        }

        return sum;
    }

}
