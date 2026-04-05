import java.util.Scanner;

/**
 * Bot minimo de teste: le o protocolo do Referee e responde WAIT.
 */
public class WaitBot {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // Init
        String[] initLine = in.nextLine().trim().split(" ");
        int mapSize = Integer.parseInt(initLine[0]);
        int myIndex = Integer.parseInt(initLine[1]);
        int spotCount = Integer.parseInt(in.nextLine().trim());
        for (int i = 0; i < spotCount; i++) in.nextLine();

        System.err.println("[WaitBot] mapSize=" + mapSize + " myIndex=" + myIndex + " spots=" + spotCount);

        // Game loop
        while (in.hasNextLine()) {
            String eLine = in.nextLine().trim();
            if (eLine.isEmpty()) continue;
            String[] eParts = eLine.split(" ");
            int myEnergy = Integer.parseInt(eParts[0]);
            int oppEnergy = Integer.parseInt(eParts[1]);

            int entityCount = Integer.parseInt(in.nextLine().trim());
            for (int i = 0; i < entityCount; i++) in.nextLine();

            System.err.println("[WaitBot] energy=" + myEnergy + " entities=" + entityCount);
            System.out.println("WAIT");
            System.out.flush();
        }
    }
}
