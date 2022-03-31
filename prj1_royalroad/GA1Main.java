
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GA1Main {

    private static final long START_TIME = System.currentTimeMillis();
    private static final String INPUT_FILE = "rr.in";
    private static final String OUTPUT_FILE = "rr.out";
    private static final int TIMEOUT = 30000; // 30 sec.

    // hyper parameters
    private static int POPULATION_SIZE = 0;
    private static final double LEAGUE_SELECTION_ROUND = 5;
    private static Random mRandom = new Random();
    private static int mN;
    private static String mY;

    private static StringBuffer mBuf = new StringBuffer(); // TODO use this.


    public static void main(String[] args) throws Exception {

        mBuf.append("# GA started: ").append(START_TIME);
        System.out.println(mBuf.toString());

        String[] input = Files.readAllLines(Paths.get("rr.in")).get(0).split(" ");
        mN = Integer.parseInt(input[ 0 ]);
        mY = input[ 1 ];

        POPULATION_SIZE = 8 * mN * 50;

        int generation = 1;
        int totalQuality = 0;
        Solution bestSolution = null;
        List<Solution> solutions = new ArrayList<>(POPULATION_SIZE);

        // 1. Generate initial solutions
        long populationStartTime = System.currentTimeMillis();
        mBuf.setLength(0);
        mBuf.append("# Random population started : ").append(populationStartTime);
        System.out.println(mBuf.toString());

        for (int id = 1 ; id <= POPULATION_SIZE ; id++) {
            String chromosome = getRandomChromosome(mN);
            int quality = getQuality(chromosome);
            Solution curSolution = new Solution(chromosome, generation, id, quality, null, null);
            solutions.add(curSolution);
            totalQuality += quality;
            if (bestSolution == null || bestSolution.mQuality < quality) {
                bestSolution = curSolution;
            }
            System.out.println(curSolution);
        }

        long populationEndTime = System.currentTimeMillis();

        mBuf.setLength(0);
        mBuf.append("# Random population ended : ").append(populationEndTime);
        mBuf.append(" (takes ").append(populationEndTime - populationStartTime).append("ms)");
        mBuf.append("\n").append("# Current best : ").append(bestSolution.toString());
        mBuf.append("\n").append("# Current quality average: ").append(
                (double) totalQuality / POPULATION_SIZE);
        System.out.println(mBuf.toString());

        // 2. evolution
        long maxLapTime = 0;
        long curGenerationStartTime = 0;
        long curGenerationEndTime = 0;
        do {
            curGenerationStartTime = System.currentTimeMillis();
            generation++;
            totalQuality = 0;
            List<Solution> children = new ArrayList<>(POPULATION_SIZE);
            mBuf.setLength(0);
            mBuf.append("# Generation ").append(generation).append(" started : ").append(
                    curGenerationStartTime);
            System.out.println(mBuf.toString());

            for (int id = 1 ; id <= POPULATION_SIZE ; id++) {
                // 2-1 select two parent
                int parentOneIndex = mRandom.nextInt(POPULATION_SIZE);
                for (int i = 0 ; i < LEAGUE_SELECTION_ROUND ; i++) {
                    int challengerIdx = mRandom.nextInt(POPULATION_SIZE);
                    while (challengerIdx == parentOneIndex) {
                        challengerIdx = mRandom.nextInt(POPULATION_SIZE);
                    }

                    if (solutions.get(challengerIdx).mQuality
                            > solutions.get(parentOneIndex).mQuality) {
                        int temp = parentOneIndex;  // for swap
                        parentOneIndex = challengerIdx;
                        challengerIdx = temp;
                    }
                }

                // Note. must parent1 != parent2
                int parentTwoIndex = mRandom.nextInt(POPULATION_SIZE);
                while (parentTwoIndex == parentOneIndex) {
                    parentTwoIndex = mRandom.nextInt(POPULATION_SIZE);
                }

                for (int i = 0 ; i < LEAGUE_SELECTION_ROUND ; i++) {
                    int challengerIdx = mRandom.nextInt(POPULATION_SIZE);
                    while (challengerIdx == parentTwoIndex || challengerIdx == parentOneIndex) {
                        challengerIdx = mRandom.nextInt(POPULATION_SIZE);
                    }

                    if (solutions.get(challengerIdx).mQuality
                            > solutions.get(parentTwoIndex).mQuality) {
                        int temp = parentTwoIndex;
                        parentTwoIndex = challengerIdx;
                        challengerIdx = temp;
                    }
                }

                // 2-2. crossover
                // first, make parent1 is better than parent2
                if (solutions.get(parentTwoIndex).mQuality
                        > solutions.get(parentOneIndex).mQuality) {
                    int temp = parentTwoIndex;
                    parentTwoIndex = parentOneIndex;
                    parentOneIndex = temp;
                }

                // then, crossover them like the below.
                //  |---(p1-1)---|---(p1-2)---|---(p1-3)---|---(p1-4)---|---(p1-5)---|...
                //  |---(p2-1)---|---(p2-2)---|---(p2-3)---|---(p2-4)---|---(p2-5)---|...
                //  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓...
                //  |---(p1-1)---|---(p2-2)---|---(p1-3)---|---(p2-4)---|---(p1-5)---|...

                String p1chromosome = solutions.get(parentOneIndex).mChromosome;
                String p2chromosome = solutions.get(parentTwoIndex).mChromosome;
                boolean isP1turn = true;

                StringBuffer buf = new StringBuffer();
                for (int i = 0 ; i < mN ; i++) {
                    if (isP1turn) {
                        buf.append(p1chromosome.substring(8 * i, 8 * (i + 1)));
                    } else {
                        buf.append(p2chromosome.substring(8 * i, 8 * (i + 1)));
                    }
                    isP1turn = !isP1turn;
                }

                String childChromosome = buf.toString();
                Solution childSolution = new Solution(childChromosome, generation, id,
                        getQuality(childChromosome), solutions.get(parentOneIndex),
                        solutions.get(parentTwoIndex));

                System.out.println(childSolution);

                if (bestSolution.mQuality < childSolution.mQuality) {
                    bestSolution = childSolution;
                }

                // remove the worst in the league selection
                totalQuality += childSolution.mQuality;
                children.add(childSolution);

            } // end

            // 2-3. replacement
            solutions = children;
            curGenerationEndTime = System.currentTimeMillis();
            long curLaptime = curGenerationEndTime - curGenerationStartTime;

            mBuf.setLength(0);
            mBuf.append("# Generation ").append(generation).append(" ended : ").append(
                    curGenerationEndTime).append(" (takes ").append(curLaptime).append("ms)");
            mBuf.append("\n").append("# Current best : ").append(bestSolution.toString());
            mBuf.append("\n").append("# Current quality average: ").append(
                    (double) totalQuality / POPULATION_SIZE);
            System.out.println(mBuf.toString());

            maxLapTime = Math.max(maxLapTime, curLaptime);
        } while (TIMEOUT > System.currentTimeMillis() - START_TIME + (2 * maxLapTime));

        // report
        mBuf.setLength(0);
        mBuf.append(bestSolution.mQuality).append(" ").append(bestSolution.mChromosome);

        Files.write(Paths.get(OUTPUT_FILE), mBuf.toString().getBytes());

        mBuf.setLength(0);
        mBuf.append("# Done. (takes ").append(System.currentTimeMillis() - START_TIME)
                .append(" ms, maxLapTime ").append(maxLapTime).append(")");
        System.out.println(mBuf.toString());
    }


    private static String getRandomChromosome(int n) {
        char[] buf = new char[ 8 * n ];
        for (int i = 0 ; i < 8 * n ; i++) {
            if (mRandom.nextBoolean()) {
                buf[ i ] = '0';
            } else {
                buf[ i ] = '1';
            }
        }
        return String.valueOf(buf);
    }

    private static int getQuality(String chromosome) {
        int sum = 0;
        for (int i = 0 ; i < mN ; i++) {
            String s1 = mY.substring(8 * i, 8 * (i + 1));
            String s2 = chromosome.substring(8 * i, 8 * (i + 1));
            if (s1.equals(s2)) {
                // Note. index in problem starts from 1, but the code's starts from 0.
                //       So, apply 8-5-8-5 sequence to make 8 if n == 1.
                if ((i & 1) == 0) {
                    sum += 8;
                } else {
                    sum += 5;
                }
            }
        }
        return sum;
    }

    static class Solution {
        int mGeneration;
        int mId;
        int mQuality;
        String mChromosome;

        Solution mParent1;
        Solution mParent2;

        public Solution(String chromosome, int gen, int id, int quality, Solution p1, Solution p2) {
            mChromosome = chromosome;
            mGeneration = gen;
            mId = id;
            mParent1 = p1;
            mParent2 = p2;
            mQuality = quality;
        }

        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("# Solution ").append(mGeneration).append("-").append(mId).append("\n");
            buf.append("\t").append("- Quality: ").append(mQuality).append("\n");
            buf.append("\t").append("- Chromosome: ").append(mChromosome);
            if (mParent1 != null) {
                buf.append("\n");
                buf.append("\t").append("- Parent1: ").append(mParent1.mGeneration).append("-")
                        .append(mParent1.mId).append("\n");
                buf.append("\t\t").append("- Quality: ").append(mParent1.mQuality).append("\n");
                buf.append("\t\t").append("- Chromosome: ").append(mParent1.mChromosome);
            }
            if (mParent2 != null) {
                buf.append("\n");
                buf.append("\t").append("- Parent2: ").append(mParent2.mGeneration).append("-")
                        .append(mParent2.mId).append("\n");
                buf.append("\t\t").append("- Quality: ").append(mParent2.mQuality).append("\n");
                buf.append("\t\t").append("- Chromosome: ").append(mParent2.mChromosome);
            }

            return buf.toString();
        }
    }
}
