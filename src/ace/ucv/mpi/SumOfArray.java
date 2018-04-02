package ace.ucv.mpi;

import mpi.*;

public class SumOfArray {
    private static final int master = 0;

    public static void main(String[] args) throws MPIException {
        int arraySize = 8;

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (arraySize % size != 0) {
            System.out.println("Try again");
            System.exit(1);
        }

        int chunk = arraySize / size;

        int[] sendMasterArray = new int[chunk];
        int[] sendSlaveArray = new int[1];

        if (rank == master) {
            int[] array = new int[arraySize];

            for (int i = 0; i < arraySize; i++) {
                array[i] = i * 2;
            }

            int offset = chunk;
            int mySum = calculateSum(chunk, array);
            System.out.println("Partial sum is: " + mySum);

            for (int dest = 1; dest < size; dest++) {
                int j = 0;
                for (int i = offset; i < offset + chunk; i++) {
                    sendMasterArray[j] = array[i];
                    j++;
                }
                MPI.COMM_WORLD.Send(sendMasterArray, 0, chunk, MPI.INT, dest, 0);
                offset = offset + chunk;
            }

        }

        if (rank > master) {
            MPI.COMM_WORLD.Recv(sendMasterArray, 0, chunk, MPI.INT, master, 0);
            int mySum = calculateSum(chunk, sendMasterArray);
            sendSlaveArray[0] = mySum;
            MPI.COMM_WORLD.Send(sendSlaveArray, 0, 1, MPI.INT, master, 0);
        }
        if (rank == master) {
            int totalSum = 0;
            for (int dest = 1; dest < size; dest++) {
                MPI.COMM_WORLD.Recv(sendSlaveArray, 0, 1, MPI.INT, dest, 0);
                totalSum += sendSlaveArray[0];
            }
            System.out.println("Total sum: " + totalSum);
        }

        MPI.Finalize();
    }

    private static int calculateSum(int chunk, int array[]) {
        int sum = 0;
        for (int i = 0; i < chunk; i++) {
            sum += array[i];
        }

        return sum;
    }
}
