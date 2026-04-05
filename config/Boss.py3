import sys

# Init
map_size = int(input())

# Game loop
while True:
    my_energy, opp_energy = map(int, input().split())
    visible_count = int(input())
    for _ in range(visible_count):
        input()
    print("WAIT", flush=True)
