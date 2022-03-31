//

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class GA1Main {

    private static final long START_TIME = System.currentTimeMillis();
    private static final String INPUT_FILE = "rr.in";
    private static final String OUTPUT_FILE = "rr.out";
    private static final int TIMEOUT = 30000; // 30 sec.

    // hyper parameters
    private static final double SELECTION_EXCEPTION = 0.1; // anyone can be a parent with 10%
    private static final double SELECTION_PRESSURE = 0.1; // if not, upper 10% can be a parent
    private static final int POPULATION_PARAMETER = 100; // this x mChromosomeLength = pop.size

    private static Random mRandom = new Random();
    private static int mN;
    private static String mY;
    private static int mChromosomeLength = 0; // will be mN * 8
    private static int mPopulationSize = 0; // will be chromosome length * 100
    private static int mMaxQuality = 0; // will be 13 * (mN / 2) if mN is even, else,

    private static StringBuffer mBuf = new StringBuffer(); // TODO use this.

    public static void main(String[] args) throws Exception {

        mBuf.append("# GA started: ").append(START_TIME);
        System.out.println(mBuf.toString());

        String[] input = Files.readAllLines(Paths.get("rr.in")).get(0).split(" ");
        mN = Integer.parseInt(input[ 0 ]);
        mY = input[ 1 ];

        mChromosomeLength = 8 * mN;
        mPopulationSize = mChromosomeLength * POPULATION_PARAMETER;
        if ((mN & 1) == 0) {
            mMaxQuality = 13 * (mN / 2);
        } else {
            mMaxQuality = ((13 * mN) + 2) / 2;
        }

        int generation = 1;
        int generationTotalQuality = 0;
        Solution bestSolution = null;
        List<Solution> population = new ArrayList<>(mPopulationSize);

        // 1. Generate initial solutions
        long populationStartTime = System.currentTimeMillis();
        mBuf.setLength(0);
        mBuf.append("# Random population started : ").append(populationStartTime);
        System.out.println(mBuf.toString());

        for (int id = 1 ; id <= mPopulationSize ; id++) {
            String chromosome = getRandomChromosome(mN);
            int quality = getQuality(chromosome);
            Solution curSolution = new Solution(chromosome, generation, id, quality, null, null);
            population.add(curSolution);
            generationTotalQuality += quality;
            if (bestSolution == null || bestSolution.mQuality < quality) {
                bestSolution = curSolution;
            }
//            System.out.println(curSolution);
        }
        Collections.sort(population, new Comparator<Solution>() {
            @Override
            public int compare(Solution s1, Solution s2) {
                return s2.mQuality - s1.mQuality;
            }
        });

        long populationEndTime = System.currentTimeMillis();
        mBuf.setLength(0);
        mBuf.append("# Random population ended : ").append(populationEndTime);
        mBuf.append(" (takes ").append(populationEndTime - populationStartTime).append("ms)");
        mBuf.append("\n").append("# Current average: ").append(
                generationTotalQuality / mPopulationSize);
        mBuf.append("\n").append("# Current best : ").append(bestSolution.toString());
        System.out.println(mBuf.toString());

        // 2. evolution
        long maxLapTime = 0;
        long curGenerationStartTime = 0;
        long curGenerationEndTime = 0;
        do {
            curGenerationStartTime = System.currentTimeMillis();
            generation++;
            generationTotalQuality = 0;
            List<Solution> children = new ArrayList<>(mPopulationSize);
            mBuf.setLength(0);
            mBuf.append("# Generation ").append(generation).append(" started : ").append(
                    curGenerationStartTime);
            System.out.println(mBuf.toString());

            for (int id = 1 ; id <= mPopulationSize ; id++) {
                // 2-1 select two parent
                int parentOneIndex = mRandom.nextInt(mPopulationSize);
                if (mRandom.nextDouble() > SELECTION_EXCEPTION) {
                    parentOneIndex = mRandom.nextInt((int) (mPopulationSize * SELECTION_PRESSURE));
                }

                // Note. must parent1 != parent2
                int parentTwoIndex = mRandom.nextInt(mPopulationSize);
                while (parentTwoIndex == parentOneIndex) {
                    parentTwoIndex = mRandom.nextInt(mPopulationSize);
                }

                if (mRandom.nextDouble() > SELECTION_EXCEPTION) {
                    while (parentTwoIndex == parentOneIndex) {
                        parentTwoIndex = mRandom.nextInt(
                                (int) (mPopulationSize * SELECTION_PRESSURE));
                    }
                }

                // 2-2. crossover - 3pt-crossover
                int[] cutPoints = new int[ 3 ];
                cutPoints[0] = mRandom.nextInt(mChromosomeLength);
                cutPoints[1] = mRandom.nextInt(mChromosomeLength);
                while (cutPoints[ 1 ] == cutPoints[0]) {
                    cutPoints[1] = mRandom.nextInt(mChromosomeLength);
                }
                cutPoints[2] = mRandom.nextInt(mChromosomeLength);
                while (cutPoints[ 2 ] == cutPoints[ 0 ] || cutPoints[ 2 ] == cutPoints[ 1 ]) {
                    cutPoints[ 2 ] = mRandom.nextInt(mChromosomeLength);
                }

                Arrays.sort(cutPoints);
                mBuf.setLength(0);
                mBuf.append(population.get(parentOneIndex).mChromosome.subSequence(
                        0, cutPoints[ 0 ]));
                mBuf.append(population.get(parentTwoIndex).mChromosome.subSequence(
                        cutPoints[ 0 ], cutPoints[ 1 ]));
                mBuf.append(population.get(parentOneIndex).mChromosome.subSequence(
                        cutPoints[ 1 ], cutPoints[ 2 ]));
                mBuf.append(population.get(parentTwoIndex).mChromosome.subSequence(
                        cutPoints[ 2 ], mChromosomeLength));

                String childChromosome = mBuf.toString();
                Solution childSolution = new Solution(childChromosome, generation, id,
                        getQuality(childChromosome), population.get(parentOneIndex),
                        population.get(parentTwoIndex));

//                System.out.println(childSolution);

                if (bestSolution.mQuality < childSolution.mQuality) {
                    bestSolution = childSolution;
                }

                generationTotalQuality += childSolution.mQuality;
                children.add(childSolution);
            } // end

            // 2-3. replacement
            population = children;
            Collections.sort(population, new Comparator<Solution>() {
                @Override
                public int compare(Solution s1, Solution s2) {
                    return s2.mQuality - s1.mQuality;
                }
            });
            curGenerationEndTime = System.currentTimeMillis();
            long curLaptime = curGenerationEndTime - curGenerationStartTime;

            mBuf.setLength(0);
            mBuf.append("# Generation ").append(generation).append(" ended : ").append(
                    curGenerationEndTime).append(" (takes ").append(curLaptime).append("ms)");
            mBuf.append("\n").append("# Current quality average: ").append(
                    generationTotalQuality / mPopulationSize);
            mBuf.append("\n").append("# Current best : ").append(bestSolution.toString());
            System.out.println(mBuf.toString());

            maxLapTime = Math.max(maxLapTime, curLaptime);
        } while (TIMEOUT > System.currentTimeMillis() - START_TIME + (2 * maxLapTime));

        // report
        mBuf.setLength(0);
        mBuf.append(bestSolution.mQuality).append(" ").append(bestSolution.mChromosome);

        Files.write(Paths.get(OUTPUT_FILE), mBuf.toString().getBytes());

        mBuf.setLength(0);
        mBuf.append("# Done. (takes ").append(System.currentTimeMillis() - START_TIME)
                .append(" ms, maxLapTime ").append(maxLapTime).append(" ms, ")
                .append(bestSolution.mQuality).append("/").append(mMaxQuality).append(")");
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
