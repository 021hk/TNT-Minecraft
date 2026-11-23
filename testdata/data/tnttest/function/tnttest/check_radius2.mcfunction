scoreboard players set chk_left tnttest 1
# every stone block of the tier-3 test cube (inside r=24) must be gone
execute unless block 392 -58 0 minecraft:air run scoreboard players set chk_left tnttest 0
execute unless block 390 -56 -2 minecraft:air run scoreboard players set chk_left tnttest 0
execute unless block 394 -60 2 minecraft:air run scoreboard players set chk_left tnttest 0
# water must flow back into the tier-2 crater
scoreboard players set chk_water tnttest 1
execute unless block 615 -59 0 minecraft:water run scoreboard players set chk_water tnttest 0
execute unless block 613 -59 0 minecraft:water run scoreboard players set chk_water tnttest 0
execute unless block 615 -60 0 minecraft:water run scoreboard players set chk_water tnttest 0
