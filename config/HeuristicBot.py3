from collections import deque

line = input().split()
MAP_SIZE = int(line[0])
MY_ID = int(line[1])

spot_count = int(input())
known_spots = {}
for _ in range(spot_count):
    sx, sy, st = map(int, input().split())
    known_spots[(sx, sy)] = st


def neighbors(x, y):
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            if dx == 0 and dy == 0:
                continue
            nx, ny = x + dx, y + dy
            if 0 <= nx < MAP_SIZE and 0 <= ny < MAP_SIZE:
                yield nx, ny


def step_toward(my_cells, target):
    visited = set([target])
    q = deque([(target[0], target[1], None)])
    while q:
        x, y, first = q.popleft()
        for nx, ny in neighbors(x, y):
            if (nx, ny) in visited:
                continue
            visited.add((nx, ny))
            step = first if first is not None else (x, y)
            if (nx, ny) in my_cells:
                return step
            q.append((nx, ny, step))
    return None


while True:
    my_energy, opp_energy = map(int, input().split())

    my_count = int(input())
    my_cells = set()
    for _ in range(my_count):
        x, y = map(int, input().split())
        my_cells.add((x, y))

    opp_count = int(input())
    opp_cells = set()
    for _ in range(opp_count):
        x, y = map(int, input().split())
        opp_cells.add((x, y))

    vis_count = int(input())
    visible_spots = []
    for _ in range(vis_count):
        sx, sy, st, rem = map(int, input().split())
        known_spots[(sx, sy)] = st
        if rem > 0:
            visible_spots.append((sx, sy))

    actions = []

    for ox, oy in sorted(opp_cells):
        if len(actions) >= 5 or my_energy < 2:
            break
        my_n = sum((nx, ny) in my_cells for nx, ny in neighbors(ox, oy))
        opp_n = sum((nx, ny) in opp_cells for nx, ny in neighbors(ox, oy))
        if my_n > opp_n:
            actions.append(f"ATTACK {ox} {oy}")
            my_energy -= 2

    for sx, sy in visible_spots:
        if len(actions) >= 5 or my_energy < 2:
            break
        step = step_toward(my_cells, (sx, sy))
        if step and step not in my_cells and step not in opp_cells:
            actions.append(f"EXPAND {step[0]} {step[1]}")
            my_energy -= 2

    if not actions:
        actions.append("WAIT")

    print(";".join(actions), flush=True)
