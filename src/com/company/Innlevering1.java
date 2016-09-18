package com.company;


/**
 * Created by root on 9/17/16.
 */

public class Innlevering1 {

    private static final Dedup[] dedups =
            new Dedup[]{
                    Dedup.newBasicDedup(),
                    Dedup.newHashSetDedup()
                    };

    private static Utils.Sampler[] samplers;
    private static Utils.Stopwatch timer;

        /**
         * 0?) start the program by running a dummy version of each algorithm just to
         *      get the OS ready for the first records.
         * 1) run each algorithm an arbitrary 10 amounts of times for each sample size
         * 2) save the average runtime for each of the sample size, for each algorithm
         * 3) output each row as follow to a file in csv format:
         *         samplesize, medianTime, v1, v2, v3,
         *         where:
         *              v1 = medianTime/ sampleSize >> looks like purely empirical analysis?
         *              v2 = medianTime / sampleSize * resultSize >> looks like worst case?
         *              v3 = medianTime / (log(sampleSize) * resultSize) >> looks like best case?
         *
         * 4) make a graph for word inputs, as well as numbers >> sorted, sorted in reverse, unsorted.
         *
         */

        public static void main(String[] args) {

            if(args.length < 1){
                System.out.println("\nPlease run the program with at least one input file.");
                System.out.println("For example: java Innlevering1 shakespeare.txt tall100.txt");
                System.exit(0);
            }

            samplers = new Utils.Sampler[args.length];

            for (int i = 0; i < args.length; i++) {
                samplers[i] = new Utils.Sampler(args[i]);
            }

            for(Utils.Sampler sample : samplers){
                runAllAlgorithms(sample);
            }

            //dummy runs

        }



        private static void runAllAlgorithms(Utils.Sampler sample) {
            //for (Utils.Sampler sample : samplers) {
                for (Dedup dedup: dedups) {

                    // Output to 'Algorithm - Classname.csv'
                    String outputFilename =
                            String.format(
                                    "%s - %s.csv",
                                    dedup.getClass().getSimpleName().toLowerCase(),
                                    sample.getFilename().split(".")[0]);

                    OutputExt out = new OutputExt(outputFilename, ",");


                    // Set up range of experiments
                    int lo = 1;
                    int hi = 10000;
                    int numberOfLevels = 100;
                    int runsPerSampleSize = 10;


                    // Calculation of step size
                    int step = (hi - lo) < numberOfLevels ? 1 : (hi - lo) / (numberOfLevels - 1);

                    // Main loop
                    for (int size = lo; size <= hi; size += step) {
                        String[] uniques = sample.get(size);

                        long avgTime = 0;   // to store a median time for each sample size
                        String[] withoutDupes= dedup.dedup(uniques);

                        for(int i = 0; i < runsPerSampleSize; i++){

                            // Copying the original sample to keep it from being affected
                            String[] testList = uniques;

                            /* Do test while measuring the time */
                            Utils.Stopwatch timer = new Utils.Stopwatch();
                            withoutDupes = dedup.dedup(testList);
                            long elapsedTime = timer.elapsedTime();
                            avgTime += elapsedTime;
                        }

                        avgTime /= runsPerSampleSize;
                        int uniquesSize = withoutDupes.length;


                        /* Write measurement to output */
                        out.addMeasurement(
                                size,
                                avgTime,
                                v1(avgTime, size),
                                v2(avgTime, size, uniquesSize),
                                v3(avgTime, size, uniquesSize));


                    }
                }
            }
      //  }

        private static long v1(long time, int problemSize){
            return (long) (time / problemSize);
        }

        private static long v2(long time, int problemSize, int resultSize){
            return (long) (time / problemSize * resultSize);
        }

        private static long v3(long time, int problemSize, int resultSize){
            return (long) (time / Math.log(resultSize) * problemSize);
        }

    /**
     * Customised version of Utils.Output to print out the additional data
     * per sample size
     */

    private static class OutputExt extends Utils.Output{

        public OutputExt(String filepath, String separator){
            super(
                    filepath,
                    separator,
                    String.join(separator,"%d","%d","%d","%d","%d%n"));
        }

        public void addMeasurement(int size, long nanoseconds, long v1, long v2, long v3){
            out.printf(measurementFormat, size, nanoseconds, v1, v2, v3);
        }
    }


}