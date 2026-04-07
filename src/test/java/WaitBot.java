import java.util.Scanner;

public class WaitBot {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // Init
        int mapSize = in.nextInt();
        int myIndex = in.nextInt();
        int spotCount = in.nextInt();
        for (int i = 0; i < spotCount; i++) {
            in.nextInt(); in.nextInt(); in.nextInt();
        }

        // Game loop
        while (true) {
            int myEnergy = in.nextInt();
            int oppEnergy = in.nextInt();

            int myCellCount = in.nextInt();
            for (int i = 0; i < myCellCount; i++) {
                in.nextInt(); in.nextInt();
            }

            int oppCellCount = in.nextInt();
            for (int i = 0; i < oppCellCount; i++) {
                in.nextInt(); in.nextInt();
            }

            int visSpotCount = in.nextInt();
            for (int i = 0; i < visSpotCount; i++) {
                in.nextInt(); in.nextInt(); in.nextInt(); in.nextInt();
            }

            System.out.println("WAIT");
        }
    }
}
