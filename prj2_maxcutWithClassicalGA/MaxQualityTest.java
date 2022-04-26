import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MaxQualityTest {

    private static final String INPUT_FILE = "weighted_chimera_297.txt";
    private static final String BEST_FILE = "sol_weighted_chimera_297.txt";

    private static int mNumVertex;  // similar with chromosome length in GA prj1
    private static int mNumEdge;
    private static int[][] mGraph;

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
                    = Integer.parseInt(line[ 2 ]); // need?
        }

        // 1. read best chromosome
        boolean[] bestChromosome = new boolean[ mNumVertex + 1 ];
        String[] bestChromosomeData = Files.readAllLines(Paths.get(BEST_FILE)).get(0).split(" ");
        for (String s : bestChromosomeData) {
            bestChromosome[ Integer.parseInt(s) ] = true;
        }

        System.out.println("numVertext: " + mNumVertex + ", numEdge: " + mNumEdge + ", quality: "
                    + getQuality(bestChromosome));

    }

    private static int getQuality(boolean[] chromosome) {
        int sum = 0;
        for (int i = 1 ; i <= mNumVertex ; i++) {
            if (chromosome[ i ]) {
                for (int j = 1 ; j <= mNumVertex ; j++) {
                    if (i == j) {
                        continue;
                    }
                    if (!chromosome[ j ]) {
                        sum += mGraph[ i ][ j ];
                    }
                }
            }
        }

        return sum;
    }
}