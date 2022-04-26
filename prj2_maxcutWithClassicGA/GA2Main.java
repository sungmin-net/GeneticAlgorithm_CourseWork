import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class GA2Main {

    private static final long START_TIME = System.currentTimeMillis();
    private static final String INPUT_FILE = "rr.in";
    private static final String OUTPUT_FILE = "rr.out";
    private static final int TIMEOUT = 180000; // 180 sec.

    // Parameters
    private static final double SELECTION_EXCEPTION = 0.1; // anyone can be a parent with 10%
    private static final double SELECTION_PRESSURE = 0.1; // if not, upper 10% can be a parent
    private static final int POPULATION_PARAMETER = 500; // this x mChromosomeLength = pop. size
    private static final int NUM_CUTTING_POINT = 4;

    private static Random mRandom = new Random();
    private static StringBuffer mBuf = new StringBuffer();
    private static int mNumVertex;  // similar with chromosome length in GA prj1
    private static int mNumEdge;
    private static int[][] mGraph = null;
    private static Comparator<Solution> mSorter = new Comparator<Solution>() {
        @Override
        public int compare(Solution s1, Solution s2) {
            return s2.mQuality - s1.mQuality;
        }
    };

    public static void main(String[] args) throws Exception {

        mBuf.append("# GA started: ").append(START_TIME);
        System.out.println(mBuf.toString());

        List<String> data = Files.readAllLines(Paths.get(INPUT_FILE));
        String[] numVertexEdge = data.get(0).split(" ");
        mNumVertex = Integer.parseInt(numVertexEdge[ 0 ]); // chromosome length
        mNumEdge = Integer.parseInt(numVertexEdge[ 1 ]);

        // 0. parse the graph
        mGraph = new int[ mNumVertex  + 1 ][ mNumVertex  + 1 ]; // node # starts with 1
        for (int i = 1 ; i < data.size() ; i++) {
            String[] line = data.get(i).split(" ");
            mGraph[ Integer.parseInt(line[ 0 ]) ][ Integer.parseInt(line[ 1 ]) ]
                    = Integer.parseInt(line[ 2 ]);
            mGraph[ Integer.parseInt(line[ 1 ]) ][ Integer.parseInt(line[ 0 ]) ]
                    = Integer.parseInt(line[ 2 ]); // need?
        }

        // 1. random population
        long populationStartTime = System.currentTimeMillis();
        int numPopulation = mNumVertex * POPULATION_PARAMETER;
        mBuf.setLength(0);
        mBuf.append("# Random population started: ").append(populationStartTime)
                .append(", size: ").append(numPopulation);
        System.out.println(mBuf.toString());

        int generation = 1;
        int genTotalQuality = 0;
        Solution bestSolution = null;
        List<Solution> population = new ArrayList<>(numPopulation);

        for (int id = 1 ; id <= numPopulation ; id++) {
            boolean[] chromosome = new boolean[ mNumVertex + 1 ];
            for (int i = 1 ; i <= mNumVertex ; i++) {
                chromosome[ i ] = mRandom.nextBoolean();
            }

            Solution curSolution = new Solution(chromosome, generation, id, null, null);
            population.add(curSolution);
            genTotalQuality += curSolution.mQuality;
            if (bestSolution == null || bestSolution.mQuality < curSolution.mQuality) {
                bestSolution = curSolution;
            }
        }

        Collections.sort(population, mSorter);
        long populationEndTime = System.currentTimeMillis();
        mBuf.setLength(0);
        mBuf.append("# Random population ended : ").append(populationEndTime);
        mBuf.append(" (takes ").append(populationEndTime - populationStartTime).append("ms)");
        mBuf.append("\n").append("# Current average: ").append(
                (double) genTotalQuality / numPopulation);
        mBuf.append("\n").append("# Current best : ").append(bestSolution.toString());
        System.out.println(mBuf.toString());

        // 2. evolution
        long maxGenTime = 0;
        long curGenStartTime = 0;
        long curGenEndTime = 0;
        long curGenTime = 0;
        do {
            curGenStartTime = System.currentTimeMillis();
            generation++;
            genTotalQuality = 0;
            List<Solution> children = new ArrayList<>(numPopulation);
            mBuf.setLength(0);
            mBuf.append("# Generation ").append(generation).append(" started: ")
                    .append(curGenStartTime);
            System.out.println(mBuf.toString());

            for (int id = 1 ; id <= numPopulation ; id++) {
                // 2-1. Select two parents
                int p1Index = mRandom.nextInt(numPopulation);
                if (mRandom.nextDouble() > SELECTION_EXCEPTION) {
                    p1Index = mRandom.nextInt((int) (numPopulation * SELECTION_PRESSURE));
                }

                int p2Index = mRandom.nextInt(numPopulation);
                while (p1Index == p2Index) {
                    p2Index = mRandom.nextInt(numPopulation);
                }

                if (mRandom.nextDouble() > SELECTION_EXCEPTION) {
                    while (p2Index == p1Index) {
                        p2Index = mRandom.nextInt((int) (numPopulation * SELECTION_PRESSURE));
                    }
                }

                // 2-2. crossover
                boolean[] childChromosome = getChildChromosome(population.get(p1Index).mChromosome,
                        population.get(p2Index).mChromosome);
                Solution childSolution = new Solution(childChromosome, generation, id,
                        population.get(p1Index), population.get(p2Index));

                if (bestSolution.mQuality < childSolution.mQuality) {
                    bestSolution = childSolution;
                }

                genTotalQuality += childSolution.mQuality;
                children.add(bestSolution);
            }

            // 2-3. replacement
            population = children;
            Collections.sort(population, mSorter);
            curGenEndTime = System.currentTimeMillis();
            curGenTime = curGenEndTime - curGenStartTime;

            mBuf.setLength(0);
            mBuf.append("# Generation ").append(generation).append(" ended: ").append(
                    curGenEndTime).append(" (takes ").append(curGenTime).append("ms)");
            mBuf.append("\n").append("# Current average: ").append(
                    (double) genTotalQuality / numPopulation);
            mBuf.append("\n").append("# Current best : ").append(bestSolution.mQuality);
            System.out.println(mBuf.toString());
            maxGenTime = Math.max(maxGenTime, curGenTime);

        } while (TIMEOUT + START_TIME > System.currentTimeMillis() + (2 * maxGenTime));

        // report
        mBuf.setLength(0);
        mBuf.append(getChromosomeStr(bestSolution.mChromosome));
        Files.write(Paths.get(OUTPUT_FILE), mBuf.toString().getBytes());

        mBuf.setLength(0);
        mBuf.append("# Done. (takes ").append(System.currentTimeMillis() - START_TIME)
        .append(" ms, maxLapTime ").append(maxGenTime).append(" ms.").append("\n")
        .append(bestSolution.mQuality).append("/").append(getMaxQuality()).append(")");

    } // end of Main()

    private static int getMaxQuality() {
        int ret = -1;

        if (mNumVertex == 50 && mNumEdge == 123) {
            return 99; // case of unweighted_50.txt
        }

        if (mNumVertex == 100 && mNumEdge == 495) {
            return 358; // case of unweighted_100.txt
        }

        if (mNumVertex == 500) {
            if (mNumEdge == 4990) {
                return 3314; // case of unweighted_500.txt
            }
            if (mNumEdge == 5000) {
                return 4743; // case of weighted_500.txt
            }
        }

        if (mNumVertex == 297 && mNumEdge == 1007) {
            return 9340; // case of weighted_chimera_297.txt
        }

        return ret;
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

    private static String getChromosomeStr(boolean[] chromosome) {
        mBuf.setLength(0);
        for (int i = 1 ; i <= mNumVertex ; i++) {
            if (chromosome[ i ]) {
                mBuf.append(i).append(" ");
            }
        }
        return mBuf.substring(0, mBuf.length() - 1).toString();
    }

    private static boolean[] getChildChromosome(boolean[] p1Chromosome, boolean[] p2Chromosome) {
        boolean[] child = new boolean[ mNumVertex  + 1 ];

        Set<Integer> set = new TreeSet<>();
        do {
            set.add(mRandom.nextInt(mNumVertex + 1)); // 0 <= random <= NUM_VERTEX
        } while (set.size() != NUM_CUTTING_POINT);

        Integer[] indices = set.toArray(new Integer[ 0 ]);

        /*
        StringBuffer buf = new StringBuffer();
        buf.append("Cutting points: ");
        for (int c : indices) {
            buf.append(c).append(", ");
        }
        buf.setLength(buf.length() - 2);
        System.out.println(buf.toString());
        */

        System.arraycopy(p1Chromosome, 1, child, 1, indices[ 0 ]);
        boolean isP1Turn = false;
        for (int i = 1 ; i < NUM_CUTTING_POINT ; i++) {
            if (isP1Turn) {
                System.arraycopy(p1Chromosome, indices[ i - 1 ], child, indices[ i - 1 ],
                        indices[ i ] - indices[ i - 1 ]);
            } else {
                System.arraycopy(p2Chromosome, indices[ i - 1 ], child, indices[ i - 1 ],
                        indices[ i ] - indices[ i - 1 ]);
            }
            isP1Turn = !isP1Turn;
        }
        if (isP1Turn) {
            System.arraycopy(p1Chromosome, indices[ NUM_CUTTING_POINT  - 1 ], child,
                    indices[ NUM_CUTTING_POINT  - 1 ],
                    mNumVertex - indices[ NUM_CUTTING_POINT  - 1 ]);
        } else {
            System.arraycopy(p2Chromosome, indices[ NUM_CUTTING_POINT  - 1 ], child,
                    indices[ NUM_CUTTING_POINT  - 1 ],
                    mNumVertex - indices[ NUM_CUTTING_POINT  - 1 ]);
        }
        return child;
    }

    private static class Solution {
        int mGen;
        int mId;
        int mQuality;
        boolean[] mChromosome;

        Solution mParent1;
        Solution mParent2;

        public Solution(boolean[] chromosome, int gen, int id, Solution p1, Solution p2) {
            mChromosome = chromosome;
            mGen = gen;
            mId = id;
            mParent1 = p1;
            mParent2 = p2;
            mQuality = getQuality(chromosome);
        }

        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("Solution ").append(mGen).append("-").append(mId).append("\n");
            buf.append("\t").append("- Quality: ").append(mQuality).append("\n");
            buf.append("\t").append("- Chromosome: ").append(getChromosomeStr(mChromosome));
            if (mParent1 != null) {
                buf.append("\n");
                buf.append("\t").append("- Parent1: ").append(mParent1.mGen).append("-")
                        .append(mParent1.mId).append("\n");
                buf.append("\t\t").append("- Quality: ").append(mParent1.mQuality).append("\n");
                buf.append("\t\t").append("- Chromosome: ").append(getChromosomeStr(
                        mParent1.mChromosome));
            }
            if (mParent2 != null) {
                buf.append("\n");
                buf.append("\t").append("- Parent2: ").append(mParent2.mGen).append("-")
                        .append(mParent2.mId).append("\n");
                buf.append("\t\t").append("- Quality: ").append(mParent2.mQuality).append("\n");
                buf.append("\t\t").append("- Chromosome: ").append(getChromosomeStr(
                        mParent2.mChromosome));
            }
            return buf.toString();
        }
    }
}
