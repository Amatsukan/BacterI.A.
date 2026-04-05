import java.util.Scanner;

/**
 * Bot mínimo de teste: lê o protocolo do Referee e responde sempre WAIT.
 * Usado pelo Main.java para simular partidas locais.
 *
 * Protocolo de input (definido em Referee.java):
 *   Init   : <mapSize>
 *   Turno  : <myEnergy> <oppEnergy>
 *              <visibleEntitiesCount>
 *              [<entidade> ...] (uma linha por entidade)
 * Output   : 1 linha — "WAIT" (ou acções separadas por ";")
 */
public class WaitBot {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        int mapSize = Integer.parseInt(in.nextLine().trim());
        System.err.println("[WaitBot] mapSize=" + mapSize);

        while (in.hasNextLine()) {
            String[] energyLine = in.nextLine().trim().split(" ");
            int myEnergy  = Integer.parseInt(energyLine[0]);
            int oppEnergy = Integer.parseInt(energyLine[1]);

            int visible = Integer.parseInt(in.nextLine().trim());
            for (int i = 0; i < visible; i++) {
                in.nextLine();
            }

            System.err.println("[WaitBot] turn myE=" + myEnergy + " oppE=" + oppEnergy);
            System.out.println("WAIT");
            System.out.flush();
        }
    }
}
