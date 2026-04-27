import random

line = input().split()
MAP_SIZE = int(line[0])
MY_ID = int(line[1])

spot_count = int(input())
for _ in range(spot_count):
    input()

while True:
    my_energy, opp_energy = map(int, input().split())
    my_count = int(input())
    for _ in range(my_count):
        input()
    opp_count = int(input())
    for _ in range(opp_count):
        input()
    vis_count = int(input())
    for _ in range(vis_count):
        input()

    action_count = random.randint(1, 5)
    actions = []
    for _ in range(action_count):
        t = random.randint(0, 3)
        x = random.randint(0, MAP_SIZE - 1)
        y = random.randint(0, MAP_SIZE - 1)
        if t == 0:
            actions.append(f"EXPAND {x} {y}")
        elif t == 1:
            actions.append(f"ATTACK {x} {y}")
        elif t == 2:
            actions.append(f"AUTOPHAGY {x} {y}")
        else:
            actions.append("WAIT")
    print(";".join(actions), flush=True)
