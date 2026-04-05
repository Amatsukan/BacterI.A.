package com.codingame.game;

import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

import java.util.List;

// O Motor do Jogo (O Referee)
public class Referee extends AbstractReferee {
    // Injeção de dependências oficiais do CodinGame SDK
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;

    // Estado global do Jogo BacterI.A.
    private int mapSize = 64;
    // Aqui adicionarias as tuas grelhas (grids) e listas de Entidades (Spots/Células)

    @Override
    public void init() {
        // --- 1. Inicialização do Jogo ---
        // Aqui geramos o mapa simétrico e os spots iniciais
        // Definimos a energia inicial, etc.
        
        for (Player player : gameManager.getPlayers()) {
            player.energy = 20; // Exemplo de energia inicial
        }

        // --- 2. Input de Inicialização (Enviado apenas no Turno 0) ---
        for (Player player : gameManager.getPlayers()) {
            player.sendInputLine(String.valueOf(mapSize));
        }
    }

    @Override
    public void gameTurn(int turn) {
        // ====================================================================
        // FASE 1: ENVIAR DADOS AOS JOGADORES (O que eles veem)
        // ====================================================================
        for (Player player : gameManager.getActivePlayers()) {
            Player opp = gameManager.getPlayer((player.getIndex() + 1) % 2);
            
            // Envia Energia
            player.sendInputLine(player.energy + " " + opp.energy);
            
            // TODO: Filtrar a Névoa de Guerra (Fog of War) aqui
            // Enviar número de entidades visíveis
            int visibleEntitiesCount = 0; 
            player.sendInputLine(String.valueOf(visibleEntitiesCount));
            
            // Loop para enviar cada entidade visível
            // player.sendInputLine("CELL " + x + " " + y + " 1 0");
            
            // Executa o bot deste jogador (dá-lhe o tempo limite)
            player.execute();
        }

        // ====================================================================
        // FASE 2: RECEBER E PROCESSAR AÇÕES
        // ====================================================================
        for (Player player : gameManager.getActivePlayers()) {
            try {
                // Lê o que o jogador fez "console.log" (stdout)
                List<String> outputs = player.getOutputs();
                String actionLine = outputs.get(0);
                
                // Parse das ações (ex: "EXPAND 10 15; ATTACK 11 15")
                String[] actions = actionLine.split(";");
                
                if (actions.length > 5) {
                    throw new Exception("Limite de 5 ações excedido!");
                }

                for (String actionStr : actions) {
                    String[] parts = actionStr.trim().split(" ");
                    String command = parts[0];
                    
                    // Exemplo de Switch para processar comandos
                    switch (command) {
                        case "EXPAND":
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            // TODO: Validar se tem 2 de energia, adjacência e espaço vazio
                            break;
                        case "ATTACK":
                            // TODO: Lógica de superioridade numérica
                            break;
                        case "AUTOPHAGY":
                            // TODO: Lógica de sacrifício
                            break;
                        case "WAIT":
                            break;
                        default:
                            throw new Exception("Comando inválido: " + command);
                    }
                }
            } catch (Exception e) {
                // Se o código do jogador crashar, devolver formato inválido, ou timeout:
                player.deactivate("Erro de Execução ou Timeout: " + e.getMessage());
                player.setScore(-1);
                gameManager.endGame();
            }
        }

        // ====================================================================
        // FASE 3: ATUALIZAR ESTADO E RENDERIZAÇÃO
        // ====================================================================
        // TODO: Atualizar energia passiva, matar células sem energia, etc.
        
        // Exemplo de renderização gráfica (Envia comandos para o viewer TypeScript):
        // graphicEntityModule.commitEntityState(0, ...);

        // ====================================================================
        // FASE 4: VERIFICAR CONDIÇÕES DE VITÓRIA
        // ====================================================================
        boolean p0Alive = true; // Substituir por verificação real de células
        boolean p1Alive = true; 

        if (!p0Alive || !p1Alive) {
            gameManager.endGame();
        }
    }

    @Override
    public void onEnd() {
        // Define as pontuações finais (Quem tem mais energia/células)
        int[] scores = {gameManager.getPlayer(0).energy, gameManager.getPlayer(1).energy};
        for (Player player : gameManager.getPlayers()) {
            player.setScore(scores[player.getIndex()]);
        }
    }
}