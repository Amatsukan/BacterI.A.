/**
 * BacterI.A. — reference player (JavaScript) for the CodinGame IDE.
 * Use print() for actions (stdout). console.log does not count as player output.
 * Reads follow config/stub.txt (same order as WaitBot.java).
 * Referee resolves both players in deterministic phases (EXPAND -> ATTACK -> AUTOPHAGY -> WAIT).
 */

readline() // mapSize myIndex
const spotCount = parseInt(readline(), 10)
for (let i = 0; i < spotCount; i++) {
  readline()
}

while (true) {
  readline() // myEnergy oppEnergy
  const myCellCount = parseInt(readline(), 10)
  for (let i = 0; i < myCellCount; i++) readline()
  const oppCellCount = parseInt(readline(), 10)
  for (let i = 0; i < oppCellCount; i++) readline()
  const visibleSpotCount = parseInt(readline(), 10)
  for (let i = 0; i < visibleSpotCount; i++) readline()

  print('WAIT')
}
