# 🦠 BacterI.A.: The Biological Programming Challenge

BacterI.A. is a competitive turn-based strategy game (1 vs 1) for programmers. Two players (Bacteria) start at opposite ends of a Petri dish (square grid). The goal is to optimize cellular expansion, manage energy, explore the map, and consume the rival colony using code.

## 🗺️ 1. The Battlefield and Procedural Generation

The Grid: A square grid (e.g., 64x64). Player 1 spawns in the Top-Left corner and Player 2 in the Bottom-Right corner (or equivalent mirrored positions), starting with exactly 1 living cell.

Perfect Symmetry (1v1): The server randomly generates resources (Nutrients) on one half of the map. It then mirrors this pattern (rotationally or axially) to the other half. Both players face the exact same topological challenge and distances.

### The Nutrients (Spots):

- 🟢 S (Small): 10 Energy
- 🟡 M (Medium): 30 Energy
- 🔴 L (Large): 70 Energy

## ⚡ 2. Economy and Energy

Energy is the game's currency. Everything revolves around preventing your bacteria from "starving to death."

Starting Energy: The game calculates the true distance (in blocks) to the nearest nutrient spot from each player's base. The initial energy is (Distance) + Margin of Error. This ensures it's always mathematically possible to reach the first food source if the algorithm is efficient.

Passive Extraction: If one of your cells is positioned on top of a Nutrient Spot, it automatically extracts 1 Energy per turn, until the spot is depleted.

## 🌫️ 3. Fog of War (Visibility)

Information is the strongest weapon. The gameState object provided to the player each turn is filtered by the server:

In Your Starting Half: You know the exact coordinates of all Nutrient Spots, their remaining sizes, and whether they are depleted or not.

In the Enemy Half: The Spots are completely invisible. You must send "scout cells" to find them.

Biological Radar (3-Block Vision): You can only see the map's occupation (enemy cells) and discover enemy Nutrients within a 3-block radius of any of your living cells.

## ⚔️ 4. Combat Mechanics and Actions

The game is played in Turns. Each turn, your code has a strict time limit (e.g., 50 milliseconds) to return an array with a maximum of 5 Actions.

The 3 Possible Actions:

EXPAND(x, y): Costs 2 Energy. Creates a new cell in an empty space. The coordinate (x, y) must be orthogonally or diagonally adjacent to one of your existing cells.

AUTOPHAGY(x, y): Costs 0 Energy. You sacrifice one of your own cells. It dies and returns 1 Energy to your global pool. Useful to "migrate" your bacteria without leaving a trail, or for emergencies.

ATTACK(x, y): Costs 2 Energy. An attempt to consume an enemy cell adjacent to yours.

Combat Math: The server counts how many allied neighbors you have surrounding the targeted enemy cell, versus how many allied neighbors the enemy has. If you hold the majority (numerical superiority), the attack succeeds: The enemy cell dies, you instantly occupy that space, and you gain +3 Energy stolen from the enemy.

## 🏆 5. Victory Conditions

The 1v1 duel ends when one of the following conditions is met:

Apex Predator (Knockout): Consume or eliminate all of the opponent's cells.

Turn Limit (Decision by Points): Once the turn limit is reached (e.g., Turn 2000), the game stops. The winner is the player with the Largest Occupied Area (total number of living cells) + (Banked Energy / 10). In case of a tie, the player who destroyed the most enemy cells during the match wins.

## 🏟️ 6. Tournament Architecture (The Arena Format)

The platform is structured competitively, similar to modern Matchmaking systems:

League System (Wood, Bronze, Silver, Gold, Legend): * New algorithms enter the "Wood League", where they face basic system-created Bots.

By winning consistently, algorithms are promoted and start facing exclusively the algorithms of other players in the same league (ELO-based matchmaking).

Asynchronous Battles: Players do not need to be online at the same time. You submit your code and the server runs the 1v1 battles in the background against the scripts of your rivals on the ranking, generating replays you can watch later.

Seasonal Tournaments: Every season, a single-elimination bracket format (1v1) determines the Arena's Grand Champion.

## 💻 7. The API Contract (Player Code Example)

This is the exact skeleton the user will need to implement and submit to the Judge (Server).

```nodejs
/**
 * Function executed every turn by the BacterI.A. Engine.
 * * @param {Object} gameState - Current game state (Fog of War applied)
 * @param {number} gameState.turn - Current turn number
 * @param {number} gameState.myEnergy - Your available energy pool
 * @param {Array} gameState.myCells - Array of coordinates [{x, y}] for your cells
 * @param {Array} gameState.visibleEnemies - Enemy cells within your vision radius
 * @param {Array} gameState.visibleSpots - Visible nutrients (coordinate, remaining size)
 * * @returns {Array} - Must return up to 5 actions: { action: "EXPAND"|"AUTOPHAGY"|"ATTACK", x: number, y: number }
 */
function takeAction(gameState) {
    let actions = [];
    
    // BASIC LOGIC EXAMPLE:
    // 1. Find my cell closest to the center
    const vanguardCell = getCellClosestToCenter(gameState.myCells);
    
    // 2. If enemies are in sight, focus on defense/attack
    if (gameState.visibleEnemies.length > 0 && gameState.myEnergy >= 2) {
        const target = getWeakestEnemy(gameState.visibleEnemies);
        actions.push({ action: 'ATTACK', x: target.x, y: target.y });
    } 
    // 3. Otherwise, expand towards the nearest resource
    else if (gameState.myEnergy >= 2) {
        const nextStep = calculatePathToSpot(vanguardCell, gameState.visibleSpots);
        actions.push({ action: 'EXPAND', x: nextStep.x, y: nextStep.y });
    }

    // 4. Cleanup: Kill idle cells that have already harvested everything to recycle energy
    const uselessCells = findIdleCells(gameState.myCells, gameState.visibleSpots);
    for(let cell of uselessCells) {
        if(actions.length < 5) {
            actions.push({ action: 'AUTOPHAGY', x: cell.x, y: cell.y });
        }
    }

    return actions; // Limit of 5 actions will be processed by the server
}
```

