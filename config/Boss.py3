import sys
from collections import deque

# -----------------------------------------------------------------------
# Init
# -----------------------------------------------------------------------
line = input().split()
MAP_SIZE = int(line[0])
MY_ID = int(line[1])

spot_count = int(input())
known_spots = {}
for _ in range(spot_count):
    parts = input().split()
    sx, sy, stype = int(parts[0]), int(parts[1]), int(parts[2])
    max_e = {1: 10, 2: 30, 3: 70}.get(stype, 10)
    known_spots[(sx, sy)] = (stype, max_e)

turn = 0

def neighbors(x, y):
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            if dx == 0 and dy == 0:
                continue
            nx, ny = x + dx, y + dy
            if 0 <= nx < MAP_SIZE and 0 <= ny < MAP_SIZE:
                yield nx, ny

def bfs_next_step(my_cells_set, target_x, target_y):
    visited = set()
    queue = deque()
    queue.append((target_x, target_y, None))
    visited.add((target_x, target_y))
    while queue:
        cx, cy, first_step = queue.popleft()
        for nx, ny in neighbors(cx, cy):
            if (nx, ny) in visited:
                continue
            visited.add((nx, ny))
            step = first_step if first_step else (cx, cy)
            if (nx, ny) in my_cells_set:
                return step
            queue.append((nx, ny, step))
    return None

def count_my_neighbors(x, y, cell_set):
    return sum(1 for nx, ny in neighbors(x, y) if (nx, ny) in cell_set)

# -----------------------------------------------------------------------
# Game loop
# -----------------------------------------------------------------------
while True:
    turn += 1
    parts = input().split()
    my_energy = int(parts[0])
    opp_energy = int(parts[1])

    # --- Read my cells ---
    my_cell_count = int(input())
    my_cells = set()
    for _ in range(my_cell_count):
        t = input().split()
        my_cells.add((int(t[0]), int(t[1])))

    # --- Read opponent cells ---
    opp_cell_count = int(input())
    opp_cells = set()
    for _ in range(opp_cell_count):
        t = input().split()
        opp_cells.add((int(t[0]), int(t[1])))

    # --- Read visible spots ---
    vis_spot_count = int(input())
    visible_spots = {}
    for _ in range(vis_spot_count):
        t = input().split()
        sx, sy, stype, rem = int(t[0]), int(t[1]), int(t[2]), int(t[3])
        visible_spots[(sx, sy)] = (stype, rem)
        known_spots[(sx, sy)] = (stype, rem)

    actions = []

    # --- Phase: Attack adjacent enemies with numerical superiority ---
    if opp_cells and my_energy >= 2:
        for ox, oy in sorted(opp_cells):
            if len(actions) >= 5 or my_energy < 2:
                break
            my_n = count_my_neighbors(ox, oy, my_cells)
            opp_n = count_my_neighbors(ox, oy, opp_cells)
            adj_to_mine = any((nx, ny) in my_cells for nx, ny in neighbors(ox, oy))
            if adj_to_mine and my_n > opp_n:
                actions.append(f"ATTACK {ox} {oy}")
                my_energy -= 2
                # Do not assume capture or reward; referee state arrives next turn.

    # --- Phase: Expand toward nearest non-depleted nutrient ---
    undepleted = {pos: info for pos, info in known_spots.items() if info[1] > 0}
    if undepleted and my_energy >= 2:
        def spot_priority(pos):
            return min(max(abs(pos[0] - cx), abs(pos[1] - cy)) for cx, cy in my_cells)

        targets = sorted(undepleted.keys(), key=spot_priority)

        for target in targets:
            if len(actions) >= 5 or my_energy < 2:
                break
            step = bfs_next_step(my_cells, target[0], target[1])
            if step and step not in my_cells and step not in opp_cells:
                actions.append(f"EXPAND {step[0]} {step[1]}")
                my_cells.add(step)
                my_energy -= 2

    # --- Phase: If no nutrients visible, expand toward center / enemy ---
    if len(actions) < 5 and my_energy >= 2 and not undepleted:
        center = MAP_SIZE // 2
        target = (center, center)
        for _ in range(5 - len(actions)):
            if my_energy < 2:
                break
            step = bfs_next_step(my_cells, target[0], target[1])
            if step and step not in my_cells and step not in opp_cells:
                actions.append(f"EXPAND {step[0]} {step[1]}")
                my_cells.add(step)
                my_energy -= 2
            else:
                break

    # --- Phase: Recycle isolated cells (0-1 own neighbors) if low energy ---
    if my_energy < 4 and len(actions) < 5:
        for cx, cy in sorted(my_cells):
            if len(actions) >= 5:
                break
            n = count_my_neighbors(cx, cy, my_cells)
            if n <= 1 and len(my_cells) > 1:
                actions.append(f"AUTOPHAGY {cx} {cy}")
                my_cells.discard((cx, cy))
                my_energy += 1

    if not actions:
        actions.append("WAIT")

    print("; ".join(actions), flush=True)
